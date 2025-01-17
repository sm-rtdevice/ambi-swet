package com.svet.config

//class Singleton<T> private constructor() {
//
//    var data: T? = null
//
//    companion object {
//        @Volatile
//        private var instance: Singleton<*>? = null
//
//        fun <T> getInstance(): Singleton<T> {
//            if (instance == null) {
//                synchronized(this) {
//                    if (instance == null) {
//                        instance = Singleton<T>()
//                    }
//                }
//            }
//
//            return instance!! as Singleton<T>
//        }
//    }
//}

//class Singleton<T> private constructor() {
//    var data: T? = null
//
////    init {
////        data = null
////    }
//
//    companion object {
//        @Volatile
//        private var instance: Singleton<Any>? = null
//
//        fun <T> getInstance(): Singleton<T> {
//            if (instance == null) {
//                synchronized(this) {
//                    if (instance == null) {
//                        instance = Singleton<Any>()
//                    }
//                }
//            }
//
//            @Suppress("UNCHECKED_CAST")
//            return instance as Singleton<T>
//        }
//    }
//}

//class Singleton<T : Configuration<*>> private constructor() {
//    var data: T? = null
//
////    init {
////        data = loadData()
////    }
//
////    private fun loadData(): T {
////        @Suppress("UNCHECKED_CAST")
////        return data.load() as T
////    }
//
//    companion object {
//        @Volatile
//        private var instance: Singleton<out Configuration<*>>? = null
//
//        fun <T : Configuration<*>> getInstance(): Singleton<T> {
//            if (instance == null) {
//                synchronized(this) {
//                    if (instance == null) {
//                        instance = Singleton<Configuration<Any>>()
//                    }
//                }
//            }
//
//            @Suppress("UNCHECKED_CAST")
//            return instance as Singleton<T>
//        }
//
//        fun <T : Configuration<*>> getData(): T {
//            return getInstance<T>().data!!
//        }
//    }
//}

// val instance = Singleton.getInstance()
//val stringSingleton = Singleton.getInstance<String>()

//ConfigFactory.get()<CaptureConfig>().data
//
//val singleton = Singleton.getInstance<CaptureConfig>().data // CaptureConfig inmplement Configuration interface
//println(singleton.data)

//val stringSingleton = Singleton.getData<CaptureConfigSingleTone>()

//
//class Singleton<T: Configuration<*>> private constructor() {
//
//    var data: Configuration<*>? = null
//
//    companion object {
//        @Volatile
//        private var instance: Singleton<Configuration<*>>? = null
//
//        fun <T : Configuration<*>> getInstance(): Singleton<T> {
//            if (instance == null) {
//                synchronized(this) {
//                    if (instance == null) {
//                        instance = Singleton<Configuration<*>>()
//                    }
//                }
//            }
//
//            var res = instance!! as Singleton<T>
//            res.data
//            return res
//
//        }
//    }
//}

//class Singleton<T : Configuration> private constructor() {
//    var data: T? = null
//
//    companion object {
//        @Volatile
//        private var instance: Singleton<Configuration>? = null
//
//        fun <T : Configuration> getInstance(): Singleton<T> {
//            return instance as? Singleton<T> ?: synchronized(this) {
//                instance as? Singleton<T> ?: Singleton<T>().also { instance = it }
//            }
//        }
//    }
//}

class Singleton<T> private constructor(): Configuration<T> {

    var data: T? = null

    override fun load(): T? {
        return data
    }

    override fun save() {
        TODO("Not yet implemented")
    }

    companion object {
        @Volatile
        private var instance: Singleton<*>? = null

        fun <T> getInstance(): Singleton<T> {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = Singleton<T>()
                    }
                }
            }

            return instance!! as Singleton<T>
        }
    }
}