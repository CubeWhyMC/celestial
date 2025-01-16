/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils.lunar

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.cubewhy.celestial.JSON
import org.cubewhy.celestial.config
import org.cubewhy.celestial.event.impl.CrashReportUploadEvent
import org.cubewhy.celestial.game.AddonMeta
import org.cubewhy.celestial.game.RemoteAddon
import org.cubewhy.celestial.string
import org.cubewhy.celestial.utils.CrashReportType
import org.cubewhy.celestial.utils.OSEnum
import org.cubewhy.celestial.utils.RequestUtils.get
import org.cubewhy.celestial.utils.RequestUtils.post
import org.cubewhy.celestial.utils.arch
import java.net.URI
import java.net.URL
import java.util.*

/**
 * Create a LauncherData instance with the official Launcher API
 */
class LauncherData(val api: URI = URI.create("https://api.lunarclientprod.com")) {
    /**
     * Create a LauncherData instance with special API resource
     *
     * @param api Launcher API
     */
    constructor(api: String) : this(URI.create(api))

    /**
     * Get metadata
     *
     * @return Launcher Metadata
     */

    fun metadata(): LauncherMetadata {
        // do request with fake system info
        // DO NOT REPLACE THE launcher_version FIELD TO config.api.versionSpoof
        get("$api/launcher/metadata?installation_id=${UUID.randomUUID()}&os=${OSEnum.current!!.jsName}&arch=${arch}&launcher_version=114.514.191&branch=master&branch_changed=true&private=true&os_release=114.514").execute()
            .use { response ->
                assert(response.code == 200) {
                    "Code = " + response.code
                }
                assert(response.body != null) { "ResponseBody was null" }
                return JSON.decodeFromString(response.string!!)
            }
    }


    fun getVersion(version: String?, branch: String?, module: String?): GameArtifactInfo {
        val map = mapOf(
            "hwid" to "HWID-PUBLIC",
            "installation_id" to UUID.randomUUID().toString(), // fake uuid
            "os" to OSEnum.current!!.jsName, // shit js
            "arch" to arch, // example: x64
            "os_release" to "19045.3086", // fake os release
            "launcher_version" to config.api.versionSpoof, // fake version
            "launch_type" to "offline",
            "version" to version,
            "branch" to branch,
            "module" to module
        )

        post("$api/launcher/launch", JSON.encodeToString(map)).execute().use { response ->
            assert(response.body != null) { "ResponseBody was null" }
            return JSON.decodeFromString(response.string!!)
        }
    }


    fun uploadCrashReport(trace: String?, type: CrashReportType, launchScript: String?): CrashReportResult? {
        // do request
        val request = mapOf(
            "type" to type.jsonName,
            "trace" to trace,
            "launchScript" to launchScript
        )
        post("$api/launcher/uploadCrashReport", request).execute().use { response ->
            if (response.isSuccessful && response.body != null) {
                val result: CrashReportResult = JSON.decodeFromString(response.string!!)
                CrashReportUploadEvent(result).call()
                return result
            }
        }
        return null
    }

    val plugins: List<RemoteAddon>?
        /**
         * Get the plugin list
         */
        get() {
            val list: MutableList<RemoteAddon> = ArrayList()
            val info = URL("$api/plugins/info")
            var json: Array<PluginInfo>
            try {
                get(info).execute().use { response ->
                    assert(response.body != null)
                    json = JSON.decodeFromString(response.string!!)
                }
            } catch (e: Exception) {
                return null // official api
            }
            for (plugin in json) {
                list.add(
                    RemoteAddon(
                        plugin.name,
                        URL(api.toString() + plugin.downloadLink),
                        plugin.sha1,
                        plugin.category,
                        plugin.meta
                    )
                )
            }
            return list
        }

