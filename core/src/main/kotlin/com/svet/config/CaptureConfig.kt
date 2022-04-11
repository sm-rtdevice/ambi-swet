package com.svet.config

import com.svet.enums.OutputDirection
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CaptureConfig {

    val ledsCount = 94
    val initialCapacity = 94 * 3 + 3 + 3
    lateinit var outputDirection: OutputDirection

    // device - устройство захвата
    // Rectangle - размеры экрана
    val width: Int = 1920
    val height: Int = 1080

    // высота и ширина одласти захвата для получения усредненного цвета
    var captureRegionWidth: Int = 50
    var captureRegionHeight: Int = 50

    // количество зон:
    // с верху 32
    // по вертиккали 20
    // вырез снизу 6

    // всего LED: 94
    // numOfLeds = topCount + leftCount + rightCount + bottomCountR + bottomCountL
    val topCount: Int = 32
    val leftCount: Int = 18
    val rightCount: Int = 18
    val bottomCountR: Int = 13
    val bottomCountL: Int = 13

    val bottomCutout: Int = 200

    // координаты областей захвата
    val positions: MutableList<Position> = ArrayList(ledsCount) // 94 num of LED's

    // длина отрезка выносной линии
    val section: Int = 20

    // ширина рамки
    val border: Int = 1

    private fun load():Boolean {
//      TODO: загрузить координаты из файла конфигурации return true
        return false
    }

    fun defaultConfig() {
        outputDirection = OutputDirection.CLOCKWISE

        val hArea = width / 2 - bottomCutout / 2

        // области захвата снизу слева
        var horizontalOffset = 0
        for (i in 0 until bottomCountL) {
            val segment = hArea / bottomCountL
            val x = width/2 /* - borderV*/ - segment - bottomCutout/2 + (segment/2 - captureRegionWidth/2 - horizontalOffset) // <-- отступ справа
            horizontalOffset += segment
            val y = height - captureRegionHeight
            positions.add(Position(x, y))
        }

        // области захвата слева
        var vArea = (height - captureRegionHeight * 2) /* - borderH )*/ / leftCount
//        var verticalOffset = captureRegionHeight
//        for (i in 0 until leftCount) {
//            val y = verticalOffset + (vArea / 2 - captureRegionHeight / 2)
//            verticalOffset += vArea
//            //val x = width - captureRegionWidth /* - borderV*/ // <-- отступ справа
//            positions.add(Position(0, y))
//        }
        var verticalOffset = vArea * leftCount
        for (i in 0 until leftCount) {
            val y = verticalOffset + (vArea / 2 - captureRegionHeight / 2)
            verticalOffset -= vArea
            positions.add(Position(0, y))
        }

        // области захвата сверху
        horizontalOffset = 0
        for (i in 0 until topCount) {
            val x = width / topCount / 2 - captureRegionWidth / 2 + horizontalOffset
            horizontalOffset += width / topCount
            positions.add(Position(x, 0)) // y + borderH
        }

        // области захвата справа
        vArea = (height - captureRegionHeight * 2) /* - borderH )*/ / rightCount
        verticalOffset = vArea
        for (i in 0 until rightCount) {
            val y = verticalOffset + (vArea / 2 - captureRegionHeight / 2)
            verticalOffset += vArea
            val x = width - captureRegionWidth /* - borderV*/ // <-- отступ справа
            positions.add(Position(x, y))
        }

        // области захвата снизу справа
        horizontalOffset = 0
        for (i in 0 until bottomCountR) {
            val segment = hArea / bottomCountR
            val x = width /* - borderV*/ - segment + (segment/2 - captureRegionWidth/2 - horizontalOffset) // <-- отступ справа
            horizontalOffset += segment
            val y = height - captureRegionHeight
            positions.add(Position(x, y))
        }
    }

    fun init() {
        if (!load()) {
            logger.info("Create default configuration")
            defaultConfig()
        }

        if (positions.isEmpty()) {
            logger.warn("Capture regions configuration was not loaded")
        }
    }

    private fun save() {}

    data class Position(
        var x: Int,
        var y: Int
    )

}