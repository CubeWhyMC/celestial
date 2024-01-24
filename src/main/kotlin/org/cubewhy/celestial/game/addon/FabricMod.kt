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
package org.cubewhy.celestial.game.addon

import org.cubewhy.celestial.Celestial
import org.cubewhy.celestial.game.BaseAddon
import java.io.File
import java.io.IOException
import java.util.*

class FabricMod(@JvmField val file: File) : BaseAddon() {
    override fun toString(): String {
        return file.name
    }

    override val isEnabled: Boolean
        get() =// TODO fabric: isEnabled
            true

    override fun toggle(): Boolean {
        return toggle0(file)
    }

    companion object {
        @JvmField
        val modFolder: File = File(Celestial.config.getValue("installation-dir").asString, "mods")

        @JvmStatic
        fun findAll(): List<FabricMod> {
            val list: MutableList<FabricMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar") && file.isFile) {
                        list.add(FabricMod(file))
                    }
                }
            }
            return list
        }

        @JvmStatic
        @Throws(IOException::class)
        fun add(file: File?): FabricMod? {
            val target = autoCopy(file!!, modFolder)
            return if ((target == null)) null else FabricMod(target)
        }
    }
}
