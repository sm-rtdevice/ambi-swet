package com.svet.capture

import com.svet.config.CaptureConfig
import com.svet.utils.Utils
import jssc.SerialPort
import jssc.SerialPortException
import jssc.SerialPortList
import java.io.UnsupportedEncodingException
import mu.KotlinLogging
import java.awt.Color

private val logger = KotlinLogging.logger {}

class Svet {

    private var portNumber: String = "COM1"
    private lateinit var serialPort: SerialPort

    @Volatile private var doWork = true
    private val buffer = ArrayList<Byte>()
    private var detectPorts: Boolean = false
    private var arduinoRebootTimeout = 0L

    private lateinit var config: CaptureConfig

    private val captureScreen = CaptureScreen()

    private var fpsTime = System.currentTimeMillis()

    private fun loadConfigs() {
        logger.info("Loading configuration...")
        // TODO: read configs from config file...

        portNumber = "COM3"
        detectPorts = true
        arduinoRebootTimeout = 1500

        config = CaptureConfig()
        config.init()

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
        var fault = false

        try {
            serialPort.openPort()
            serialPort.setParams(
                SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
            )
        } catch (ex: UninitializedPropertyAccessException){
            logger.error("Uninitialized property exception during connect to COM-$portNumber error", ex)
            fault = true
        } catch (ex: SerialPortException) {
            logger.error("Serial port exception during connect to COM-$portNumber error", ex)
            fault = true
        } catch (ex: UnsupportedEncodingException) {
            logger.error("Unsupported encoding exception during connect to COM-$portNumber error", ex)
            fault = true
        }

        doWork = if (!fault) {
            Thread.sleep(arduinoRebootTimeout) // arduino reboot timeout
            logger.info("Connect to COM-$portNumber success")
            true
        } else {
            false
        }

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

    fun showingBuffer() {
        // TODO: work in thread
        while (doWork) {
            serialPort.writeBytes(buffer.toByteArray())
            //Thread.sleep(1L)
        }
    }

    fun showRandomScene() {
        // TODO: work in thread
        while (doWork) {
            serialPort.writeBytes(Utils.preparerRandomBuffer(config).toByteArray())
            //Thread.sleep(10L)
        }
    }

    fun showScene() {
        // TODO: work in thread
        while (doWork) {
            serialPort.writeBytes(
                captureScreen.updateAdaBuffer(
                    captureScreen.getRegionsCaptureColors(captureScreen.capture(), config),
                    config
                ).toByteArray()
            )

            print("\rFPS: ${1000L / (System.currentTimeMillis() - fpsTime)}")
            fpsTime = System.currentTimeMillis()
        }
    }

    fun showSolidColor(color: Color) {
        serialPort.writeBytes(
            captureScreen.updateAdaBuffer(color, config).toByteArray()
        )
    }

}