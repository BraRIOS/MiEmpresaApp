package com.brios.miempresa.spike

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.brios.miempresa.data.Category
import com.brios.miempresa.data.CategoryDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpikeInsertDirtyCategoryActivity : ComponentActivity() {
    @Inject
    lateinit var categoryDao: CategoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val dirtyCategory =
                Category(
                    id = "cat-003",
                    name = "Carnes",
                    icon = "🥩",
                    companyId = "comp-test-123",
                    dirty = true,
                    lastSyncedAt = null,
                )

            categoryDao.upsertAll(listOf(dirtyCategory))
            Log.d("SpikeInsertDirty", "✅ Inserted dirty category: $dirtyCategory")

            finish()
        }
    }
}
