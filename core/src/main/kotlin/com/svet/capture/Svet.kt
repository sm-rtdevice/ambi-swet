package com.svet.capture

import com.svet.config.SvetConfig
import com.svet.processor.ImageProcessorUtils
import com.svet.utils.Utils
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Color
import java.io.UnsupportedEncodingException
import jssc.SerialPort
import jssc.SerialPortException
import jssc.SerialPortList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

class Svet {
    private val svetConfig = SvetConfig()
    private var serialPort: SerialPort? = null
    private val captureScreen = CaptureScreen()
    private var job: Job = Job()
    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile private var doWork = true

    private fun loadConfigs() {
        log.debug { "Loading configuration..." }
        // test configuration
        svetConfig.connectConfig.portNumber = "COM4"
        svetConfig.connectConfig.detectPorts = true
        log.debug { "Loading configuration done" }
    }

    fun init() {
        log.info { "Start initialization..." }

        loadConfigs()

        if (svetConfig.connectConfig.detectPorts) {
            val portNames = SerialPortList.getPortNames()
            if (portNames.isNotEmpty()) {
                for (portName in portNames) {
                    log.info { "Available com port: $portName" }
                }
            } else {
                log.info { "No available com ports found" }
                return
            }
        }

        serialPort = SerialPort(svetConfig.connectConfig.portNumber)

        log.info { "Initialization done" }
    }

    fun connect() {
        if (serialPort == null) {
            log.warn { "Port ${svetConfig.connectConfig.portNumber} is not available" }
            doWork = false
            return
        }

        log.info { "Connect to port ${svetConfig.connectConfig.portNumber}" }

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
        } catch (ex: UninitializedPropertyAccessException) {
            log.error(ex) { "Uninitialized property exception during connect to port ${svetConfig.connectConfig.portNumber}" }
        } catch (ex: SerialPortException) {
            log.error(ex) { "Serial port exception during connect to port ${svetConfig.connectConfig.portNumber}" }
        } catch (ex: UnsupportedEncodingException) {
            log.error(ex) { "Unsupported encoding exception during connect to port ${svetConfig.connectConfig.portNumber}" }
        } catch (ex: Exception) {
            log.error(ex) { "Connect to port ${svetConfig.connectConfig.portNumber}" }
        }

        doWork = if (!fault) {
            Thread.sleep(svetConfig.connectConfig.arduinoRebootTimeout) // arduino reboot timeout
            log.info { "Connect to port ${svetConfig.connectConfig.portNumber} success" }
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

        log.info { "Disconnect from port ${svetConfig.connectConfig.portNumber}" }
        try {
            serialPort?.closePort()
        } catch (ex: SerialPortException) {
            log.error(ex) { "Disconnect from port ${svetConfig.connectConfig.portNumber}" }
            return
        }

        log.info { "Disconnect from port ${svetConfig.connectConfig.portNumber} success" }
    }

    suspend fun reconnect() {
        log.info { "Reconnect to port ${svetConfig.connectConfig.portNumber}" }
        disconnect()
        connect()
    }

    fun showRandomScene() {
        if (serialPort == null) {
            log.warn { "COM port is not available, random scene is not Launched" }
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
            log.info { "Random scene stopped" }
        }

        log.info { "Random scene Launched" }
    }

    fun showSolidColor(color: Color) {
        if (serialPort == null) {
            log.warn { "COM port is not available, solid color is not installed" }
            return
        }

        serialPort?.writeBytes(
            captureScreen.updateAdaBuffer(color, svetConfig.captureConfig).toByteArray()
        )
    }

    fun showSolidColor(color: Color, save: Boolean) {
        if (serialPort == null) {
            log.warn { "COM port is not available, solid color is not installed" }
            return
        }

        serialPort?.writeBytes(
            Utils.showSolidColorCmd(color, save).toByteArray()
        )
    }

    fun setStartupMode(mode: Byte) {
        if (serialPort == null) {
            log.warn { "COM port is not available, startup mode is not installed" }
            return
        }

        serialPort?.writeBytes(
            Utils.setStartupModeCmd(mode).toByteArray()
        )
    }

    suspend fun launchCapture() {
        if (serialPort == null) {
            log.warn { "COM port is not available, capture is not Launched" }
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
            log.info { "Capture stopped" }
        }

        log.info { "Capture Launched" }
    }

    suspend fun blink() {
        if (serialPort == null) {
            log.warn { "COM port is not available, blink is not Launched" }
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
            log.info { "blink stopped" }
        }

        log.info { "blink Launched" }
    }

    /**
     * Градиент перехода между двумя цветами с заданной дискретностью.
     **/
    suspend fun gradient() {
        if (serialPort == null) {
            log.warn { "COM port is not available, gradient is not Launched" }
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
            log.info { "gradient stopped" }
        }

        log.info { "gradient Launched" }
    }

}