    companion object {
        /**
         * Get LunarClient main-class (online)
         *
         * @param json Json of the special LunarClient instance
         * @return main class of the LunarClient instance
         */
        fun getMainClass(json: GameArtifactInfo? = null): String =
            json?.launchTypeData?.mainClass ?: "com.moonsworth.lunar.genesis.Genesis"

        fun check(api: String): Boolean {
            try {
                get(api).execute()
                return true
            } catch (e: Exception) {
                return false
            }
        }

        fun getDefaultJvmArgs(json: GameArtifactInfo): List<String> {
            val out: MutableList<String> = ArrayList()
            for (arg in json.jre.extraArguments) {
                // block sentry
                if (arg.startsWith("-Dichor.filteredGenesisSentries")) {
                    out.add("-Dichor.filteredGenesisSentries=.*") // block sentry
                    continue
                }
                out.add(arg)
            }
            out.add("-Djava.library.path=natives")
            return out
        }

        /**
         * Get support versions
         *
         * @param metadata LC metadata
         * @return Support versions list
         */
        fun getSupportVersions(metadata: LauncherMetadata): Map<String, Any> {
            val map: MutableMap<String, Any> = HashMap()
            val versions: MutableList<String> = ArrayList()
            for (version in metadata.versions) {
                var versionId: String
                for (subVersion in version.subversions) {
                    versionId = subVersion.id
                    if (version.default) {
                        map["default"] = versionId
                    }
                    versions.add(versionId)
                }
            }
            map["versions"] = versions
            return map
        }

        /**
         * Get information of a subversion
         *
         * @param version version name
         * @return version json
         */
        fun getVersionInMetadata(metadata: LauncherMetadata, version: String): LunarSubVersion? {
            for (version1 in metadata.versions) {
                if (version.contains(version1.id))
                    for (subVersion in version1.subversions)
                        if (subVersion.id == version) return subVersion
            }
            return null
        }

        /**
         * Get support addons
         *
         * @param metadata LC metadata
         * @param version  Minecraft version
         * @return Module List
         */
        fun getSupportModules(metadata: LauncherMetadata, version: String): Map<String, Any> {
            val map: MutableMap<String, Any> = HashMap()
            val modules: MutableList<String> = ArrayList()
            val version1 = getVersionInMetadata(metadata, version)
            val modulesJson = version1!!.modules
            for (moduleJson in modulesJson) {
                val id = moduleJson.id
                modules.add(id)
                if (moduleJson.default) {
                    map["default"] = id
                }
            }
            map["modules"] = modules
            return map
        }

        /**
         * Get the textures' index of LunarClient
         *
         * @param version version info
         * @return textures' index
         */

        fun getLunarTexturesIndex(version: GameArtifactInfo): Map<String, String>? {
            val indexUrl = version.textures.indexUrl
            // get index json
            val baseUrl = version.textures.baseUrl
            return getIndex(baseUrl, indexUrl)
        }

        fun getLunarUiAssetsIndex(version: GameArtifactInfo): Map<String, String> {
            if (version.ui == null) return emptyMap() // there's no ui files for old LC
            return getIndex(version.ui.assets.baseUrl, version.ui.assets.indexUrl)
        }

        private fun getIndex(baseUrl: String, indexUrl: String): Map<String, String> {
            get(indexUrl).execute().use { response ->
                if (response.body != null) {
                    // parse
                    val map: MutableMap<String, String> = HashMap()
                    for (s in response.body!!.string().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()) {
                        // filename hashcode
                        map[s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]] =
                            baseUrl + s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1]
                    }
                    return map
                }
            }
            return emptyMap()
        }
    }
}

// Entities

@Serializable
data class LunarVersion(
    val id: String,
    val default: Boolean,
    val subversions: List<LunarSubVersion>
)

@Serializable
data class LunarSubVersion(
    val id: String,
    val default: Boolean,
    val modules: List<LunarModule>
)

@Serializable
data class GameArtifactInfo(
    val launchTypeData: LaunchTypeData,
    val textures: Textures,
    val jre: RuntimeInfo,
    val ui: UiInfo? = null,
    val canaryToken: String? = ""
) {
    @Serializable
    data class RuntimeInfo(
        val extraArguments: List<String>
    )

    @Serializable
    data class Artifact(
        val name: String,
        val sha1: String,
        val url: String,
        val type: ArtifactType,
    ) {
        enum class ArtifactType {
            CLASS_PATH,
            EXTERNAL_FILE,
            NATIVES,
            JAVAAGENT // LCCN API, not support yet
        }
    }

    @Serializable
    data class LaunchTypeData(
        val artifacts: List<Artifact>,
        val mainClass: String,
        val ichor: Boolean = true
    )

    @Serializable
    data class Textures(
        val indexUrl: String,
        val indexSha1: String,
        val baseUrl: String
    )

    @Serializable
    data class UiInfo(
        val sourceUrl: String,
        val sourceSha1: String,
        val assets: UiAssets
    )

    @Serializable
    data class UiAssets(
        val baseUrl: String,
        val indexUrl: String,
        val indexSha1: String
    )
}

@Serializable
data class LunarModule(
    val id: String,
    val default: Boolean,
)

@Serializable
data class Blogpost(
    val title: String,
    val excerpt: String? = null, // only available on LCCN API
    val image: String,
    val link: String,
    val author: String? = null, // moonsworth removed this in v3.0.0
    @SerialName("button_text")
    val buttonText: String? = null,
    val type: ButtonType? = ButtonType.OPEN_LINK
) {
    enum class ButtonType {
        OPEN_LINK,
        CHANGE_API
    }
}

@Serializable
data class Alert(
    val name: String? = "Alert",
    val text: String? = null,
    val link: String? = null
)

@Serializable
data class LauncherMetadata(
    val versions: List<LunarVersion>,
    @SerialName("blogPosts")
    val blogposts: List<Blogpost> = emptyList(),
    val alert: Alert? = null
)

@Serializable
data class PluginInfo(
    val name: String,
    val sha1: String,
    val downloadLink: String,
    val category: RemoteAddon.Category,
    val meta: AddonMeta? = null
)

@Serializable
data class CrashReportResult(
    val id: String,
    val url: String,
    val message: String
)
