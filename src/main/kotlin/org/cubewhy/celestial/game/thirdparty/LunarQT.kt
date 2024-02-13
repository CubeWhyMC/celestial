/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.thirdparty

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.cubewhy.celestial.config
import org.cubewhy.celestial.toFile
import org.cubewhy.celestial.utils.downloadLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

object LunarQT {
    private val log: Logger = LoggerFactory.getLogger(LunarQT::class.java)

    private val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }
    val configLunarQT: LunarQTConfig = try {
        JSON.decodeFromString(getConfigDir().resolve("config.json").readText())
    } catch (e: FileNotFoundException) {
        log.error(e.stackTraceToString())
        LunarQTConfig()
    }

    fun saveConfig() {
        val string = JSON.encodeToString(configLunarQT)
        FileUtils.writeStringToFile(getConfigDir().resolve("config.json"), string, Charsets.UTF_8)
    }

    fun checkUpdate(): Boolean {
        log.info("Checking update for LunarQT")
        log.warn("LCQT2 is stopped update, downloading lcqt from XiaoHeiPa/lcqt2")
        downloadLoader(
            "XiaoHeiPa/lcqt2",
            config.addon.lcqt.installationDir.toFile()
        )
        return false
    }

    private fun getConfigDir(): File {
        val os = System.getProperty("os.name").lowercase()
        val home = System.getProperty("user.home")

        val configDir: File = when {
            os.contains("windows") -> System.getenv("APPDATA")?.let(::File) ?: File(home, "AppData\\Roaming")
            os.contains("mac") -> File(home, "Library/Application Support")
            else -> System.getenv("XDG_CONFIG_HOME")?.let(::File) ?: File(home, ".config")
        }

        return File(configDir, "lcqt2")
    }
}