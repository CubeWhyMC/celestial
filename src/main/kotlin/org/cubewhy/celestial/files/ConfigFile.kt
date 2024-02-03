/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.files

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.StandardCharsets

open class ConfigFile(val file: File) {
    lateinit var config: JsonObject
        private set

    init {
        this.load()
    }

    fun setValue(key: String, value: String): ConfigFile {
        config.addProperty(key, value)
        return this
    }

    fun setValue(key: String, value: Char): ConfigFile {
        config.addProperty(key, value)
        return this
    }

    fun setValue(key: String, value: Number): ConfigFile {
        config.addProperty(key, value)
        return this
    }

    fun setValue(key: String, value: Boolean): ConfigFile {
        config.addProperty(key, value)
        return this
    }

    fun setValue(key: String, value: JsonObject?): ConfigFile {
        config.add(key, value)
        return this
    }

    fun initValue(key: String, value: JsonElement?): ConfigFile {
        if (!config.has(key)) {
            log.info("Init value $key -> $value")
            config.add(key, value)
        }
        return this
    }

    fun initValue(key: String, value: String): ConfigFile {
        return this.initValue(key, JsonPrimitive(value))
    }

    fun initValue(key: String, value: Int): ConfigFile {
        return this.initValue(key, JsonPrimitive(value))
    }

    fun getValue(key: String): JsonElement {
        return config[key]
    }

    fun save(): ConfigFile {
        try {
            val bufferedWriter = BufferedWriter(FileWriter(this.file))
            bufferedWriter.write(config.toString())
            bufferedWriter.flush()
            bufferedWriter.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return this
    }

    private fun load(): ConfigFile {
        val gson = Gson()
        var bufferedReader: BufferedReader
        var successful = false

        while (!successful) {
            try {
                bufferedReader = BufferedReader(FileReader(this.file, StandardCharsets.UTF_8))
                config = with(gson.fromJson(bufferedReader, JsonObject::class.java)) {
                    this ?: JsonObject()
                }
                successful = true
            } catch (e: FileNotFoundException) {
                try {
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdirs()
                    }
                    file.createNewFile()
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        return this
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConfigFile::class.java)
    }
}
