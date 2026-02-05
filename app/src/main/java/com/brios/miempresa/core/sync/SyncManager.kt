package com.brios.miempresa.core.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.brios.miempresa.core.sync.workers.PeriodicSyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncType {
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
            val syncRequest =
                PeriodicWorkRequestBuilder<PeriodicSyncWorker>(
                    15,
                    TimeUnit.MINUTES,
                ).build()

            workManager.enqueueUniquePeriodicWork(
                "periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest,
            )
        }

        fun syncNow(type: SyncType) {
            // Manual sync trigger (future: OneTimeWorkRequest)
        }

        fun cancelAll() {
            workManager.cancelUniqueWork("periodic_sync")
        }
    }
