/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.cubewhy.celestial.gui.Language
import org.cubewhy.celestial.utils.totalMem
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

@Serializable
data class GameVersionInfo(
    var version: String,
    var module: String,
    var branch: String = "master"
)

@Serializable
data class GameResize(
    var width: Int = 854,
    var height: Int = 480
)

@Serializable
data class GameConfiguration(
    var ram: Int = totalMem / 4,
    var gameDir: String = minecraftFolder.path,
    var target: GameVersionInfo? = null,
    var wrapper: String = "", // // like optirun on linux
    var resize: GameResize = GameResize(), // (854, 480) for default
    @SerialName("program-args")
    var args: ArrayList<String> = ArrayList(),
    @SerialName("vm-args")
    var vmArgs: ArrayList<String> = ArrayList(),
    var javaagents: HashMap<String, String?> = HashMap()
)


enum class CloseFunction(val jsonValue: String, val text: String) {
    NOTHING("nothing", f.getString("gui.settings.launcher.close-action.nothing")),
    EXIT_JAVA("exitJava", f.getString("gui.settings.launcher.close-action.exit-java")),
    TRAY("tray", f.getString("gui.settings.launcher.close-action.tray")),
    REOPEN("reopen", f.getString("gui.settings.launcher.close-action.reopen"));

    override fun toString() = text

    companion object {
        fun findByJsonValue(value: String): CloseFunction? {
            for (value1 in entries) {
                if (value1.jsonValue == value) return value1
            }
            return null
        }
    }
}

@Serializable
data class AddonLoaderConfiguration(
    var state: Boolean,
    @SerialName("installation")
    var installationDir: String,
    @SerialName("check-update")
    var checkUpdate: Boolean = true
)

@Serializable
data class AddonConfiguration(
    val weave: AddonLoaderConfiguration = AddonLoaderConfiguration(false, configDir.resolve("loaders/weave.jar").path),
    val lunarcn: AddonLoaderConfiguration =AddonLoaderConfiguration(false, configDir.resolve("loaders/cn.jar").path),
    val lcqt: AddonLoaderConfiguration = AddonLoaderConfiguration(false, configDir.resolve("loaders/lcqt-agent.jar").path),
)

@Serializable
data class CeleWrapConfiguration(
    var state: Boolean = true,
    var checkUpdate: Boolean = true
)

@Serializable
data class BasicConfig(
    var api: String = "https://lunarclient.top/api",
    var jre: String = "", // // leave empty if you want to use the default one
    var language: Language = Language.ENGLISH,
    var theme: String = "dark",
    var dataSharing: Boolean = true,
    @SerialName("installation-dir")
    var installationDir: String = File(configDir, "game").path,
    @SerialName("game-dir")
    var game: GameConfiguration = GameConfiguration(),
    @SerialName("max-threads")
    var maxThreads: Int = Runtime.getRuntime().availableProcessors() * 2,
    var addon: AddonConfiguration = AddonConfiguration(),
    var proxy: ProxyConfig = ProxyConfig(),
    var connectMethod: ConnectMethod = ConnectMethod.DISABLE,
    var celeWrap: CeleWrapConfiguration = CeleWrapConfiguration()
) {

    enum class ConnectMethod {
        ATTACH, // using the attachment API to inject LunarClient (recommend)
        CMDLINE, // using the "-javaagent" parameter
        DISABLE
    }
}

class Mirror(address: String) {
    val host: String = address.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    val port: Int = address.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()

    override fun toString(): String {
        return "$host:$port"
    }
}

@Serializable
data class ProxyConfig(
    var state: Boolean = false,
    @SerialName("proxy")
    var proxyAddress: String = "http://127.0.0.1:8080",
    var mirror: HashMap<String, String> = HashMap()
) {
    operator fun get(address: String) =
        mirror[address]?.let { Mirror(it) }

    fun useMirror(src: URL): URL {
        val host = src.host
        var port = src.port
        if (port == -1) {
            port = src.defaultPort
        }
        val completed = "$host:$port"
        if (mirror.containsKey(completed)) {
            val mirror = this[completed]!!
            return URL(src.protocol, mirror.host, mirror.port, src.file)
        }
        return src
    }

    fun toProxy() : Proxy? {
        if (this.proxyAddress.isBlank()) return null
        val address = URL(this.proxyAddress)
        return if (state) Proxy(getProtocolType(address.protocol), InetSocketAddress(address.host, address.port)) else null
    }

    private fun getProtocolType(protocol: String): Proxy.Type {
        return when (protocol) {
            "http" -> Proxy.Type.HTTP
            "socks" -> Proxy.Type.SOCKS
            else -> throw IllegalStateException("Unexpected value: $protocol")
        }
    }

    fun applyMirrors(map: HashMap<String, String>) {
        this.mirror = map
    }
}
