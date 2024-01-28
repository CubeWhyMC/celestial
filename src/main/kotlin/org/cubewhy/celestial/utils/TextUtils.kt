/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils

import com.google.gson.Gson
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

object TextUtils {

    fun dumpTrace(e: Exception): String {
        val s = StringWriter()
        val stream = PrintWriter(s)
        e.printStackTrace(stream)
        return s.toString()
    }

    fun <T> jsonToObj(json: String, clz: Class<T>): T? {
        val gson = Gson()
        if (Objects.isNull(json)) return null
        val obj = gson.fromJson(json, clz)
        return if (Objects.isNull(obj)) {
            null
        } else {
            obj
        }
    }
}
