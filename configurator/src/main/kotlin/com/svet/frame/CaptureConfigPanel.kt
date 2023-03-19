package com.svet.frame

import com.svet.capture.CaptureScreen
import com.svet.config.SvetConfig
import com.svet.processor.ImageProcessorUtils
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.Timer


class CaptureConfigPanel internal constructor() : JPanel(), ActionListener {

    private val svetConfig = SvetConfig()
    private val verticalFontAlignCorrection = 3
    private val captureScreen = CaptureScreen()
    val timer: Timer = Timer(16, this)
    private var fpsTime = System.currentTimeMillis()

    init {
        this.preferredSize = Dimension(640, 480)
        timer.start()
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == timer) {
            repaint()
        }
    }

    override fun paint(graphics: Graphics) {
        val repaintTime = System.currentTimeMillis()

        val g2D = graphics as Graphics2D

        g2D.paint = Color.RED
        drawGridLines(g2D)

        g2D.paint = Color.BLACK
        drawCaptureColoredRegions(g2D)

        val elapsedTime = System.currentTimeMillis() - repaintTime
        print("\rrepaint time: $elapsedTime ms. FPS: ${1000L / (System.currentTimeMillis() - fpsTime)}")

        fpsTime = System.currentTimeMillis()
    }

    private fun drawGridLines(g2D: Graphics2D) {
        g2D.drawLine(svetConfig.captureConfig.width / 2, 0, svetConfig.captureConfig.width / 2, svetConfig.captureConfig.height)
        g2D.drawLine(0, svetConfig.captureConfig.height / 2, svetConfig.captureConfig.width, svetConfig.captureConfig.height / 2)
    }

    private fun drawCaptureColoredRegion(
        g2D: Graphics2D, colors: List<Color>, offset: Int, areaNumLeds: Int,
        ox: Int, oy: Int,
        lx1: Int, ly1: Int, lx2: Int, ly2: Int
    ) {
        for (i in 0 until areaNumLeds) {

            val ledNumber = i + offset

            // вокруг области захвата
            g2D.paint = Color.RED
            g2D.drawRect(
                svetConfig.captureConfig.positions[ledNumber].x - svetConfig.captureConfig.border,
                svetConfig.captureConfig.positions[ledNumber].y - svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border
            )

            // вокруг усредненного цвета области захвата
            g2D.paint = Color.green
            g2D.drawRect(
                svetConfig.captureConfig.positions[ledNumber].x - svetConfig.captureConfig.border + ox,
                svetConfig.captureConfig.positions[ledNumber].y - svetConfig.captureConfig.border + oy,
                svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border
            )

            // усредненный цвет
            g2D.drawImage(
                ImageProcessorUtils.createMonotonousImage(
                    svetConfig.captureConfig.captureRegionWidth,
                    svetConfig.captureConfig.captureRegionHeight,
                    BufferedImage.TYPE_3BYTE_BGR,
                    colors[ledNumber]
                ),
                svetConfig.captureConfig.positions[ledNumber].x + ox,
                svetConfig.captureConfig.positions[ledNumber].y + oy,
                this
            )

            // номер области захвата
            val ledNumberStr = (ledNumber).toString()
            graphics.drawString(
                ledNumberStr,
                svetConfig.captureConfig.positions[ledNumber].x - svetConfig.captureConfig.border + ox + svetConfig.captureConfig.captureRegionWidth / 2 - graphics.fontMetrics.stringWidth(ledNumberStr) / 2,
                svetConfig.captureConfig.positions[ledNumber].y - svetConfig.captureConfig.border + oy + svetConfig.captureConfig.captureRegionHeight / 2 + verticalFontAlignCorrection
            )

            // соединительная линия
            g2D.paint = Color.YELLOW
            g2D.drawLine(
                svetConfig.captureConfig.positions[ledNumber].x + lx1,
                svetConfig.captureConfig.positions[ledNumber].y + ly1,
                svetConfig.captureConfig.positions[ledNumber].x + lx2,
                svetConfig.captureConfig.positions[ledNumber].y + ly2
            )
        }
    }

    private fun drawCaptureColoredRegions(g2D: Graphics2D) {
        val colors = captureScreen.getRegionsCaptureColors(captureScreen.capture(), svetConfig.captureConfig)

        g2D.stroke = BasicStroke(svetConfig.captureConfig.border.toFloat())

        val ox = svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.section
        val oy = svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.section

        val vLineX = svetConfig.captureConfig.captureRegionWidth / 2
        val hLineY = svetConfig.captureConfig.captureRegionHeight / 2

        // области захвата снизу слева
        var offset = 0
        drawCaptureColoredRegion(
            g2D, colors, offset, svetConfig.captureConfig.bottomCountL,
            0, oy * (-1),
            vLineX, svetConfig.captureConfig.border * (-1), vLineX, svetConfig.captureConfig.section * (-1)
        )

        // области захвата слева
        offset += svetConfig.captureConfig.bottomCountL
        drawCaptureColoredRegion(
            g2D, colors, offset, svetConfig.captureConfig.leftCount,
            ox, 0,
            svetConfig.captureConfig.captureRegionWidth, hLineY, svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.section - svetConfig.captureConfig.border, hLineY
        )

        // области захвата сверху
        offset += svetConfig.captureConfig.leftCount
        drawCaptureColoredRegion(
            g2D, colors, offset, svetConfig.captureConfig.topCount,
            0, oy,
            vLineX, svetConfig.captureConfig.captureRegionHeight, vLineX, svetConfig.captureConfig.section + svetConfig.captureConfig.captureRegionHeight - svetConfig.captureConfig.border
        )

        // области захвата справа
        offset += svetConfig.captureConfig.topCount
        drawCaptureColoredRegion(
            g2D, colors, offset, svetConfig.captureConfig.rightCount,
            ox * (-1), 0,
            (svetConfig.captureConfig.border) * (-1), hLineY, svetConfig.captureConfig.section * (-1), hLineY
        )

        // области захвата снизу справа
        offset += svetConfig.captureConfig.rightCount
        drawCaptureColoredRegion(
            g2D, colors, offset, svetConfig.captureConfig.bottomCountR, 0, oy * (-1),
            vLineX, svetConfig.captureConfig.border * (-1), vLineX, svetConfig.captureConfig.section * (-1)
        )
    }

    private fun drawCaptureRegionsColor(g2D: Graphics2D) {

        val colors = captureScreen.getRegionsCaptureColors(captureScreen.capture(), svetConfig.captureConfig)

        g2D.stroke = BasicStroke(svetConfig.captureConfig.border.toFloat())

        // области захвата снизу слева
        var offset = 0
        for (i in 0 until svetConfig.captureConfig.bottomCountL) {

            val x = svetConfig.captureConfig.positions[i].x + svetConfig.captureConfig.captureRegionWidth / 2
            val y = svetConfig.captureConfig.height - svetConfig.captureConfig.captureRegionHeight
            g2D.drawLine(x, y - svetConfig.captureConfig.border, x, y - svetConfig.captureConfig.section - svetConfig.captureConfig.border)

            graphics.drawRect(svetConfig.captureConfig.positions[i].x, svetConfig.captureConfig.positions[i].y, svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border)

            g2D.drawRect(
                svetConfig.captureConfig.positions[i].x - svetConfig.captureConfig.border,
                svetConfig.captureConfig.positions[i].y - svetConfig.captureConfig.captureRegionHeight - svetConfig.captureConfig.section - svetConfig.captureConfig.border * 2,
                svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border
            )

            g2D.drawImage(
                ImageProcessorUtils.createMonotonousImage(
                    svetConfig.captureConfig.captureRegionWidth,
                    svetConfig.captureConfig.captureRegionHeight,
                    BufferedImage.TYPE_3BYTE_BGR,
                    colors[i]
                ),
                svetConfig.captureConfig.positions[i].x,
                svetConfig.captureConfig.positions[i].y - svetConfig.captureConfig.captureRegionHeight - svetConfig.captureConfig.section - svetConfig.captureConfig.border,
                this
            )
        }

        // области захвата слева
        offset += svetConfig.captureConfig.bottomCountL
        for (i in 0 until svetConfig.captureConfig.leftCount) {

            val x = svetConfig.captureConfig.captureRegionWidth
            val y = svetConfig.captureConfig.positions[i + offset].y + svetConfig.captureConfig.captureRegionHeight / 2
            g2D.drawLine(x, y, x + svetConfig.captureConfig.section, y)

            g2D.drawRect(
                svetConfig.captureConfig.positions[i + offset].x - svetConfig.captureConfig.border + svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.section + svetConfig.captureConfig.border * 2,
                svetConfig.captureConfig.positions[i + offset].y - svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border
            )
        }

        // области захвата сверху
        offset += svetConfig.captureConfig.leftCount
        for (i in 0 until svetConfig.captureConfig.topCount) {

            val x = svetConfig.captureConfig.positions[i + offset].x + svetConfig.captureConfig.captureRegionWidth / 2
            val y = svetConfig.captureConfig.captureRegionHeight
            g2D.drawLine(x, y + svetConfig.captureConfig.border, x, y + svetConfig.captureConfig.section + svetConfig.captureConfig.border)

            g2D.drawRect(
                svetConfig.captureConfig.positions[i + offset].x - svetConfig.captureConfig.border,
                svetConfig.captureConfig.positions[i + offset].y - svetConfig.captureConfig.border + svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.section + svetConfig.captureConfig.border * 2,
                svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border
            )
        }

        // области захвата справа
        offset += svetConfig.captureConfig.topCount
        for (i in 0 until svetConfig.captureConfig.leftCount) {

            val x = svetConfig.captureConfig.width - svetConfig.captureConfig.captureRegionWidth
            val y = svetConfig.captureConfig.positions[i + offset].y + svetConfig.captureConfig.captureRegionHeight / 2
            g2D.drawLine(x, y, x - svetConfig.captureConfig.section, y)

            g2D.drawRect(
                svetConfig.captureConfig.positions[i + offset].x - svetConfig.captureConfig.border - svetConfig.captureConfig.captureRegionWidth - svetConfig.captureConfig.section - svetConfig.captureConfig.border * 2,
                svetConfig.captureConfig.positions[i + offset].y - svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border
            )
        }

        // области захвата снизу справа
        offset += svetConfig.captureConfig.rightCount
        for (i in 0 until svetConfig.captureConfig.bottomCountR) {

            val x = svetConfig.captureConfig.positions[i+ offset].x + svetConfig.captureConfig.captureRegionWidth / 2
            val y = svetConfig.captureConfig.height - svetConfig.captureConfig.captureRegionHeight
            g2D.drawLine(x, y - svetConfig.captureConfig.border, x, y - svetConfig.captureConfig.section - svetConfig.captureConfig.border)

            g2D.drawRect(
                svetConfig.captureConfig.positions[i + offset].x - svetConfig.captureConfig.border,
                svetConfig.captureConfig.positions[i + offset].y - svetConfig.captureConfig.border  - svetConfig.captureConfig.captureRegionWidth - svetConfig.captureConfig.section - svetConfig.captureConfig.border * 2,
                svetConfig.captureConfig.captureRegionWidth + svetConfig.captureConfig.border,
                svetConfig.captureConfig.captureRegionHeight + svetConfig.captureConfig.border
            )
        }
    }

    private fun drawCaptureRegions(graphics: Graphics) {
        val captureRegion = ImageProcessorUtils.createMonotonousImage(
            svetConfig.captureConfig.captureRegionWidth,
            svetConfig.captureConfig.captureRegionHeight,
            BufferedImage.TYPE_3BYTE_BGR,
            Color.GREEN
        )

        for (i in svetConfig.captureConfig.positions.indices) {
            graphics.drawImage(captureRegion, svetConfig.captureConfig.positions[i].x, svetConfig.captureConfig.positions[i].y, this)

            val regionNumStr = i.toString()
            graphics.drawString(
                regionNumStr,
                svetConfig.captureConfig.positions[i].x + svetConfig.captureConfig.captureRegionWidth / 2 - graphics.fontMetrics.stringWidth(regionNumStr) / 2,
                svetConfig.captureConfig.positions[i].y + svetConfig.captureConfig.captureRegionHeight / 2 + verticalFontAlignCorrection
            )
        }
    }

    private fun drawCaptureRegionsAvgColors(graphics: Graphics) {
        val colors = captureScreen.getRegionsCaptureColors(captureScreen.capture(), svetConfig.captureConfig)

        for (i in svetConfig.captureConfig.positions.indices) {
            graphics.drawImage(
                ImageProcessorUtils.createMonotonousImage(
                    svetConfig.captureConfig.captureRegionWidth,
                    svetConfig.captureConfig.captureRegionHeight,
                    BufferedImage.TYPE_3BYTE_BGR,
                    colors[i]
                ),
                svetConfig.captureConfig.positions[i].x,
                svetConfig.captureConfig.positions[i].y,
                this
            )

            val regionNumStr = i.toString()
            graphics.drawString(
                regionNumStr,
                svetConfig.captureConfig.positions[i].x + svetConfig.captureConfig.captureRegionWidth / 2 - graphics.fontMetrics.stringWidth(regionNumStr) / 2,
                svetConfig.captureConfig.positions[i].y + svetConfig.captureConfig.captureRegionHeight / 2 + verticalFontAlignCorrection
            )
        }
    }

}
