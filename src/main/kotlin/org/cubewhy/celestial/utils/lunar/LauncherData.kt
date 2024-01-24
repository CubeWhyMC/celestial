/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils.lunar

import com.google.gson.*
import org.cubewhy.celestial.event.impl.CrashReportUploadEvent
import org.cubewhy.celestial.game.AddonMeta
import org.cubewhy.celestial.game.RemoteAddon
import org.cubewhy.celestial.utils.CrashReportType
import org.cubewhy.celestial.utils.OSEnum
import org.cubewhy.celestial.utils.RequestUtils.get
import org.cubewhy.celestial.utils.RequestUtils.post
import java.io.File
import java.io.IOException
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
    @Throws(IOException::class)
    fun metadata(): JsonObject {
        // do request with fake system info
        get("$api/launcher/metadata?installation_id=469a9de3-49b1-489f-ad67-ec55b9e0e727&os=NMSLOS&arch=x64&launcher_version=114.514.191&branch=master&branch_changed=true&private=true&os_release=114.514").execute()
            .use { response ->
                assert(response.code == 200) {
                    "Code = " + response.code // check success
                }
                assert(response.body != null) { "ResponseBody was null" }
                return JsonParser.parseString(response.body!!.string()).asJsonObject
            }
    }

    @Throws(IOException::class)
    fun getVersion(version: String?, branch: String?, module: String?): JsonObject {
        val json = JsonObject()
        json.addProperty("hwid", "HWID-PUBLIC")
        json.addProperty("installation_id", UUID(100, 0).toString()) // fake uuid
        json.addProperty("os", OSEnum.find(System.getProperty("os.name"))?.jsName) // shit js
        json.addProperty("arch", "x64") // example: x64
        json.addProperty("os_release", "19045.3086")
        json.addProperty("launcher_version", "2.15.1")
        json.addProperty("launch_type", "offline")
        json.addProperty("version", version)
        json.addProperty("branch", branch)
        json.addProperty("module", module)

        post("$api/launcher/launch", Gson().toJson(json)).execute().use { response ->
            assert(response.body != null) { "ResponseBody was null" }
            return JsonParser.parseString(response.body!!.string()).asJsonObject
        }
    }

    @Throws(IOException::class)
    fun uploadCrashReport(trace: String?, type: CrashReportType, launchScript: String?): Map<String, String> {
        val map: MutableMap<String, String> = HashMap()
        // do request
        val request = JsonObject()
        request.addProperty("type", type.jsonName)
        request.addProperty("trace", trace)
        request.addProperty("launchScript", launchScript)
        post("$api/launcher/uploadCrashReport", request).execute().use { response ->
            if (response.isSuccessful && response.body != null) {
                val json = JsonParser.parseString(response.body!!.string()).asJsonObject.getAsJsonObject("data")
                val id = json["id"].asString
                val url = json["url"].asString
                map["id"] = id
                map["message"] = json["message"].asString
                map["url"] = url
                CrashReportUploadEvent(id, url).call()
            }
        }
        return map
    }

    @get:Throws(IOException::class)
    val plugins: List<RemoteAddon>?
        /**
         * Get the plugin list
         */
        get() {
            val list: MutableList<RemoteAddon> = ArrayList()
            val info = URL("$api/plugins/info")
            var json: JsonArray
            try {
                get(info).execute().use { response ->
                    assert(response.body != null)
                    json = JsonParser.parseString(response.body!!.string()).asJsonObject.getAsJsonArray("data")
                }
            } catch (e: JsonSyntaxException) {
                return null // official api
            }
            for (element in json) {
                val plugin = element.asJsonObject
                list.add(
                    RemoteAddon(
                        plugin["name"].asString,
                        URL(api.toString() + plugin["downloadLink"].asString),
                        RemoteAddon.Category.parse(
                            plugin["category"].asString
                        )!!,
                        Gson().fromJson(plugin["meta"], AddonMeta::class.java)
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
        fun getMainClass(json: JsonObject?): String {
            if (json == null) {
                return "com.moonsworth.lunar.genesis.Genesis"
            }
            return json
                .getAsJsonObject("launchTypeData")["mainClass"].asString
        }

        /**
         * Get ICHOR state
         *
         * @return true (always)
         */
        @Throws(IOException::class)
        fun getIchorState(json: JsonObject): Boolean {
            return if (json.getAsJsonObject("launchTypeData").has("ichor")) {
                json
                    .getAsJsonObject("launchTypeData")["ichor"].asBoolean
            } else {
                true // forces enable ichor in the latest api version
            }
        }

        /**
         * Get the alert message
         *
         * @param metadata metadata from api
         * @return a map of the alert (title, message)
         */
        fun getAlert(metadata: JsonObject): Map<String, String>? {
            if (metadata.has("alert") && !metadata["alert"].isJsonNull) {
                val alert = metadata.getAsJsonObject("alert")
                val map: MutableMap<String, String> = HashMap()
                map["title"] = alert["name"].asString
                map["message"] = alert["text"].asString
                return map
            } else {
                return null
            }
        }

        /**
         * Get blog posts
         *
         * @param metadata metadata from api
         * @return a list of blog posts
         */
        fun getBlogPosts(metadata: JsonObject): JsonArray {
            return metadata.getAsJsonArray("blogPosts")
        }

        fun getDefaultJvmArgs(json: JsonObject, installation: File): List<String> {
            val out: MutableList<String> = ArrayList()
            for (arg in json
                .getAsJsonObject("jre")
                .getAsJsonArray("extraArguments")) {
                if (arg.asString == "-Djna.boot.library.path=natives") {
                    out.add("-Djna.boot.library.path=\"$installation/natives\"")
                    continue
                }
                out.add(arg.asString)
            }
            out.add("-Djava.library.path=\"$installation/natives\"")
            return out
        }

        /**
         * Get support versions
         *
         * @param metadata LC metadata
         * @return Support versions list
         */
        fun getSupportVersions(metadata: JsonElement): Map<String, Any> {
            val map: MutableMap<String, Any> = HashMap()
            val versions: MutableList<String> = ArrayList()
            val versionsJson = Objects.requireNonNull(metadata).asJsonObject.getAsJsonArray("versions")
            for (version in versionsJson) {
                var versionId: String
                if (version.asJsonObject.has("subversions")) {
                    for (subVersion in version.asJsonObject["subversions"].asJsonArray) {
                        versionId = subVersion.asJsonObject["id"].asString
                        if (version.asJsonObject["default"].asBoolean) {
                            map["default"] = versionId
                        }
                        versions.add(versionId)
                    }
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
        @Throws(IOException::class)
        fun getVersionInMetadata(metadata: JsonElement, version: String): JsonObject? {
            val metadata1 = metadata.asJsonObject
            for (version1 in metadata1["versions"].asJsonArray) {
                if (version.contains(version1.asJsonObject["id"].asString)) {
                    for (subVersion in version1.asJsonObject["subversions"].asJsonArray) {
                        if (subVersion.asJsonObject["id"].asString == version) {
                            return subVersion.asJsonObject
                        }
                    }
                }
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
        @Throws(IOException::class)
        fun getSupportModules(metadata: JsonElement, version: String): Map<String, Any> {
            val map: MutableMap<String, Any> = HashMap()
            val modules: MutableList<String> = ArrayList()
            //        boolean isSubVersion = StringUtils.count(version, '.') >= 2;
            val version1 = getVersionInMetadata(metadata, version)
            val modulesJson = version1!!.getAsJsonArray("modules")
            for (moduleJson in modulesJson) {
                val id = moduleJson.asJsonObject["id"].asString
                modules.add(id)
                if (moduleJson.asJsonObject["default"].asBoolean) {
                    map["default"] = id
                }
            }
            map["modules"] = modules
            return map
        }

        /**
         * Get a list of LunarClient Artifacts
         *
         * @param version artifacts json
         * @return artifact map {"fileName": {url, sha1, type}}
         */
        @Throws(IOException::class)
        fun getArtifacts(version: JsonElement): Map<String, Map<String, String>> {
            val out: MutableMap<String, Map<String, String>> = HashMap()

            val versionJson = Objects.requireNonNull(version).asJsonObject
            val launchTypeData = versionJson.getAsJsonObject("launchTypeData")
            val artifacts = launchTypeData.getAsJsonArray("artifacts")

            for (artifact in artifacts) {
                val info: MutableMap<String, String> = HashMap()
                val key = artifact.asJsonObject["name"].asString
                val url = artifact.asJsonObject["url"].asString
                val sha1 = artifact.asJsonObject["sha1"].asString
                val type = artifact.asJsonObject["type"].asString
                info["url"] = url
                info["sha1"] = sha1
                info["type"] = type
                out[key] = info
            }

            return out
        }

        /**
         * Get the textures' index of LunarClient
         *
         * @param version version info
         * @return textures' index
         */
        @Throws(IOException::class)
        fun getLunarTexturesIndex(version: JsonElement): Map<String, String>? {
            val versionJson = version.asJsonObject
            val indexUrl = versionJson.getAsJsonObject("textures")["indexUrl"].asString
            // get index json
            val baseUrl = getTexturesBaseUrl(version)
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
            return null
        }

        fun getTexturesBaseUrl(version: JsonElement): String {
            val versionJson = Objects.requireNonNull(version).asJsonObject
            return versionJson.getAsJsonObject("textures")["baseUrl"].asString
        }
    }
}
