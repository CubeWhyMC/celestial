/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils

import com.google.gson.Gson
import java.util.*

object TextUtils {

    fun <T> jsonToObj(json: String, clz: Class<T>): T? {
        val gson = Gson()
        val obj = gson.fromJson(json, clz)
        return if (Objects.isNull(obj)) {
            null
        } else {
            obj
        }
    }
}
