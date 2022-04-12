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
    private val timer: Timer = Timer(100, this)

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
        val g2D = graphics as Graphics2D

        g2D.paint = Color.RED

        drawGridLines(g2D)

        g2D.paint = Color.BLACK

        drawCaptureRegionsColor(g2D)

//        drawCaptureRegions(graphics)

        drawCaptureRegionsAvgColors(graphics)
    }

    private fun drawGridLines(g2D: Graphics2D) {
        //g2D.stroke = BasicStroke(1F)
        g2D.drawLine(config.width / 2, 0, config.width / 2, config.height)
        g2D.drawLine(0, config.height / 2, config.width, config.height / 2)
    }

    private fun drawCaptureRegionsColor(g2D: Graphics2D) {

        g2D.stroke = BasicStroke(config.border.toFloat())

        // области захвата снизу слева
        var offset = 0
        for (i in 0 until config.bottomCountL) {

            val x = config.positions[i].x + config.captureRegionWidth / 2
            val y = config.height - config.captureRegionHeight
            g2D.drawLine(x, y - config.border, x, y - config.section - config.border)

            g2D.drawRect(
                config.positions[i].x - config.border,
                config.positions[i].y - config.captureRegionHeight - config.section - config.border * 2,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
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