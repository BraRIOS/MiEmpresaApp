package com.brios.miempresa.core.util

import android.graphics.Bitmap

/**
 * Result wrapper for QR code generation operations.
 *
 * Follows sealed class pattern used in PriceValidationResult.
 * Provides type-safe error handling without exceptions bubbling to UI.
 */
sealed class QrCodeResult {
    /**
     * QR code generated successfully.
     *
     * @param bitmap Generated QR code bitmap (typically 512x512px)
     */
    data class Success(val bitmap: Bitmap) : QrCodeResult()

    /**
     * QR code generation failed.
     *
     * @param message Human-readable error message for debugging/logging
     */
    data class Error(val message: String) : QrCodeResult()
}
