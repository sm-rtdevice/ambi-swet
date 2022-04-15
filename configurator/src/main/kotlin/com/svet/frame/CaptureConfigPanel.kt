package com.svet.frame

import com.svet.capture.CaptureScreen
import com.svet.config.CaptureConfig
import com.svet.processor.ImageProcessorUtils
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.Timer


class CaptureConfigPanel internal constructor() : JPanel(), ActionListener {

    private val config: CaptureConfig
    private val verticalFontAlignCorrection = 3
    private val captureScreen: CaptureScreen
    val timer: Timer = Timer(16, this)
    private var fpsTime = System.currentTimeMillis()

    init {
        this.preferredSize = Dimension(640, 480)

        config = CaptureConfig()
        config.init()
        captureScreen = CaptureScreen()

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
        g2D.drawLine(config.width / 2, 0, config.width / 2, config.height)
        g2D.drawLine(0, config.height / 2, config.width, config.height / 2)
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
                config.positions[ledNumber].x - config.border,
                config.positions[ledNumber].y - config.border,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )

            // вокруг усредненного цвета области захвата
            g2D.paint = Color.green
            g2D.drawRect(
                config.positions[ledNumber].x - config.border + ox,
                config.positions[ledNumber].y - config.border + oy,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )

            // усредненный цвет
            g2D.drawImage(
                ImageProcessorUtils.createMonotonousImage(
                    config.captureRegionWidth,
                    config.captureRegionHeight,
                    BufferedImage.TYPE_3BYTE_BGR,
                    colors[ledNumber]
                ),
                config.positions[ledNumber].x + ox,
                config.positions[ledNumber].y + oy,
                this
            )

            // номер области захвата
            val ledNumberStr = (ledNumber).toString()
            graphics.drawString(
                ledNumberStr,
                config.positions[ledNumber].x - config.border + ox + config.captureRegionWidth / 2 - graphics.fontMetrics.stringWidth(ledNumberStr) / 2,
                config.positions[ledNumber].y - config.border + oy + config.captureRegionHeight / 2 + verticalFontAlignCorrection
            )

