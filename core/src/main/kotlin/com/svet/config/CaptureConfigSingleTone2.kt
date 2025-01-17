package com.svet.config

import com.sun.org.apache.bcel.internal.generic.RETURN


class CaptureConfigSingleTone2: Configuration<Int> {

    var cfg: Int? = 1

    override fun load(): Int {
        return 1
    }

    override fun save() {
        print("test saved")
        return
    }
}

//class CaptureConfigSingleTone2: Configuration<CaptureConfigSingleTone2> {
//
//    var data: String? = null
//
//    override fun load(): CaptureConfigSingleTone2 {
//        return this
//    }
//
//    override fun save() {
//        print("test saved")
//        return
//    }
//}
