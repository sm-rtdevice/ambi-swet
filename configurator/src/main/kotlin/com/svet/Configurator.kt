package com.svet

import com.svet.frame.CaptureConfigFrame
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

//fun main(args: Array<String>) {
//    log.info {
//        if (args.isNotEmpty()) { "Ambient svet configurator started with arguments: ${args.joinToString()}" }
//        else { "Ambient svet configurator started" }
//    }
//
//    CaptureConfigFrame()
//}

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

fun main() {
    val singleton1 = MyClass.factory.getInstance()
    val singleton2 = MyClass.factory.getInstance()

    println(singleton1 == singleton2) // true, оба экземпляра одинаковые
    singleton1.doSomething()
}
