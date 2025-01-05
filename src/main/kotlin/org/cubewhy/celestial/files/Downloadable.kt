/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.files

import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

class Downloadable(
    private val url: URL,
    private val file: File,
    private val hash: String?,
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
                    file, this.hash, this.type
                )
            } catch (e: Exception) {
                log.error("Download ${this.url} failed, try again... [$i/$FALL_BACK]")
                continue  // try again
            }
            break // no error
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Downloadable::class.java)
        const val FALL_BACK: Int = 5
    }
}
