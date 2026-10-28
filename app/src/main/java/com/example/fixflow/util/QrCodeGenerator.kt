package com.example.fixflow.util

import android.graphics.Bitmap
import android.graphics.Color

object QrCodeGenerator {

    /**
     * Generates a clean QR code style matrix Bitmap for a repair Job ID or verification string.
     */
    fun generateQrCodeBitmap(data: String, size: Int = 300): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val hash = data.hashCode()

        // Generate a 15x15 barcode pattern based on data hash
        val modules = 15
        val moduleSize = size / modules

        for (x in 0 until modules) {
            for (y in 0 until modules) {
                // Fixed finder patterns at 3 corners
                val isFinderCorner = (x in 0..2 && y in 0..2) ||
                                     (x in (modules - 3) until modules && y in 0..2) ||
                                     (x in 0..2 && y in (modules - 3) until modules)

                val isBlack = if (isFinderCorner) {
                    true
                } else {
                    ((x * 31 + y * 17 + hash) % 3 == 0)
                }

                val pixelColor = if (isBlack) Color.BLACK else Color.WHITE
                for (px in 0 until moduleSize) {
                    for (py in 0 until moduleSize) {
                        val posX = x * moduleSize + px
                        val posY = y * moduleSize + py
                        if (posX < size && posY < size) {
                            bitmap.setPixel(posX, posY, pixelColor)
                        }
                    }
                }
            }
        }
        return bitmap
    }
}
