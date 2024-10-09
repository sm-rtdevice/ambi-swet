package com.svet.capture

import com.svet.config.SvetConfig
import com.svet.processor.ImageProcessorUtils
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
    private val svetConfig = SvetConfig()
    private var serialPort: SerialPort? = null
    private val captureScreen = CaptureScreen()
    private var job: Job = Job()
    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile private var doWork = true

    private fun loadConfigs() {
        logger.info("Loading configuration...")
        // test configuration
        svetConfig.connectConfig.portNumber = "COM4"
        svetConfig.connectConfig.detectPorts = true
        logger.info("Loading configuration done")
    }

    fun init() {
        logger.info("Start initialization...")

        loadConfigs()

        if (svetConfig.connectConfig.detectPorts) {
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

        serialPort = SerialPort(svetConfig.connectConfig.portNumber)

        logger.info("Initialization done")
    }

    fun connect() {
        if (serialPort == null) {
            logger.warn("Port ${svetConfig.connectConfig.portNumber} is not available")
            doWork = false
            return
        }

        logger.info("Connect to port ${svetConfig.connectConfig.portNumber}")

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
            logger.error("Uninitialized property exception during connect to port ${svetConfig.connectConfig.portNumber} error", ex)
        } catch (ex: SerialPortException) {
            logger.error("Serial port exception during connect to port ${svetConfig.connectConfig.portNumber} error", ex)
        } catch (ex: UnsupportedEncodingException) {
            logger.error("Unsupported encoding exception during connect to port ${svetConfig.connectConfig.portNumber} error", ex)
        } catch (ex: Exception) {
            logger.error("Connect to port ${svetConfig.connectConfig.portNumber} error", ex)
        }

        doWork = if (!fault) {
            Thread.sleep(svetConfig.connectConfig.arduinoRebootTimeout) // arduino reboot timeout
            logger.info("Connect to port ${svetConfig.connectConfig.portNumber} success")
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

        logger.info("Disconnect from port ${svetConfig.connectConfig.portNumber}")
        try {
            serialPort?.closePort()
        } catch (ex: SerialPortException) {
            logger.error("Disconnect from port ${svetConfig.connectConfig.portNumber} error", ex)
            return
        }

        logger.info("Disconnect from port ${svetConfig.connectConfig.portNumber} success")
    }

    suspend fun reconnect() {
        logger.info("Reconnect to port ${svetConfig.connectConfig.portNumber}")
        disconnect()
        connect()
    }

    fun showRandomScene() {
        if (serialPort == null) {
            logger.info("COM port is not available, random scene is not Launched")
            return
        }

        job = scope.launch {
            while (doWork) {
                val elapsedTime = measureTimeMillis {
                    serialPort?.writeBytes(Utils.preparerRandomBuffer(svetConfig.captureConfig).toByteArray())
                    delay(100)
                }
                print("\rFPS: ${1000L / (elapsedTime + 1)}; elapsed: $elapsedTime ms")
            }
            logger.info("Random scene stopped")
        }

        logger.info("Random scene Launched")
    }

    fun showSolidColor(color: Color) {
        if (serialPort == null) {
            logger.info("COM port is not available, solid color is not installed")
            return
        }

        serialPort?.writeBytes(
            captureScreen.updateAdaBuffer(color, svetConfig.captureConfig).toByteArray()
        )
    }

    fun showSolidColor(color: Color, save: Boolean) {
        if (serialPort == null) {
            logger.info("COM port is not available, solid color is not installed")
            return
        }

        serialPort?.writeBytes(
            Utils.showSolidColorCmd(color, save).toByteArray()
        )
    }

    fun setStartupMode(mode: Byte) {
        if (serialPort == null) {
            logger.info("COM port is not available, startup mode is not installed")
            return
        }

        serialPort?.writeBytes(
            Utils.setStartupModeCmd(mode).toByteArray()
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
                            captureScreen.getRegionsCaptureColors(captureScreen.capture(), svetConfig.captureConfig),
                            svetConfig.captureConfig
                        ).toByteArray()
                    )

                    delay(1)
                }
                print("\rFPS: ${1000L / (elapsedTime + 1)}; elapsed: $elapsedTime ms")
            }
            logger.info("Capture stopped")
        }

        logger.info("Capture Launched")
    }

    suspend fun blink() {
        if (serialPort == null) {
            logger.info("COM port is not available, blink is not Launched")
            return
        }

        var fon = Color(0, 0, 0)
        var i = 0

        job = scope.launch {
            while (doWork) {
                val elapsedTime = measureTimeMillis {
                    serialPort?.writeBytes(Utils.showSolidColorCmd(fon, false).toByteArray())

//                    logger.info("color: $i")
                    fon = Color(i, i, i)
                    i = (i + 5) % 250

                    delay(1)
                }
                print("\rFPS: ${1000L / (elapsedTime + 1)}; elapsed: $elapsedTime ms")
            }
            logger.info("blink stopped")
        }

        logger.info("blink Launched")
    }

    /**
     * Градиент перехода между двумя цветами с заданной дискретностью.
     **/
    suspend fun gradient() {
        if (serialPort == null) {
            logger.info("COM port is not available, gradient is not Launched")
            return
        }

        val startColor = Color(0, 0, 255) // Начальный цвет (красный)
        val endColor = Color(255, 0, 0) // Конечный цвет (синий)

        val steps = 100 // Дискретность градиента

        val gradient = ImageProcessorUtils.calculateGradient(startColor, endColor, steps)
        var i = 0
        var vector = 1

        job = scope.launch {
            while (doWork) {
                val elapsedTime = measureTimeMillis {
//                    logger.info("color: $i")
                    serialPort?.writeBytes(Utils.showSolidColorCmd(gradient[i], false).toByteArray())
//                    // в одном направлении
//                    i = (i + vector) % (steps + 1)

                    // в обоих направлениях
                    i += vector
                    if (i >= steps || i <= 0) { vector *= -1 }

                    delay(10)
                }
                print("\rFPS: ${1000L / (elapsedTime + 1)}; elapsed: $elapsedTime ms")
            }
            logger.info("gradient stopped")
        }

        logger.info("gradient Launched")
    }

}
