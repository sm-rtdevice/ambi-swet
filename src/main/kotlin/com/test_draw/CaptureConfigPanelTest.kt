package com.test_draw

import com.configs.CaptureConfig
import com.utils.ImageProcessorUtils
import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.JPanel


class CaptureConfigPanelTest internal constructor() : JPanel() {

    private val config: CaptureConfig
    private val verticalFontAlignCorrection = 3

    init {
        this.preferredSize = Dimension(640, 480)

        config = CaptureConfig()
        config.init()
    }

    override fun paint(graphics: Graphics) {
        val g2D = graphics as Graphics2D
        g2D.paint = Color.RED
        //g2D.stroke = BasicStroke(1F)
        g2D.drawLine(config.width / 2, 0, config.width / 2, config.height)
        g2D.drawLine(0, config.height / 2, config.width, config.height / 2)

        g2D.paint = Color.WHITE // BLACK

        captureRegionsColor(g2D)

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

    private fun captureRegionsColor(g2D: Graphics2D) {

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
            val y = config.positions[i+offset].y + config.captureRegionHeight/2/*+ config.captureRegionHeight*/ /*config.height - config.captureRegionHeight*/
            g2D.drawLine(x, y, x + config.section, y )

            g2D.drawRect(
                config.positions[i+offset].x - config.border + config.captureRegionWidth + config.section + config.border * 2,
                config.positions[i+offset].y - config.border,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )
        }

        // области захвата свеоху
        offset += config.leftCount
        for (i in 0 until config.topCount) {

            val x = config.positions[i+offset].x + config.captureRegionWidth / 2
            val y = config.captureRegionHeight
            g2D.drawLine(x, y + config.border, x, y + config.section + config.border)

            g2D.drawRect(
                config.positions[i+offset].x - config.border /*+ config.captureRegionWidth + config.section + config.border * 2*/,
                config.positions[i+offset].y - config.border + config.captureRegionHeight + config.section + config.border * 2,
                config.captureRegionWidth + config.border,
                config.captureRegionHeight + config.border
            )
        }

        // области захвата справа
//TODO:

        // области захвата снизу справа
//TODO:

    }

}