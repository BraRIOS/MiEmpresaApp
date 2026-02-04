package com.brios.miempresa.spike

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.brios.miempresa.domain.DriveApi
import com.brios.miempresa.domain.GoogleAuthClient
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SpikeSetupActivity : ComponentActivity() {
    @Inject
    lateinit var driveApi: DriveApi

    @Inject
    lateinit var googleAuthClient: GoogleAuthClient

    private val FOLDER_NAME = "Spike S2 Test Company"
    private val SHEET_NAME = "Spike S2 - Categories Sync Test"

    companion object {
        private const val TAG = "SpikeS2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                setupTestSheet()
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up test sheet", e)
            } finally {
                // Delay to allow viewing logs before activity closes
                delay(3000)
                if (!isFinishing && !isDestroyed) {
                    finish()
                }
            }
        }
    }

    private suspend fun setupTestSheet() =
        withContext(Dispatchers.IO) {
            val driveService = googleAuthClient.getGoogleDriveService()
            val sheetsService = googleAuthClient.getGoogleSheetsService()

            if (driveService == null || sheetsService == null) {
                Log.e(TAG, "Failed to get Google services")
                return@withContext
            }

            // Step 1: Create folder "Spike S2 Test Company"
            val folderMetadata =
                com.google.api.services.drive.model.File().apply {
                    name = FOLDER_NAME
                    mimeType = "application/vnd.google-apps.folder"
                    parents = listOf("root")
                }

            val folder =
                driveService.files().create(folderMetadata)
                    .setFields("id, name")
                    .execute()

            Log.d(TAG, "Created folder: ${folder.name} (${folder.id})")

            // Step 2: Create Sheet "Spike S2 - Categories Sync Test"
            val spreadsheet =
                Spreadsheet().apply {
                    properties = SpreadsheetProperties().setTitle(SHEET_NAME)
                    sheets =
                        listOf(
                            Sheet().apply {
                                properties = SheetProperties().setTitle("Categories")
                            },
                        )
                }

            val createdSpreadsheet = sheetsService.spreadsheets().create(spreadsheet).execute()

            // Move to folder
            driveService.files().update(createdSpreadsheet.spreadsheetId, null)
                .setAddParents(folder.id)
                .setFields("id, parents")
                .execute()

            Log.d(TAG, "Created spreadsheet: ${createdSpreadsheet.spreadsheetId}")

            // Step 3: Add headers: id, name, icon, companyId, count
            val headers =
                ValueRange()
                    .setRange("Categories!A1:E1")
                    .setValues(
                        listOf(
                            listOf("id", "name", "icon", "companyId", "count"),
                        ),
                    )

            sheetsService.spreadsheets().values()
                .update(createdSpreadsheet.spreadsheetId, "Categories!A1:E1", headers)
                .setValueInputOption("RAW")
                .execute()

            Log.d(TAG, "Added headers")

            // Step 4: Add 2 initial rows
            val rows =
                ValueRange()
                    .setRange("Categories!A2:D3")
                    .setValues(
                        listOf(
                            listOf("cat-001", "Vinos", "🍷", "comp-test-123"),
                            listOf("cat-002", "Aceites", "🫒", "comp-test-123"),
                        ),
                    )

            sheetsService.spreadsheets().values()
                .update(createdSpreadsheet.spreadsheetId, "Categories!A2:D3", rows)
                .setValueInputOption("RAW")
                .execute()

            Log.d(TAG, "Added initial rows")

            // Step 5: Add formula in E2: =COUNTA(A2:A)
            val formula =
                ValueRange()
                    .setRange("Categories!E2")
                    .setValues(
                        listOf(
                            listOf("=COUNTA(A2:A)"),
                        ),
                    )

            sheetsService.spreadsheets().values()
                .update(createdSpreadsheet.spreadsheetId, "Categories!E2", formula)
                .setValueInputOption("USER_ENTERED")
                .execute()

            Log.d(TAG, "Added formula in E2")

            // Step 6: Log sheetId
            Log.d(TAG, "✅ Setup complete! Sheet ID: ${createdSpreadsheet.spreadsheetId}")
        }
}
