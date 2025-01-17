package com.svet.config

import com.sun.org.apache.bcel.internal.generic.RETURN

class CaptureConfigSingleTone: Configuration<String> {

    var cfg: String? = "default"

    override fun load(): String {
        return "CaptureConfigSingleTone"
    }

    override fun save() {
        print("test saved")
        return
    }
}
