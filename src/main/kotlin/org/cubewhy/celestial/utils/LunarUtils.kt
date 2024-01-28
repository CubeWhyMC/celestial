/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import com.google.gson.JsonParser
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets

object LunarUtils {

    fun isReallyOfficial(session: File?): Boolean {
        val json = JsonParser.parseString(FileUtils.readFileToString(session, StandardCharsets.UTF_8)).asJsonObject
        return !json.has("celestial")
    }
}
