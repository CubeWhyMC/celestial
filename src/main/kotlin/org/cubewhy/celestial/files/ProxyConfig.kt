/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.files

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

class ProxyConfig(file: File?) : ConfigFile(file!!) {
    class Mirror(address: String) {
        val host: String = address.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val port: Int = address.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
    }

    init {
        this.initValue("state", JsonPrimitive(false))
        this.initValue("proxy", "http://127.0.0.1:8080")
        this.initValue("mirror", JsonObject())
    }

    fun useProxy(address: URL): ProxyConfig {
        this.setValue("proxy", address.toString())
        this.save()
        return this
    }

    fun setState(state: Boolean): ProxyConfig {
        this.setValue("state", state)
        return this
    }

    val state: Boolean
        get() = this.getValue("state").asBoolean

    val proxy: Proxy?
        get() {
            val address = URL(this.proxyAddress)
            return if (state) Proxy(getType(address.protocol), InetSocketAddress(address.host, address.port)) else null
        }

    private val proxyAddress: String
        get() = this.getValue("proxy").asString

    private fun getType(protocol: String): Proxy.Type {
        return when (protocol) {
            "http" -> Proxy.Type.HTTP
            "socks" -> Proxy.Type.SOCKS
            else -> throw IllegalStateException("Unexpected value: $protocol")
        }
    }

    fun useMirror(src: URL): URL {
        val host = src.host
        var port = src.port
        if (port == -1) {
            port = src.defaultPort
        }
        val completed = "$host:$port"
        if (config.getAsJsonObject("mirror").has(completed)) {
            val mirror = getMirror(completed)
            return URL(src.protocol, mirror.host, mirror.port, src.file)
        }
        return src
    }

    private fun getMirror(address: String?): Mirror {
        val json = getValue("mirror").asJsonObject
        return Mirror(json[address].asString)
    }

    fun applyMirrors(mirrors: JsonObject?): ProxyConfig {
        log.info("apply mirrors")
        this.setValue("mirror", mirrors)
        return this
    }

    fun addMirror(source: String, mirror: String) {
        this.config["mirror"].asJsonObject.addProperty(source, mirror)
    }

    fun hasMirror(source: String): Boolean {
        return this.config["mirror"].asJsonObject.has(source)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ProxyConfig::class.java)
    }
}
