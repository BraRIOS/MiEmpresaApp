package com.brios.miempresa.spike

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brios.miempresa.data.Category
import com.brios.miempresa.data.CategoryDao
import com.brios.miempresa.domain.GoogleAuthClient
import com.brios.miempresa.domain.SpreadsheetsApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncCategoriesWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val categoryDao: CategoryDao,
        private val googleAuthClient: GoogleAuthClient,
        private val spreadsheetsApi: SpreadsheetsApi,
    ) : CoroutineWorker(appContext, workerParams) {
        companion object {
            private const val TAG = "SyncWorker"
            const val INPUT_SHEET_ID = "sheetId"
            const val INPUT_COMPANY_ID = "companyId"
        }

        override suspend fun doWork(): Result {
            val sheetId = inputData.getString(INPUT_SHEET_ID)
            val companyId = inputData.getString(INPUT_COMPANY_ID)

            if (sheetId == null || companyId == null) {
                Log.e(TAG, "Missing required input: sheetId or companyId")
                return Result.failure()
            }

            Log.i(TAG, "Starting sync for company $companyId")
            val startTime = System.currentTimeMillis()

            return try {
                // PHASE 1: Upload dirty Room → Sheets (must run BEFORE download to avoid data loss)
                uploadDirtyCategories(sheetId, companyId)

                // PHASE 2: Download Sheets → Room
                downloadFromSheets(sheetId, companyId)

                val duration = System.currentTimeMillis() - startTime
                Log.i(TAG, "Sync completed successfully in ${duration}ms")
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed: ${e.message}", e)
                Result.failure()
            }
        }

        private suspend fun downloadFromSheets(
            sheetId: String,
            companyId: String,
        ) {
            Log.d(TAG, "PHASE 2: Downloading categories from Sheets...")
            val phaseStart = System.currentTimeMillis()

            val range = "Categories!A2:D"
            Log.d(TAG, "Reading range: $range from sheetId: $sheetId")
            val values = spreadsheetsApi.readRange(sheetId, range)
            Log.d(TAG, "API returned: values=${values?.size ?: 0} rows, first row: ${values?.firstOrNull()}")

            if (values.isNullOrEmpty()) {
                Log.i(TAG, "No categories found in Sheets")
                return
            }

            val categories =
                values.mapIndexed { index, row ->
                    if (row.size < 4) {
                        Log.w(TAG, "Skipping incomplete row at index $index: $row")
                        return@mapIndexed null
                    }
                    // Validate companyId from Sheets matches parameter (multitenancy security)
                    val sheetCompanyId = row[3].toString()
                    if (sheetCompanyId != companyId) {
                        Log.w(TAG, "Skipping row with mismatched companyId: $sheetCompanyId != $companyId")
                        return@mapIndexed null
                    }
                    Category(
                        id = row[0].toString(),
                        name = row[1].toString(),
                        icon = row[2].toString(),
                        companyId = companyId,
                        dirty = false,
                        lastSyncedAt = System.currentTimeMillis(),
                    )
                }.filterNotNull()

            categoryDao.upsertAll(categories)

            val phaseDuration = System.currentTimeMillis() - phaseStart
            Log.d(TAG, "Downloaded ${categories.size} categories in ${phaseDuration}ms")
        }

        private suspend fun uploadDirtyCategories(
            sheetId: String,
            companyId: String,
        ) {
            Log.d(TAG, "PHASE 1: Uploading dirty categories to Sheets...")
            val phaseStart = System.currentTimeMillis()

            val dirtyCategories = categoryDao.getDirty(companyId)

            if (dirtyCategories.isEmpty()) {
                Log.i(TAG, "No dirty categories to upload")
                return
            }

            Log.d(TAG, "Found ${dirtyCategories.size} dirty categories")

            // Determine starting row for append (current row count + 2 for header)
            val existingValues = spreadsheetsApi.readRange(sheetId, "Categories!A2:D")
            val startRow = (existingValues?.size ?: 0) + 2

            // Build rows for append (exclude column E with formula)
            val rows =
                dirtyCategories.map { category ->
                    listOf(
                        category.id,
                        category.name,
                        category.icon,
                        category.companyId,
                    )
                }

            // KNOWN LIMITATION (Spike): appendRows always appends, causing duplicates on repeated syncs.
            // Proper solution (check-then-update or batchUpdate upsert logic) deferred to MVP.
            // For spike: Accept duplicates as documented trade-off for simplicity.
            val range = "Categories!A$startRow:D"
            spreadsheetsApi.appendRows(sheetId, range, rows, "USER_ENTERED")

            // Mark as synced in Room
            val ids = dirtyCategories.map { it.id }
            val timestamp = System.currentTimeMillis()
            categoryDao.markSynced(ids, timestamp, companyId)

            val phaseDuration = System.currentTimeMillis() - phaseStart
            Log.d(TAG, "Uploaded ${dirtyCategories.size} categories in ${phaseDuration}ms")
        }
    }
