package com.brios.miempresa.spike

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Spike S2 Activity: Manual trigger for SyncCategoriesWorker
 * Enqueues Worker with test data and auto-closes
 */
class SpikeWorkerTriggerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sheetId = "1BaFFd80Me1M0UGsebJipo8wipJXmxmo7BWoOPFUwXLY"
        val companyId = "comp-test-123"

        // Build InputData
        val inputData =
            Data.Builder()
                .putString("sheetId", sheetId)
                .putString("companyId", companyId)
                .build()

        // Set network constraint
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        // Create OneTimeWorkRequest
        val syncRequest =
            OneTimeWorkRequestBuilder<SyncCategoriesWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .build()

        // Enqueue Worker
        WorkManager.getInstance(applicationContext).enqueue(syncRequest)

        val workerId = syncRequest.id
        Log.d("SpikeWorkerTrigger", "✅ Worker enqueued: $workerId")
        Log.d("SpikeWorkerTrigger", "📊 Monitor with: adb shell dumpsys jobscheduler | grep androidx.work")
        Log.d("SpikeWorkerTrigger", "🔍 Check status: WorkManager.getInstance(context).getWorkInfoById(UUID.fromString(\"$workerId\"))")

        // Auto-close after 2 seconds
        lifecycleScope.launch {
            delay(2000L)
            finish()
        }
    }
}
