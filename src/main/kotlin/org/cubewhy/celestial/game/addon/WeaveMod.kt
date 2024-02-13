/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game.addon

import org.cubewhy.celestial.config
import org.cubewhy.celestial.game.BaseAddon
import org.cubewhy.celestial.toFile
import org.cubewhy.celestial.utils.downloadLoader
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class WeaveMod(val file: File) : BaseAddon() {
    override fun toString(): String {
        return file.name
    }

    override val isEnabled: Boolean
        get() = file.name.endsWith(".jar")

    override fun toggle(): Boolean {
        return toggle0(file)
    }

    companion object {
        val modFolder: File = File(System.getProperty("user.home"), ".weave/mods")
        private val log: Logger = LoggerFactory.getLogger(WeaveMod::class.java)

        /**
         * Find all mods in the weave mods folder
         */
        @Contract(pure = true)
        fun findEnabled(): MutableList<WeaveMod> {
            val list: MutableList<WeaveMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar") && file.isFile) {
                        list.add(WeaveMod(file))
                    }
                }
            }
            return list
        }

        fun findDisabled(): List<WeaveMod> {
            val list: MutableList<WeaveMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar.disabled") && file.isFile) {
                        list.add(WeaveMod(file))
                    }
                }
            }
            return list
        }


        fun findAll(): List<WeaveMod> {
            val list = findEnabled()
            list.addAll(findDisabled())
            return Collections.unmodifiableList(list)
        }


        fun add(file: File): WeaveMod? {
            val target = autoCopy(file, modFolder)
            return if ((target == null)) null else WeaveMod(target)
        }


        @get:Contract(" -> new")
        val installation: File
            get() = config.addon.weave.installationDir.toFile()



        fun checkUpdate(): Boolean {
            log.info("Updating Weave Loader")
            return downloadLoader(
                "Weave-MC/Weave-Loader",
                config.addon.weave.installationDir.toFile()
            )
        }
    }
}
