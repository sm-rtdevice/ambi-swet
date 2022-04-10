package com

import mu.KotlinLogging
import java.awt.*
import java.awt.image.BufferedImage
import java.io.IOException

private val logger = KotlinLogging.logger {}

class CaptureScreen {

    private val screenRect = Rectangle(Toolkit.getDefaultToolkit().screenSize)
    private lateinit var capture: BufferedImage

    private var startTime = 0L
    private var fps = 0L
    private val robot = Robot()

    @Throws(AWTException::class, IOException::class)
    fun capture() {
        startTime = System.currentTimeMillis()
        capture = robot.createScreenCapture(screenRect)
        val color = Color(capture.getRGB(100, 100))
        //ImageIO.write(capture, "bmp", File("C:\\pictures\\captured_image.bmp"))

        ++fps
        if (System.currentTimeMillis() - startTime > 1000) {
            startTime = System.currentTimeMillis()
            println("$color; fps: $fps")
            fps = 0
        }

    }
}