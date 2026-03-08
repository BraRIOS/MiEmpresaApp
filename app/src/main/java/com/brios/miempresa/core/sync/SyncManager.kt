package com.brios.miempresa.core.sync

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.brios.miempresa.BuildConfig
import com.brios.miempresa.core.sync.workers.PeriodicSyncWorker
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
                    .addTag(SYNC_WORK_TAG)
                    .build()

            workManager.enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest,
            )
        }

        fun syncNow(type: SyncType = SyncType.ALL): UUID {
            val syncRequest =
                OneTimeWorkRequestBuilder<PeriodicSyncWorker>()
                    .setInputData(workDataOf(SYNC_TYPE_KEY to type.name))
                    .addTag(SYNC_WORK_TAG)
                    .build()
            workManager.enqueue(syncRequest)
            return syncRequest.id
        }

        fun observeWorkState(workId: UUID): Flow<WorkInfo.State?> =
            workManager
                .getWorkInfoByIdFlow(workId)
                .map { workInfo -> workInfo?.state }

        fun cancelAll() {
            val periodicCancellation = workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
            val syncTagCancellation = workManager.cancelAllWorkByTag(SYNC_WORK_TAG)
            runCatching { periodicCancellation.result.get() }
            runCatching { syncTagCancellation.result.get() }
            waitForSyncWorkToStop()
        }

        private fun waitForSyncWorkToStop() {
            val timeoutAt = System.currentTimeMillis() + WORK_CANCEL_TIMEOUT_MS
            while (System.currentTimeMillis() < timeoutAt) {
                val hasActiveSyncWork =
                    runCatching { workManager.getWorkInfosByTag(SYNC_WORK_TAG).get() }
                        .getOrDefault(emptyList())
                        .any { info ->
                            info.state == WorkInfo.State.ENQUEUED ||
                                info.state == WorkInfo.State.RUNNING ||
                                info.state == WorkInfo.State.BLOCKED
                        }

                if (!hasActiveSyncWork) return
                runCatching { Thread.sleep(WORK_CANCEL_POLL_INTERVAL_MS) }.getOrElse { return }
            }
        }

        companion object {
            const val SYNC_TYPE_KEY = "sync_type"
            private const val PERIODIC_SYNC_WORK_NAME = "periodic_sync"
            private const val SYNC_WORK_TAG = "sync_work"
            private const val WORK_CANCEL_TIMEOUT_MS = 5_000L
            private const val WORK_CANCEL_POLL_INTERVAL_MS = 100L
        }
    }
