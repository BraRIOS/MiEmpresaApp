package com.brios.miempresa.core.sync.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
            return try {
                syncCoordinator.syncAll().fold(
                    onSuccess = { Result.success() },
                    onFailure = { Result.retry() },
                )
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
