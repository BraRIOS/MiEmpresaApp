package com.brios.miempresa.domain

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Domain service for generating QR codes from text content.
 *
 * Used for:
 * - Generating catalog share QRs (US-021)
 * - Future: Product share QRs, promotional codes
 *
 * Stateless singleton safe for Hilt injection.
 */
class QrCodeGenerator {
    /**
     * Generates QR code bitmap from text content.
     *
     * @param content Text to encode (typically deeplink, e.g., "miempresa://catalogo?sheetId=xyz")
     * @param sizePx Size in pixels for both width and height (default 512px)
     * @return QrCodeResult.Success with bitmap, or QrCodeResult.Error if generation fails
     *
     * @sample
     * ```kotlin
     * val result = qrCodeGenerator.generate("miempresa://catalogo?sheetId=abc123")
     * when (result) {
     *     is QrCodeResult.Success -> displayQr(result.bitmap)
     *     is QrCodeResult.Error -> showError(result.message)
     * }
     * ```
     */
    fun generate(
        content: String,
        sizePx: Int = 512,
    ): QrCodeResult {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)

            // Convert BitMatrix to Bitmap
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            QrCodeResult.Success(bitmap)
        } catch (e: Exception) {
            // ZXing can throw WriterException, IllegalArgumentException, etc.
            QrCodeResult.Error("Failed to generate QR code: ${e.message}")
        }
    }
}
