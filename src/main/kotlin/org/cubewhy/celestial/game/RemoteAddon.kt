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
package org.cubewhy.celestial.game

import org.jetbrains.annotations.Contract
import java.net.URL

class RemoteAddon(@JvmField var name: String, @JvmField var downloadURL: URL, @JvmField var category: Category) {
    enum class Category {
        AGENT,
        CN,
        WEAVE;


        companion object {
            /**
             * Parse plugin type from a string
             */
            @Contract(pure = true)
            fun parse(category: String): Category? {
                return when (category) {
                    "cn" -> CN
                    "weave", "Mod" -> WEAVE
                    "Agent" -> AGENT
                    else -> null
                }
            }
        }
    }
}
