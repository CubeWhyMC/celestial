/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import com.google.gson.Gson
import org.cubewhy.celestial.entities.Assets
import org.cubewhy.celestial.entities.ReleaseEntity
import org.cubewhy.celestial.files.DownloadManager
import org.cubewhy.celestial.files.Downloadable
import java.io.File
import java.net.URL
import java.util.*


fun downloadLoader(repo: String, file: File): Boolean {
    var apiJson: String
    try {
        RequestUtils.get(String.format("https://api.github.com/repos/%s/releases/latest", repo)).execute()
            .use { response ->
                assert(response.body != null)
                apiJson = response.body!!.string()
            }
    } catch (e: Exception) {
        return false
    }
    val releaseEntity = apiJson.jsonToObj(ReleaseEntity::class.java)
    var hash: String? = null
    var loader: URL? = null
    if (releaseEntity != null) {
        val assetsArray = releaseEntity.assets.toTypedArray<Assets>()
        for (assets in assetsArray) {
            val url = URL(assets.browser_download_url)
            if (assets.name.endsWith(".jar")) {
                loader = url
            }
            if (assets.name.endsWith(".sha256")) {
                try {
                    RequestUtils.get(url).execute().use { response ->
                        assert(response.body != null)
                        hash = response.body!!.string().split(" ").dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0]
                    }
                } catch (ignored: Exception) {
                    // it's OK to be null
                }
            }
        }
    }
    if (loader === null) {
        return false
    }
    // send download
    DownloadManager.download(Downloadable(loader, file, hash!!, Downloadable.Type.SHA256))
    return true
}

private fun <T> String.jsonToObj(clz: Class<T>): T? {
    val gson = Gson()
    val obj = gson.fromJson(this, clz)
    return if (Objects.isNull(obj)) {
        null
    } else {
        obj
    }
}

