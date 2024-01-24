/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game

import org.jetbrains.annotations.Contract
import java.net.URL

data class RemoteAddon(var name: String, var downloadURL: URL, var category: Category, val meta: AddonMeta?) {
    enum class Category {
        AGENT, CN, WEAVE;

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

    override fun toString(): String {
        return "RemoteAddon(name='$name', downloadURL=$downloadURL, category=$category, meta=$meta)"
    }
}
