package com.example.printtest

import android.graphics.Bitmap
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.ceil

object EscPosConverter {

    fun bitmapToBytes(bitmap: Bitmap): ByteArray
    {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val bytesByLine = ceil(bitmapWidth.toFloat() / 8f).toInt()

        val imageBytes = initGSv0Command(bytesByLine, bitmapHeight)

        var i = 8
        for (posY in 0 until bitmapHeight) {
            for (j in 0 until bytesByLine) {
                var b = 0
                for (k in 0..7) {
                    val posX = j * 8 + k
                    if (posX < bitmapWidth) {
                        val color = bitmap.getPixel(posX, posY)

                        if ((color.red < 160 || color.green < 160 || color.blue < 160))
                        {
                            b = b or (1 shl (7 - k))
                        }
                    }
                }
                imageBytes[i++] = b.toByte()
            }
        }
        return imageBytes
    }
    private fun initGSv0Command(bytesByLine: Int, bitmapHeight: Int): ByteArray
    {
        val xH = bytesByLine / 256
        val xL = bytesByLine - (xH * 256)
        val yH = bitmapHeight / 256
        val yL = bitmapHeight - (yH * 256)

        val imageBytes = ByteArray(8 + bytesByLine * bitmapHeight)
        imageBytes[0] = 0x1D
        imageBytes[1] = 0x76
        imageBytes[2] = 0x30
        imageBytes[3] = 0x00
        imageBytes[4] = xL.toByte()
        imageBytes[5] = xH.toByte()
        imageBytes[6] = yL.toByte()
        imageBytes[7] = yH.toByte()
        return imageBytes
    }
}