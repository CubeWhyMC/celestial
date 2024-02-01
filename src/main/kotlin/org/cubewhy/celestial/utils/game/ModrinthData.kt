/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils.game

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.Response
import org.cubewhy.celestial.utils.RequestUtils
import java.net.URL

class ModrinthData(private val api: URL) {
    fun getProject(id: String) =
        RequestUtils.get("$api/v2/project/$id").execute().use { response ->
            response.json
        }

    fun getVersion(id: String) {
        RequestUtils.get("$api/v2/version/$id").execute().use { response ->
            response.json
        }
    }

    companion object {
        // TODO Modrinth api
    }
}

val Response.json: JsonElement?
    get() {
        val json = this.body!!.string()
        return JsonParser.parseString(json)
    }