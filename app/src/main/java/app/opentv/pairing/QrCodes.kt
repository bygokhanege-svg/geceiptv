/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.pairing

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodes {

    /**
     * Renders [content] as a QR code.
     *
     * Drawn black-on-white with a wide quiet zone even though the rest of the app is dark.
     * Inverted QR codes are readable by some phone cameras and not others, and a code that
     * works on the maintainer's phone but not the user's is worse than no feature at all.
     *
     * Error correction is set to M rather than L: this is being photographed off a television
     * across a room, at an angle, with reflections.
     */
    fun render(content: String, sizePx: Int): Bitmap? = runCatching {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to QUIET_ZONE_MODULES,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }.getOrNull()

    /**
     * Decodes a QR code string from raw camera YUV (NV21) frame data.
     */
    fun decodeYuv(data: ByteArray, width: Int, height: Int): String? {
        val direct = runCatching {
            val source = com.google.zxing.PlanarYUVLuminanceSource(
                data, width, height, 0, 0, width, height, false
            )
            val bitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
            com.google.zxing.qrcode.QRCodeReader().decode(bitmap).text
        }.getOrNull()
        if (direct != null) return direct

        // Fallback: rotate 90 degrees for portrait phone orientation
        return runCatching {
            val rotated = ByteArray(data.size)
            var i = 0
            for (x in 0 until width) {
                for (y in height - 1 downTo 0) {
                    rotated[i++] = data[y * width + x]
                }
            }
            val source = com.google.zxing.PlanarYUVLuminanceSource(
                rotated, height, width, 0, 0, height, width, false
            )
            val bitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
            com.google.zxing.qrcode.QRCodeReader().decode(bitmap).text
        }.getOrNull()
    }

    /**
     * Decodes a QR code string from an Android Bitmap.
     */
    fun decodeBitmap(bitmap: Bitmap): String? = runCatching {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = com.google.zxing.RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
        com.google.zxing.qrcode.QRCodeReader().decode(binaryBitmap).text
    }.getOrNull()

    private const val QUIET_ZONE_MODULES = 2
}

