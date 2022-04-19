package com.svet.capture

import com.svet.config.CaptureConfig
import com.svet.utils.Utils
import jssc.SerialPort
import jssc.SerialPortException
import jssc.SerialPortList
import mu.KotlinLogging
import java.awt.Color
import kotlinx.coroutines.*
import java.io.UnsupportedEncodingException
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class Svet {

    private var portNumber: String = "COM1"
    private var serialPort: SerialPort? = null

    @Volatile private var doWork = true
    private var detectPorts: Boolean = false
    private var arduinoRebootTimeout = 0L

    private lateinit var config: CaptureConfig

    private val captureScreen = CaptureScreen()

    private var fpsTime = System.currentTimeMillis()

    private var job: Job = Job()
    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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
        if (serialPort == null) {
            logger.warn("Port $portNumber is not available")
            doWork = false
            return
        }

        logger.info("Connect to port $portNumber")

        var fault = true
        try {
            serialPort!!.openPort()
            serialPort!!.setParams(
                SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
            )
            fault = false
        } catch (ex: UninitializedPropertyAccessException){
            logger.error("Uninitialized property exception during connect to port $portNumber error", ex)
        } catch (ex: SerialPortException) {
            logger.error("Serial port exception during connect to port $portNumber error", ex)
        } catch (ex: UnsupportedEncodingException) {
            logger.error("Unsupported encoding exception during connect to port $portNumber error", ex)
        } catch (ex: Exception) {
            logger.error("Connect to port $portNumber error", ex)
        }

        doWork = if (!fault) {
            Thread.sleep(arduinoRebootTimeout) // arduino reboot timeout
            logger.info("Connect to port $portNumber success")
            true
        } else {
            false
        }

    }

    suspend fun disconnect() {
        doWork = false
        job.join()

        if (serialPort == null) {
            return
        }

        logger.info("Disconnect from port $portNumber")
        try {
            serialPort?.closePort()
        } catch (ex: SerialPortException) {
            logger.error("Disconnect from port $portNumber error", ex)
            return
        }

        logger.info("Disconnect from port $portNumber success")
    }

    suspend fun reconnect() {
        logger.info("Reconnect to port $portNumber")
        disconnect()
        connect()
    }

    fun showRandomScene() {
        while (doWork) {
            serialPort?.writeBytes(Utils.preparerRandomBuffer(config).toByteArray())
            Thread.sleep(1L)
        }
    }

    fun showScene() {
        while (doWork) {
            serialPort?.writeBytes(
                captureScreen.updateAdaBuffer(
                    captureScreen.getRegionsCaptureColors(captureScreen.capture(), config),
                    config
                ).toByteArray()
            )
            Thread.sleep(1L)

            print("\rFPS: ${1000L / (System.currentTimeMillis() - fpsTime)}")
            fpsTime = System.currentTimeMillis()
        }
    }

    fun showSolidColor(color: Color) {
        serialPort?.writeBytes(
            captureScreen.updateAdaBuffer(color, config).toByteArray()
        )
    }

    suspend fun launchCapture() {
        if (serialPort == null) {
            logger.info("COM port is not available, capture is not Launched")
            return
        }

        job = scope.launch {
            while (doWork) {
                val elapsedTime = measureTimeMillis {

                    serialPort?.writeBytes(
                        captureScreen.updateAdaBuffer(
                            captureScreen.getRegionsCaptureColors(captureScreen.capture(), config),
                            config
                        ).toByteArray()
                    )

                    delay(5000)
                }
                print("\rFPS: ${1000L / (elapsedTime + 1)}; elapsed: $elapsedTime ms")
            }
            logger.info("Capture stopped")
        }

        logger.info("Capture Launched")
    }

}