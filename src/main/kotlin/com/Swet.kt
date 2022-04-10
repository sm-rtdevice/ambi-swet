package com

import jssc.SerialPort
import jssc.SerialPortException
import jssc.SerialPortList
import java.io.UnsupportedEncodingException
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Swet {

    private var portNumber: String = "COM1"
    private lateinit var serialPort: SerialPort
    private var numLeds: Int = 0

    @Volatile private var doWork = true
    private val buffer = ArrayList<Byte>()
    private var detectPorts: Boolean = false
    private var arduinoRebootTimeout = 0L

    private fun loadConfigs() {
        logger.info("Loading configuration...")
        // TODO: read configs from config file...
        numLeds = 94
        //portNumber = "COM4"
        detectPorts = true
        arduinoRebootTimeout = 1500

        logger.info("Loading configuration done")
    }

    fun init() {
        logger.info("Start initialization...")

        loadConfigs()

        if (detectPorts) {
            val portNames = SerialPortList.getPortNames()
            if (portNames.isNotEmpty()) {
                for (portName in portNames) {
                    logger.info("Available com port: $portName")
                }
            } else {
                logger.info("No available com ports found")
                return
            }
        }

        serialPort = SerialPort(portNumber)

        logger.info("Initialization done")
    }

    fun connect() {
        logger.info("Connect to COM-$portNumber")

        try {
            serialPort.openPort()
            serialPort.setParams(
                SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
            )
        } catch (ex: SerialPortException) {
            logger.error("Serial port exception during connect to COM-$portNumber error", ex)
        } catch (ex: UnsupportedEncodingException) {
            logger.error("Unsupported encoding exception during connect to COM-$portNumber error", ex)
        }

        Thread.sleep(arduinoRebootTimeout) // arduino reboot timeout

        doWork = true

        logger.info("Connect to COM-$portNumber success")
    }

    fun disconnect() {
        logger.info("Disconnect from COM-$portNumber")

        doWork = false
        try {
            serialPort.closePort()
        } catch (ex: SerialPortException) {
            logger.error("Disconnect from COM-$portNumber error", ex)
        }

        logger.info("Disconnect from COM-$portNumber success")
    }

    fun reconnect() {
        logger.info("Reconnect to COM-$portNumber")
        disconnect()
        connect()
    }

    fun show() {
        serialPort.writeBytes(buffer.toByteArray())
    }

    fun showScene() {
        // TODO: work in thread
        while (doWork) {
            serialPort.writeBytes(buffer.toByteArray())
            //Thread.sleep(1L)
        }
    }

    fun showRandomScene() {
        // TODO: work in thread
        while (doWork) {
            preparerRandomBuffer()
            serialPort.writeBytes(buffer.toByteArray())
            //Thread.sleep(10L)
        }
    }

    fun prepareBuffer() {
        val hi: Byte = 0
        val lo: Byte = 0
        val chk: Byte = 0x55

        buffer.clear()
        buffer.addAll(listOf(hi, lo, chk)) // CRC?

        buffer.addAll(listOf(0x7F, 0x00, 0x00)) // 1 R
        buffer.addAll(listOf(0x00, 0x7F, 0x00)) // 2 G
        buffer.addAll(listOf(0x00, 0x00, 0x7F)) // 3 B
        buffer.addAll(listOf(-0x7F, 0x00, 0x00)) // 4 R
        buffer.addAll(listOf(0x00, -0x7F, 0x00)) // 5 G
        buffer.addAll(listOf(0x00, 0x00, 0)) // 6 B
        for (i in 1..numLeds-6) {
            buffer.addAll(listOf(0, 10, 10)) // R, G, B [-0x7F .. 0x7F]
        }
    }

    fun prepareBufferForAdaSketch() {
        val hi: Byte = 0
        val lo: Byte = 0
        val chk: Byte = 0x55

        buffer.clear()
        buffer.addAll(listOf('A'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte())) // заголовок
        buffer.addAll(listOf(hi, lo, chk)) // CRC?

        for (i in 1..numLeds) {
            buffer.addAll(listOf(1, -128, 0)) // R, G, B
        }
    }

    fun preparerRandomBuffer() {
        val bright: Byte = 100 // 1..127: 1 - max яркость, 127 - min яркость

        val hi: Byte = 0
        val lo: Byte = 0
        val chk: Byte = 0x55

        buffer.clear()
        buffer.addAll(listOf('A'.code.toByte(), 'd'.code.toByte(), 'a'.code.toByte())) // заголовок
        buffer.addAll(listOf(hi, lo, chk)) // CRC?

        for (i in 1..numLeds) {
            buffer.addAll(listOf(
//            (-128..127).random().toByte(),  // R
//            (-128..127).random().toByte(),  // G
//            (-128..127).random().toByte())) // B
            (0..127/bright).random().toByte(),  // R
            (0..127/bright).random().toByte(),  // G
            (0..127/bright).random().toByte())) // B
        }
    }
}