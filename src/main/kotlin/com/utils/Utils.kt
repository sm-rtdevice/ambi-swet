package com.utils

import com.configs.CaptureConfig
import java.awt.Color
import java.awt.image.BufferedImage

class Utils {
    // how to use, send to COM port: toAdaBuffer(getRegionsCaptureColors(..))
    companion object {

        // подготовка буффера для отправки в COM порт
        fun toAdaBuffer(regionCaptureColors: List<Color>): List<Byte> {

            val buffer = ArrayList<Byte>(288) // from config: size =  94 * 3 + 3 + 3

            val hi: Byte = 0
            val lo: Byte = 0
            val chk: Byte = 0x55

            buffer.addAll(listOf('A'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte())) // заголовок
            buffer.addAll(listOf(hi, lo, chk)) // CRC?

            for (i in regionCaptureColors.indices step 3) {
                buffer[i] = regionCaptureColors[i].red.toByte()
                buffer[i + 1] = regionCaptureColors[i + 1].green.toByte()
                buffer[i + 2] = regionCaptureColors[i + 2].blue.toByte()
            }

            return buffer
        }

        // определение усредненного цвета областей захвата
        fun getRegionsCaptureColors(capturedScreenshot: BufferedImage, captureConfig: CaptureConfig): List<Color> {
            val result = ArrayList<Color>(captureConfig.positions.size)

            for (i in captureConfig.positions.indices) {
                result[i] = ImageProcessorUtils.getAverageColor(
                    capturedScreenshot.getSubimage(
                        captureConfig.positions[i].x,
                        captureConfig.positions[i].y,
                        captureConfig.captureRegionWidth,
                        captureConfig.captureRegionHeight
                    )
                )
            }

            return result
        }

    }

//    @Throws(Exception::class)
//    fun getCapacity(al: List<*>?): Int {
//        val field: Field = ArrayList::class.java.getDeclaredField("elementData")
//        field.isAccessible = true
//        return (field.get(al) as Array<*>).size
//    }

}