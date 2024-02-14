/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.serialization.encodeToString
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.cubewhy.celestial.JSON
import org.cubewhy.celestial.config
import java.net.URL

object RequestUtils {
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .proxy(config.proxy.toProxy())
        .build()


    fun get(url: String): Call {
        return get(URL(url))
    }


    fun get(url: URL): Call {
        val request: Request = Request.Builder()
            .url(config.proxy.useMirror(url))
            .build()

        return httpClient.newCall(request)
    }

    fun request(request: Request): Call {
        return httpClient.newCall(request)
    }


    fun post(url: String, json: String): Call {
        val body: RequestBody = json.toRequestBody("application/json".toMediaType()) // MUST be JSON in the latest LC-API
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        return httpClient.newCall(request)
    }


    fun post(url: String, map: Map<*, *>): Call {
        return post(url, JSON.encodeToString(map))
    }
}