            // соединительная линия
            g2D.paint = Color.YELLOW
            g2D.drawLine(
                config.positions[ledNumber].x + lx1,
                config.positions[ledNumber].y + ly1,
                config.positions[ledNumber].x + lx2,
                config.positions[ledNumber].y + ly2
            )

        }

    }

    private fun drawCaptureColoredRegions(g2D: Graphics2D) {
        val colors = captureScreen.getRegionsCaptureColors(captureScreen.capture(), config)

        g2D.stroke = BasicStroke(config.border.toFloat())

        val ox = config.captureRegionWidth + config.section
        val oy = config.captureRegionHeight + config.section

        val vLineX = config.captureRegionWidth / 2
        val hLineY = config.captureRegionHeight / 2

        // области захвата снизу слева
        var offset = 0
        drawCaptureColoredRegion(
            g2D, colors, offset, config.bottomCountL,
            0, oy * (-1),
            vLineX, config.border * (-1), vLineX, config.section * (-1)
        )

        // области захвата слева
        offset += config.bottomCountL
        drawCaptureColoredRegion(
            g2D, colors, offset, config.leftCount,
            ox, 0,
            config.captureRegionWidth, hLineY, config.captureRegionWidth + config.section - config.border, hLineY
        )

        // области захвата сверху
        offset += config.leftCount
        drawCaptureColoredRegion(
            g2D, colors, offset, config.topCount,
            0, oy,
            vLineX, config.captureRegionHeight, vLineX, config.section + config.captureRegionHeight - config.border
        )

        // области захвата справа
        offset += config.topCount
        drawCaptureColoredRegion(
            g2D, colors, offset, config.rightCount,
            ox * (-1), 0,
            (config.border) * (-1), hLineY, config.section * (-1), hLineY
        )

        // области захвата снизу справа
        offset += config.rightCount
        drawCaptureColoredRegion(
            g2D, colors, offset, config.bottomCountR, 0, oy * (-1),
            vLineX, config.border * (-1), vLineX, config.section * (-1)
        )
    }

    private fun drawCaptureRegionsColor(g2D: Graphics2D) {

        val colors = captureScreen.getRegionsCaptureColors(captureScreen.capture(), config)

        g2D.stroke = BasicStroke(config.border.toFloat())

        // области захвата снизу слева
        var offset = 0
        for (i in 0 until config.bottomCountL) {

            val x = config.positions[i].x + config.captureRegionWidth / 2
            val y = config.height - config.captureRegionHeight
            g2D.drawLine(x, y - config.border, x, y - config.section - config.border)

            graphics.drawRect(config.positions[i].x, config.positions[i].y, config.captureRegionWidth + config.border,config.captureRegionHeight + config.border)

            g2D.drawRect(
                config.positions[i].x - config.border,
                config.positions[i].y - config.captureRegionHeight - config.section - config.border * 2,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )

            g2D.drawImage(
                ImageProcessorUtils.createMonotonousImage(
                    config.captureRegionWidth,
                    config.captureRegionHeight,
                    BufferedImage.TYPE_3BYTE_BGR,
                    colors[i]
                ),
                config.positions[i].x,
                config.positions[i].y - config.captureRegionHeight - config.section - config.border,
                this
            )

        }

        // области захвата слева
        offset += config.bottomCountL
        for (i in 0 until config.leftCount) {

            val x = config.captureRegionWidth
            val y = config.positions[i + offset].y + config.captureRegionHeight / 2
            g2D.drawLine(x, y, x + config.section, y)

            g2D.drawRect(
                config.positions[i + offset].x - config.border + config.captureRegionWidth + config.section + config.border * 2,
                config.positions[i + offset].y - config.border,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )
        }

        // области захвата сверху
        offset += config.leftCount
        for (i in 0 until config.topCount) {

            val x = config.positions[i + offset].x + config.captureRegionWidth / 2
            val y = config.captureRegionHeight
            g2D.drawLine(x, y + config.border, x, y + config.section + config.border)

            g2D.drawRect(
                config.positions[i + offset].x - config.border,
                config.positions[i + offset].y - config.border + config.captureRegionHeight + config.section + config.border * 2,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )
        }

        // области захвата справа
        offset += config.topCount
        for (i in 0 until config.leftCount) {

            val x = config.width - config.captureRegionWidth
            val y = config.positions[i + offset].y + config.captureRegionHeight / 2
            g2D.drawLine(x, y, x - config.section, y)

            g2D.drawRect(
                config.positions[i + offset].x - config.border - config.captureRegionWidth - config.section - config.border * 2,
                config.positions[i + offset].y - config.border,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )
        }

        // области захвата снизу справа
        offset += config.rightCount
        for (i in 0 until config.bottomCountR) {

            val x = config.positions[i+ offset].x + config.captureRegionWidth / 2
            val y = config.height - config.captureRegionHeight
            g2D.drawLine(x, y - config.border, x, y - config.section - config.border)

            g2D.drawRect(
                config.positions[i + offset].x - config.border,
                config.positions[i + offset].y - config.border  - config.captureRegionWidth - config.section - config.border * 2,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )
        }

    }

    private fun drawCaptureRegions(graphics: Graphics) {
        val captureRegion = ImageProcessorUtils.createMonotonousImage(
            config.captureRegionWidth,
            config.captureRegionHeight,
            BufferedImage.TYPE_3BYTE_BGR,
            Color.GREEN
        )

        for (i in config.positions.indices) {
            graphics.drawImage(captureRegion, config.positions[i].x, config.positions[i].y, this)

            val regionNumStr = i.toString()
            graphics.drawString(
                regionNumStr,
                config.positions[i].x + config.captureRegionWidth / 2 - graphics.fontMetrics.stringWidth(regionNumStr) / 2,
                config.positions[i].y + config.captureRegionHeight / 2 + verticalFontAlignCorrection
            )
        }
    }

    private fun drawCaptureRegionsAvgColors(graphics: Graphics) {
        val colors = captureScreen.getRegionsCaptureColors(captureScreen.capture(), config)

        for (i in config.positions.indices) {
            graphics.drawImage(
                ImageProcessorUtils.createMonotonousImage(
                    config.captureRegionWidth,
                    config.captureRegionHeight,
                    BufferedImage.TYPE_3BYTE_BGR,
                    colors[i]
                ),
                config.positions[i].x,
                config.positions[i].y,
                this
            )

            val regionNumStr = i.toString()
            graphics.drawString(
                regionNumStr,
                config.positions[i].x + config.captureRegionWidth / 2 - graphics.fontMetrics.stringWidth(regionNumStr) / 2,
                config.positions[i].y + config.captureRegionHeight / 2 + verticalFontAlignCorrection
            )

        }
    }

}