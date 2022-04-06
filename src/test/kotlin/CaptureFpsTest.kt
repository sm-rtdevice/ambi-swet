import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage

private val logger = KotlinLogging.logger {}

class CaptureTest {

    private val screenRect = Rectangle(Toolkit.getDefaultToolkit().screenSize) // 1920*1800
    private lateinit var capture: BufferedImage

    private var startTime = 0L
    private var fps = 0L

    private var numLeds: Int = 94

    @Test
    fun captureFullHdFpsTest() { // fps: 30
        while (true) {
            capture = Robot().createScreenCapture(screenRect) // 1920*1800
            val color = Color(capture.getRGB(100, 100))

            ++fps
            if (System.currentTimeMillis() - startTime > 1000) {
                startTime = System.currentTimeMillis()
                println("$color; fps: $fps")
                fps = 0
            }
        }
    }

    @Test
    fun capture4KFpsTest() { // fps: 10
        while (true) {
            capture = Robot().createScreenCapture(Rectangle(0, 0, 3840, 2160))
            val color = Color(capture.getRGB(100, 100))

            ++fps
            if (System.currentTimeMillis() - startTime > 1000) {
                startTime = System.currentTimeMillis()
                println("$color; fps: $fps")
                fps = 0
            }
        }
    }

    @Test
    fun multiCaptureFpsTest() { // fps: 1
        while (true) {
            lateinit var color: Color

            for (i in 1..numLeds) {
                capture = Robot().createScreenCapture(Rectangle(0, 0, 100, 100))
                color = Color(capture.getRGB(5, 5))
            }

            ++fps
            if (System.currentTimeMillis() - startTime > 1000) {
                startTime = System.currentTimeMillis()
                println("$color; fps: $fps")
                fps = 0
            }
        }
    }

}