package com.brios.miempresa.core.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brios.miempresa.core.sync.SyncManager
import com.brios.miempresa.core.sync.SyncType
import com.brios.miempresa.core.sync.domain.SyncCoordinator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PeriodicSyncWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val syncCoordinator: SyncCoordinator,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result {
            val syncTypeName = inputData.getString(SyncManager.SYNC_TYPE_KEY)
            val syncType =
                syncTypeName?.let {
                    try {
                        SyncType.valueOf(it)
                    } catch (_: IllegalArgumentException) {
                        SyncType.ALL
                    }
                } ?: SyncType.ALL

            return try {
                val result =
                    when (syncType) {
                        SyncType.ALL -> syncCoordinator.syncAll()
                        SyncType.PRODUCTS -> syncCoordinator.syncProducts()
                        SyncType.CATEGORIES -> syncCoordinator.syncCategories()
                        SyncType.ORDERS -> syncCoordinator.syncOrders()
                    }
                result.fold(
                    onSuccess = { Result.success() },
                    onFailure = { Result.retry() },
                )
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
