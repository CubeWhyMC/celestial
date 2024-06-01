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

package org.cubewhy.celestial.game.thirdparty

import org.cubewhy.celestial.configDir
import org.cubewhy.celestial.utils.downloadLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object CeleWrap {
    private val log: Logger = LoggerFactory.getLogger(CeleWrap::class.java)
    val installation = configDir.resolve("celewrap.jar") // celewrap library

    fun checkUpdate() : Boolean {
        log.info("Checking update for CeleWrap")
        return downloadLoader("CubeWhyMC/celewrap", installation)
    }
}