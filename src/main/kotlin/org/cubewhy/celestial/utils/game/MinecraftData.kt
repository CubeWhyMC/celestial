/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils.game

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.cubewhy.celestial.utils.RequestUtils.get
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

object MinecraftData {
    private var versionManifest: URL
    var texture: URL? = null

    init {
        try {
            versionManifest = URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json")
            texture = URL("https://resources.download.minecraft.net")
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    fun manifest(): JsonObject {
        get(versionManifest).execute().use { response ->
            assert(response.body != null)
            return JsonParser.parseString(response.body!!.string()).asJsonObject
        }
    }

    /**
     * Get information of a Minecraft release
     *
     * @param version version id
     * @return version json
     */
    @Throws(IOException::class)
    fun getVersion(version: String, json: JsonElement): JsonObject? {
        val versionsArray = json.asJsonObject.getAsJsonArray("versions")
        for (element in versionsArray) {
            if (element.asJsonObject["id"].asString == version) {
                val url = element.asJsonObject["url"].asString
                get(url).execute().use { response ->
                    assert(response.body != null)
                    return JsonParser.parseString(response.body!!.string()).asJsonObject
                }
            }
        }
        return null
    }

    /**
     * Get texture index (Minecraft)
     *
     * @param json json object from MinecraftData.getVersion
     * @return json of texture index
     */
    @Throws(IOException::class)
    fun getTextureIndex(json: JsonElement): JsonObject {
        val url = URL(json.asJsonObject.getAsJsonObject("assetIndex")["url"].asString)
        get(url).execute().use { response ->
            assert(response.body != null)
            return JsonParser.parseString(response.body!!.string()).asJsonObject
        }
    }
}
