/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game.addon

import org.cubewhy.celestial.Celestial.config
import org.cubewhy.celestial.game.BaseAddon
import org.cubewhy.celestial.utils.AddonUtils.downloadLoader
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.util.*

class WeaveMod(@JvmField val file: File) : BaseAddon() {
    override fun toString(): String {
        return file.name
    }

    override val isEnabled: Boolean
        get() = file.name.endsWith(".jar")

    override fun toggle(): Boolean {
        return toggle0(file)
    }

    companion object {

        @JvmField
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

        @JvmStatic
        fun findAll(): List<WeaveMod> {
            val list = findEnabled()
            list.addAll(findDisabled())
            return Collections.unmodifiableList(list)
        }

        @JvmStatic
        
        fun add(file: File): WeaveMod? {
            val target = autoCopy(file, modFolder)
            return if ((target == null)) null else WeaveMod(target)
        }

        @JvmStatic
        @get:Contract(" -> new")
        val installation: File
            get() = File(
                config.getValue("addon").asJsonObject.get("weave").asJsonObject.get("installation")
                    .asString
            )

        @JvmStatic

        fun checkUpdate(): Boolean {
            log.info("Updating Weave Loader")
            return downloadLoader(
                "Weave-MC/Weave-Loader",
                File(
                    config.getValue("addon").asJsonObject.getAsJsonObject("weave").get("installation")
                        .asString
                )
            )
        }
    }
}
