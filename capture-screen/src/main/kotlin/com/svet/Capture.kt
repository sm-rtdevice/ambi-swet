package com.svet

import com.svet.capture.Svet
import java.awt.Color

fun main(args: Array<String>) {
    println("Program ambient-svet-capture was started")
    println("Program arguments: ${args.joinToString()}")

    val svet = Svet()
    svet.init()
    svet.connect()

    svet.showScene()

//    svet.showSolidColor(Color(0,0,0))
//    Thread.sleep(1000)

    svet.disconnect()

    println("End program")
}