package com.brios.miempresa

import android.app.Application
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.brios.miempresa.core.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MiEmpresa : Application(), Configuration.Provider, ImageLoaderFactory {
    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    @Inject
    lateinit var syncManager: SyncManager

    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

    override fun onCreate() {
        super.onCreate()
        syncManager.schedulePeriodic()
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .memoryCache {
                MemoryCache
                    .Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }.diskCache {
                DiskCache
                    .Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizeBytes(250 * 1024 * 1024) // 250 MB
                    .build()
            }.crossfade(true)
            .respectCacheHeaders(false)
            .build()
}
