/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.cubewhy.celestial.proxy
import java.net.URL

object RequestUtils {
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .proxy(proxy.proxy)
        .build()
    private val JSON: MediaType = "application/json".toMediaType()


    fun get(url: String): Call {
        return get(URL(url))
    }


    fun get(url: URL): Call {
        val request: Request = Request.Builder()
            .url(proxy.useMirror(url))
            .build()

        return httpClient.newCall(request)
    }

    fun request(request: Request): Call {
        return httpClient.newCall(request)
    }


    fun post(url: String, json: String): Call {
        val body: RequestBody = json.toRequestBody(JSON) // MUST be JSON in the latest LC-API
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        return httpClient.newCall(request)
    }


    fun post(url: String, json: JsonElement): Call {
        val gson = Gson()
        val realJson = gson.toJson(json)
        return post(url, realJson)
    }
}
