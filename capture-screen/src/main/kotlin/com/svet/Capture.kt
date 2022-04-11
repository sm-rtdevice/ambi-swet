package com.svet

import com.svet.capture.Svet

fun main(args: Array<String>) {
    println("Program ambient-svet-capture was started")
    println("Program arguments: ${args.joinToString()}")

    val svet = Svet()
    svet.init()
    svet.connect()

//    svet.prepareBufferForAdaSketch()
//    svet.prepareBuffer()
//    svet.preparerRandomBuffer()
//    svet.show()
//    svet.showScene()
//    for (i in 1..12) {
//        svet.show()
//    }

    svet.showRandomScene()

    svet.disconnect()

//    val captureImage = CaptureScreen()
//
//    while (true) {
//        captureImage.capture()
//    }

    println("End program")
}