package com.svet.config

/**
 * Конфигурация подключения к контроллеру.
 **/
class ConnectConfig {
    var portNumber: String = "COM1"
    var detectPorts: Boolean = false
    var arduinoRebootTimeout = 1500L
}
