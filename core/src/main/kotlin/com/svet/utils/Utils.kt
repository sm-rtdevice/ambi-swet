package com.svet.utils

import com.svet.config.CaptureConfig
import com.svet.processor.ImageProcessorUtils
import java.awt.Color
import java.awt.image.BufferedImage

class Utils {
    // how to use, send to COM port: toAdaBuffer(getRegionsCaptureColors(..))
    companion object {

        // подготовка буффера для отправки в COM порт
        fun toAdaBuffer(regionCaptureColors: List<Color>): List<Byte> {

            val buffer = ArrayList<Byte>(288) // captureConfig.initialCapacity

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
                result.add(ImageProcessorUtils.getAverageColor(
                    capturedScreenshot.getSubimage(
                        captureConfig.positions[i].x,
                        captureConfig.positions[i].y,
                        captureConfig.captureRegionWidth,
                        captureConfig.captureRegionHeight
                    )
                ))

//                TODO: reuse array
//                result[i] = ImageProcessorUtils.getAverageColor(
//                    capturedScreenshot.getSubimage(
//                        captureConfig.positions[i].x,
//                        captureConfig.positions[i].y,
//                        captureConfig.captureRegionWidth,
//                        captureConfig.captureRegionHeight
//                    )
//                )
            }

            return result
        }


        fun preparerRandomBuffer(captureConfig: CaptureConfig): List<Byte> {
            val buffer = ArrayList<Byte>(captureConfig.initialCapacity)

            val bright: Byte = 100 // 1..127: 1 - max яркость, 127 - min яркость

            val hi: Byte = 0
            val lo: Byte = 0
            val chk: Byte = 0x55

            buffer.addAll(listOf('A'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte())) // заголовок
            buffer.addAll(listOf(hi, lo, chk)) // CRC?

            for (i in 1..captureConfig.ledsCount) {
                buffer.addAll(listOf(
//            (-128..127).random().toByte(),  // R
//            (-128..127).random().toByte(),  // G
//            (-128..127).random().toByte())) // B
                    (0..127/bright).random().toByte(),  // R
                    (0..127/bright).random().toByte(),  // G
                    (0..127/bright).random().toByte())) // B
            }

            return buffer
        }

        fun prepareBufferForAdaSketch(captureConfig: CaptureConfig): List<Byte> {
            val buffer = ArrayList<Byte>(captureConfig.initialCapacity)

            val hi: Byte = 0
            val lo: Byte = 0
            val chk: Byte = 0x55

            buffer.clear()
            buffer.addAll(listOf('A'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte())) // заголовок
            buffer.addAll(listOf(hi, lo, chk)) // CRC?

            for (i in 1..captureConfig.ledsCount) {
                buffer.addAll(listOf(1, -128, 0)) // R, G, B
            }

            return buffer
        }

        fun prepareBuffer(captureConfig: CaptureConfig): List<Byte> {
            val buffer = ArrayList<Byte>(captureConfig.initialCapacity)

            val hi: Byte = 0
            val lo: Byte = 0
            val chk: Byte = 0x55

            buffer.addAll(listOf(hi, lo, chk)) // CRC?

            buffer.addAll(listOf(0x7F, 0x00, 0x00)) // 1 R
            buffer.addAll(listOf(0x00, 0x7F, 0x00)) // 2 G
            buffer.addAll(listOf(0x00, 0x00, 0x7F)) // 3 B
            buffer.addAll(listOf(-0x7F, 0x00, 0x00)) // 4 R
            buffer.addAll(listOf(0x00, -0x7F, 0x00)) // 5 G
            buffer.addAll(listOf(0x00, 0x00, 0)) // 6 B
            for (i in 1..captureConfig.ledsCount - 6) {
                buffer.addAll(listOf(0, 10, 10)) // R, G, B [-0x7F .. 0x7F]
            }

            return buffer
        }

    }



//    @Throws(Exception::class)
//    fun getCapacity(al: List<*>?): Int {
//        val field: Field = ArrayList::class.java.getDeclaredField("elementData")
//        field.isAccessible = true
//        return (field.get(al) as Array<*>).size
//    }

}
