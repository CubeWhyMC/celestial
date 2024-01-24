/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import org.cubewhy.celestial.entities.Assets
import org.cubewhy.celestial.entities.ReleaseEntity
import org.cubewhy.celestial.files.DownloadManager
import org.cubewhy.celestial.files.Downloadable
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.jar.JarFile

object AddonUtils {
    @JvmStatic
    @Throws(IOException::class)
    fun isWeaveMod(file: File): Boolean {
        return isWeaveMod(JarFile(file))
    }

    /**
     * Is weave mod
     *
     * @param jar file of the mod
     * @return yes or no
     */
    fun isWeaveMod(jar: JarFile): Boolean {
        // find weave.mod.json
        return jar.getJarEntry("weave.mod.json") != null
    }

    @JvmStatic
    @Throws(IOException::class)
    fun isLunarCNMod(file: File): Boolean {
        return isLunarCNMod(JarFile(file))
    }

    /**
     * Is LunarCN mod
     *
     * @param jar file of the mod
     * @return yes or no
     */
    fun isLunarCNMod(jar: JarFile): Boolean {
        // find lunarcn.mod.json
        return jar.getJarEntry("lunarcn.mod.json") != null
    }

    @JvmStatic
    @Throws(MalformedURLException::class)
    fun downloadLoader(repo: String?, file: File?): Boolean {
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
        val releaseEntity = TextUtils.jsonToObj(apiJson, ReleaseEntity::class.java)
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
        if (loader == null) {
            return false
        }
        // send download
        DownloadManager.download(Downloadable(loader, file!!, hash!!, Downloadable.Type.SHA256))
        return true
    }
}
