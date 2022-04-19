package com.svet

import com.svet.capture.Svet

suspend fun main(args: Array<String>) {
    println("Program ambient-svet-capture was started")
    println("Program arguments: ${args.joinToString()}")

    val svet = Svet()
    svet.init()
    svet.connect()
    //svet.showScene()
    svet.launchCapture()

//    svet.showSolidColor(Color(0,0,0))
//    Thread.sleep(1000)

    val exitCmd = readLine()

    svet.disconnect()

    println("End program with command: $exitCmd")
}