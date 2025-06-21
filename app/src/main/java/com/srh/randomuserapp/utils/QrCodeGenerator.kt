package com.srh.randomuserapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for generating QR codes for users.
 * Each user gets a unique QR code that can be scanned in AR mode.
 */
@Singleton
class QrCodeGenerator @Inject constructor() {

    /**
     * Generates QR code data string for a user
     * Format: "RANDOMUSER:{userId}"
     *
     * @param userId Unique user identifier
     * @return QR code data string
     */
    fun generateQrCodeData(userId: String): String {
        return "RANDOMUSER:$userId"
    }

    /**
     * Generates a QR code bitmap from user ID
     *
     * @param userId Unique user identifier
     * @param size Size of the QR code in pixels (default 512)
     * @return Bitmap containing the QR code
     */
    fun generateQrCodeBitmap(userId: String, size: Int = 512): Bitmap {
        val qrCodeData = generateQrCodeData(userId)
        return generateBitmapFromData(qrCodeData, size)
    }

    /**
     * Generates a QR code bitmap from data string
     *
     * @param data QR code data string
     * @param size Size of the QR code in pixels
     * @return Bitmap containing the QR code
     */
    fun generateBitmapFromData(data: String, size: Int = 512): Bitmap {
        val writer = QRCodeWriter()

        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
        }

        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }

        return bitmap
    }

    /**
     * Extracts user ID from QR code data
     *
     * @param qrCodeData Scanned QR code data
     * @return User ID if valid QR code, null otherwise
     */
    fun extractUserIdFromQrCode(qrCodeData: String): String? {
        return if (qrCodeData.startsWith("RANDOMUSER:")) {
            qrCodeData.substringAfter("RANDOMUSER:")
        } else {
            null
        }
    }

    /**
     * Validates if QR code data is for this app
     *
     * @param qrCodeData Scanned QR code data
     * @return True if valid app QR code, false otherwise
     */
    fun isValidAppQrCode(qrCodeData: String): Boolean {
        return qrCodeData.startsWith("RANDOMUSER:") &&
                qrCodeData.length > "RANDOMUSER:".length
    }
}