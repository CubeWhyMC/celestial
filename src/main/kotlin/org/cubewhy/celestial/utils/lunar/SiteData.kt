package org.cubewhy.celestial.utils.lunar

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.cubewhy.celestial.utils.RequestUtils.get
import java.io.IOException
import java.net.URI

class SiteData
/**
 * Create a SiteData instance with the official Launcher API
 */ @JvmOverloads constructor(val api: URI = URI.create("https://api.lunarclientprod.com")) {
    /**
     * Create a SiteData instance with special API resource
     *
     * @param api Launcher API
     */

    
    fun metadata(): JsonObject {
        get("$api/site/metadata").execute().use { response ->
            assert(response.code == 200) {
                "Code = " + response.code // check success
            }
            assert(response.body != null) { "ResponseBody was null" }
            return JsonParser.parseString(response.body!!.string()).asJsonObject
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
