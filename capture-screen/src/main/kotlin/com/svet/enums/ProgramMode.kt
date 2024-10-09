package com.svet.enums

enum class ProgramMode(val mode: Int) {
    CAPTURE(0), // default mode
    RANDOM_SCENE(1),
    SET_SOLID_COLOR(2),
    SET_STARTUP_MODE(3),
    TEST_MODE(999);

    companion object {
        infix fun from(value: Int?): ProgramMode = ProgramMode.values().firstOrNull { it.mode == value } ?: CAPTURE
    }
}
