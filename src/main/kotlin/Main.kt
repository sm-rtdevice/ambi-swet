import com.Swet

fun main(args: Array<String>) {
    println("Program ambi-swet was started")
    println("Program arguments: ${args.joinToString()}")

    val swet = Swet()
    swet.init()
    swet.connect()

//    swet.prepareBufferForAdaSketch()
//    swet.prepareBuffer()
//    swet.preparerRandomBuffer()
//    swet.show()
//    swet.showScene()
//    for (i in 1..12) {
//        swet.show()
//    }

    swet.showRandomScene()

    swet.disconnect()
    println("End program")
}