package com.svet

import com.svet.capture.Svet
import com.svet.enums.ProgramMode
import com.svet.enums.ProgramMode.CAPTURE
import com.svet.enums.ProgramMode.RANDOM_SCENE
import com.svet.enums.ProgramMode.SET_SOLID_COLOR
import com.svet.enums.ProgramMode.SET_STARTUP_MODE
import com.svet.enums.ProgramMode.TEST_MODE
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Color
import kotlin.system.exitProcess

private val log = KotlinLogging.logger {}

suspend fun main(args: Array<String>) {
    log.info {
        if (args.isNotEmpty()) { "Ambient svet capture started with arguments: ${args.joinToString()}" }
        else { "Ambient svet capture started" }
    }

    val svet = Svet()
    svet.init()
    svet.connect()

    val mode = ProgramMode.from(parseOptional(args, 0))
//    val mode = TEST_MODE
    when (mode) {
        CAPTURE -> svet.launchCapture()
        RANDOM_SCENE -> svet.showRandomScene()
        SET_SOLID_COLOR -> { // запомнить цвет подсветки, for example program arguments: 2 128 92 30 1
            val r = parse(args, 1)
            val g = parse(args, 2)
            val b = parse(args, 3)
            val save = parse(args, 4)
            if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
                log.warn { "Invalid RGB params" }
                exitProcess(1)
            }
            svet.showSolidColor(Color(r, g, b), save == 1)
            log.info { "Set solid color is done" }
            exitProcess(0)
        }
        SET_STARTUP_MODE -> { // включение монотонной подсветки при старте, for example program arguments: 3 1
            val on = parse(args, 1)
            if (on == 0 || on == 1) {
                svet.setStartupMode(on.toByte())
                log.info { "Set startup mode '$on' is done" }
            } else {
                log.warn { "Unknown startup mode '$on' not set. Available: 0 (off) or 1 (on)" }
            }
            exitProcess(0)
        }
        TEST_MODE -> {
            svet.gradient()
        }
    }

    val exitCmd = readlnOrNull()

    svet.disconnect()
    log.info { "End program with command: $exitCmd" }
}

fun parseOptional(args: Array<String>, index: Int): Int? {
    return if (args.size > index) args[index].toIntOrNull()
    else null
}

fun parse(args: Array<String>, index: Int): Int {
    val param = parseOptional(args, index)
    if (param == null) {
        log.warn { "Missing argument by index $index" }
        exitProcess(1)
    }
    return param
}
