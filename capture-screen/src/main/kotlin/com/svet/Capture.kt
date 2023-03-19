package com.svet

import com.svet.capture.Svet
import java.awt.Color

suspend fun main(args: Array<String>) {
    println("Program ambient-svet-capture was started")
    println("Program arguments: ${args.joinToString()}")

    val svet = Svet()
    svet.init()
    svet.connect()

    svet.launchCapture()
//    svet.showRandomScene()
//    svet.showSolidColor(Color(255,255,0))

    val exitCmd = readLine()

    svet.disconnect()

    println("End program with command: $exitCmd")
}
