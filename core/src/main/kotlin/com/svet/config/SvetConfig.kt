package com.svet.config

import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths

private val logger = KotlinLogging.logger {}

/**
 * Конфигурация приложения.
 **/
class SvetConfig {

    private val configDirectory = "config"
    private val connectConfigFileName = "connect-config.json"
    private val captureConfigFileName = "capture-config.json"

    lateinit var connectConfig: ConnectConfig
    lateinit var captureConfig: CaptureConfig

    init {
        loadConfigs()
    }

    fun loadConfigs() {
        loadConnectConfig()
        loadCaptureConfig()
    }

    fun saveConfigs() {
        saveConnectConfig()
        saveCaptureConfig()
    }

    private fun loadConnectConfig() {
        logger.info("Loading connect configuration...")

        connectConfig = ConfigHelper.loadConfig(
            resolveConfigFileName(connectConfigFileName),
            ConnectConfig::class.java,
            ConnectConfig() // TODO: ConnectConfig.defaultConfig()
        )

        logger.info("Loading connect configuration done")
    }

    private fun loadCaptureConfig() {
        logger.info("Loading capture configuration...")

        captureConfig = ConfigHelper.loadConfig(
            resolveConfigFileName(captureConfigFileName),
            CaptureConfig::class.java,
            CaptureConfig.defaultConfig()
        )

        if (captureConfig.positions.isEmpty()) {
            logger.warn("Capture regions configuration was not loaded")
        }

        logger.info("Loading capture configuration done")
    }

    fun saveConnectConfig() {
        Files.createDirectories(Paths.get(configDirectory))
        ConfigHelper.saveConfig(resolveConfigFileName(connectConfigFileName), connectConfig)
    }

    fun saveCaptureConfig() {
        Files.createDirectories(Paths.get(configDirectory))
        ConfigHelper.saveConfig(resolveConfigFileName(captureConfigFileName), captureConfig)
    }

    private fun resolveConfigFileName(fileName: String): String {
        return configDirectory + "\\" + fileName
    }

}
