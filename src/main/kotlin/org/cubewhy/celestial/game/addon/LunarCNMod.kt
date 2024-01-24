/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game.addon

import org.cubewhy.celestial.Celestial
import org.cubewhy.celestial.Celestial.config
import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.game.BaseAddon
import org.cubewhy.celestial.gui.GuiLauncher
import org.cubewhy.celestial.utils.AddonUtils.downloadLoader
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.util.*

class LunarCNMod(@JvmField val file: File) : BaseAddon() {
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
        val modFolder: File = File(Celestial.configDir, "mods")
        private val log: Logger = LoggerFactory.getLogger(LunarCNMod::class.java)

        init {
            if (modFolder.mkdirs()) {
                log.info("Making lunarCN mods folder")
            }
        }

        /**
         * Find all mods in the lunarcn mods folder
         */
        @Contract(pure = true)
        fun findEnabled(): MutableList<LunarCNMod> {
            val list: MutableList<LunarCNMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar") && file.isFile) {
                        list.add(LunarCNMod(file))
                    }
                }
            }
            return list
        }

        fun findDisabled(): List<LunarCNMod> {
            val list: MutableList<LunarCNMod> = ArrayList()
            if (modFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(modFolder.listFiles())) {
                    if (file.name.endsWith(".jar.disabled") && file.isFile) {
                        list.add(LunarCNMod(file))
                    }
                }
            }
            return list
        }

        @JvmStatic
        fun findAll(): List<LunarCNMod> {
            val list = findEnabled()
            list.addAll(findDisabled())
            return Collections.unmodifiableList(list)
        }

        @JvmStatic
        @get:Contract(" -> new")
        val installation: File
            get() = File(
                config.getValue("addon").asJsonObject.get("lunarcn").asJsonObject.get("installation").asString
            )

        @JvmStatic
        @Throws(IOException::class)
        fun add(file: File?): LunarCNMod? {
            val target = autoCopy(file!!, modFolder)
            return if ((target == null)) null else LunarCNMod(target)
        }

        @JvmStatic
        @Throws(MalformedURLException::class)
        fun checkUpdate(): Boolean {
            log.info("Updating LunarCN Loader...")
            GuiLauncher.statusBar.text = f.getString("gui.addon.mods.cn.warn")
            return downloadLoader(
                "CubeWhyMC/LunarClient-CN",
                File(
                    config.getValue("addon").asJsonObject.getAsJsonObject("lunarcn").get("installation")
                        .asString
                )
            )
        }
    }
}
