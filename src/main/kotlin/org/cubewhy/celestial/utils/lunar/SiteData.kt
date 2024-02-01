package org.cubewhy.celestial.utils.lunar

import com.google.gson.JsonObject
import org.cubewhy.celestial.json
import org.cubewhy.celestial.utils.RequestUtils.get
import java.net.URI

/**
 * Create a SiteData instance
 * @param api Launcher API
 */
class SiteData(val api: URI = URI.create("https://api.lunarclientprod.com")) {


    fun metadata(): JsonObject {
        get("$api/site/metadata").execute().use { response ->
            return response.json!!.asJsonObject
        }
    }

    fun getAlert(metadata: JsonObject): String? {
        return metadata["alert"].asString
    }

    fun getPlayersInGame(metadata: JsonObject): Int {
        return metadata.getAsJsonObject("statistics")["game"].asInt
    }

    fun getPlayersInLauncher(metadata: JsonObject): Int {
        return metadata.getAsJsonObject("statistics")["launcher"].asInt
    }
}
