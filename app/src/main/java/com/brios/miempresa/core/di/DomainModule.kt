package com.brios.miempresa.core.di

import com.brios.miempresa.core.util.QrCodeGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing domain services.
 *
 * Services:
 * - QrCodeGenerator: Stateless QR generation utility
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    /**
     * Provides QrCodeGenerator as singleton.
     *
     * Singleton safe because:
     * - Stateless utility (no mutable state)
     * - Thread-safe (ZXing QRCodeWriter created per-call)
     */
    @Provides
    @Singleton
    fun provideQrCodeGenerator(): QrCodeGenerator {
        return QrCodeGenerator()
    }
}
