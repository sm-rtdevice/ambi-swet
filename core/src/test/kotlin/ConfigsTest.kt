import com.svet.config.CaptureConfig
import com.svet.config.CaptureConfigSingleTone
import com.svet.config.CaptureConfigSingleTone2
import com.svet.config.ConfigHelper
import com.svet.config.Singleton
import com.svet.config.SvetConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ConfigsTest {

    @Test
    fun loadConfigTest() {
        val svetConfig = SvetConfig()
        Assertions.assertNotNull(svetConfig.captureConfig)
        Assertions.assertNotNull(svetConfig.connectConfig)

//        ConfigHelper.saveConfig("capture-config.json", svetConfig.captureConfig)
//        ConfigHelper.saveConfig("connect-config.json", svetConfig.connectConfig)
    }

    @Test
    fun loadByDefaultTest() {
        val loaded: CaptureConfig? =
            ConfigHelper.load("config/capture-config.json", CaptureConfig::class.java)

        val config = loaded ?: CaptureConfig.defaultConfig()

        Assertions.assertNotNull(config)
    }

    @Test
    fun loadSingletonConfigTest() {
//        val stringSingleton = Singleton.getData<CaptureConfigSingleTone>()
        val stringSingleton1 = Singleton.getInstance<CaptureConfigSingleTone>()
        val stringSingleton2 = Singleton.getInstance<CaptureConfigSingleTone>()
        val v1 = stringSingleton1.load()
        val stringSingleton3 = Singleton.getInstance<CaptureConfigSingleTone2>()
        val v2 = stringSingleton3.load()
//        print(stringSingleton.data)
    }

    class SingletonFactory<T : Any>(private val creator: () -> T) {
        @Volatile
        private var instance: T? = null

        fun getInstance(): T {
            return instance ?: synchronized(this) {
                instance ?: creator().also { instance = it }
            }
        }
    }

    // Пример использования
    class MyClass private constructor() {
        companion object {
            val factory = SingletonFactory { MyClass() }
        }

        fun doSomething() {
            println("Doing something in MyClass")
        }
    }

    // Пример использования
    class MyClass2 private constructor() {
        companion object {
            val factory = SingletonFactory { MyClass2() }
//            val inst = factory.getInstance()
        }

        fun doSomething() {
            println("Doing something in MyClass")
        }
    }

    @Test
    fun loadSingletonFactoryTest() {
        val singleton1 = MyClass.factory.getInstance()
        val singleton2 = MyClass.factory.getInstance()
//        val singleton3 = MyClass2.inst
        val singleton4 = MyClass2.factory.getInstance()

        println(singleton1 == singleton2) // true, оба экземпляра одинаковые
    }

}
