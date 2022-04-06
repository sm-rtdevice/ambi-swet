package com.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


class ImageProcessorUtils {

    companion object {

        fun load(fileName: String): BufferedImage {
            return ImageIO.read(File(fileName))
        }

        fun saveJpg(image: BufferedImage, fileName: String) {
            val output = File(fileName)
            ImageIO.write(image, "jpg", output)
        }

        fun toGray(source: BufferedImage): BufferedImage {
            val result = BufferedImage(source.width, source.height, source.type)

            for (x in 0 until source.width) {
                for (y in 0 until source.height) {

                    val color = Color(source.getRGB(x, y))
                    val blue = color.blue
                    val red = color.red
                    val green = color.green

                    // Применяем стандартный алгоритм для получения черно-белого изображения
                    val grey = (red * 0.299 + green * 0.587 + blue * 0.114).toInt()

                    result.setRGB(x, y, Color(grey, grey, grey).rgb)
                }
            }

            return result
        }

        fun createMonotonousImage(width: Int, height: Int, imageType: Int, avgColor: Color): BufferedImage {
            val result = BufferedImage(width, height, imageType)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    result.setRGB(x, y, avgColor.rgb)
                }
            }
            return result
        }

        fun getAverageColor(source: BufferedImage): Color {
            val count = source.width * source.height

            var red = 0L
            var green = 0L
            var blue = 0L

            for (x in 0 until source.width) {
                for (y in 0 until source.height) {
                    val color = Color(source.getRGB(x, y))
                    red += color.red
                    green += color.green
                    blue += color.blue
                }
            }

            return Color((red / count).toInt(), (green / count).toInt(), (blue / count).toInt())
        }
    }

}