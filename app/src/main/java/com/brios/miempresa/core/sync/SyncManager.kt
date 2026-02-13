package com.brios.miempresa.core.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.brios.miempresa.BuildConfig
import com.brios.miempresa.core.sync.workers.PeriodicSyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncType {
    ALL,
    PRODUCTS,
    CATEGORIES,
    ORDERS,
}

@Singleton
class SyncManager
    @Inject
    constructor(
        private val workManager: WorkManager,
    ) {
        fun schedulePeriodic() {
            val constraints =
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val syncRequest =
                PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
                    BuildConfig.SYNC_PERIOD_MINUTES,
                    TimeUnit.MINUTES,
                )
                    .setConstraints(constraints)
                    .build()

            workManager.enqueueUniquePeriodicWork(
                "periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest,
            )
        }

        fun syncNow(type: SyncType = SyncType.ALL) {
            val syncRequest =
                OneTimeWorkRequestBuilder<PeriodicSyncWorker>()
                    .setInputData(workDataOf(SYNC_TYPE_KEY to type.name))
                    .build()
            workManager.enqueue(syncRequest)
        }

        fun cancelAll() {
            workManager.cancelUniqueWork("periodic_sync")
        }

        companion object {
            const val SYNC_TYPE_KEY = "sync_type"
        }
    }
