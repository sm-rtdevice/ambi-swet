package com.svet

import com.svet.capture.Svet

fun main(args: Array<String>) {
    println("Program ambient-svet-capture was started")
    println("Program arguments: ${args.joinToString()}")

    val svet = Svet()
    svet.init()
    svet.connect()
    svet.showScene()

    svet.disconnect()

    println("End program")
}