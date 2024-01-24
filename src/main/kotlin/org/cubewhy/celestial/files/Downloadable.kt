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
package org.cubewhy.celestial.files

import java.io.File
import java.net.URL

class Downloadable(
    private val url: URL,
    private val file: File,
    private val crcSha: String?,
    private val type: Type
) : Runnable {
    constructor(url: URL, file: File, sha1: String?) : this(url, file, sha1, Type.SHA1)


    enum class Type {
        SHA1,
        SHA256
    }

    /**
     * Start download
     */
    override fun run() {
        // TODO multipart support
        for (i in 0 until FALL_BACK) {
            try {
                DownloadManager.download0(
                    this.url,
                    file, this.crcSha, this.type
                )
            } catch (e: Exception) {
                continue  // try again
            }
            break // no error
        }
    }

    companion object {
        const val FALL_BACK: Int = 5
    }
}
