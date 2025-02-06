/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game

import org.apache.commons.io.FileUtils
import java.io.File

abstract class BaseAddon {
    abstract val isEnabled: Boolean

    /**
     * Toggle state
     *
     * @return true equals Enable, false equals Disable
     */
    abstract fun toggle(): Boolean

    protected fun toggle(file: File): Boolean {
        if (isEnabled) {
            return !file.renameTo(File(file.path + ".disabled"))
        }
        return file.renameTo(File(file.path.substring(0, file.path.length - 9)))
    }

    companion object {
        @JvmStatic
        protected fun autoCopy(file: File, folder: File?): File? {
            var name = file.name
            if (!name.endsWith(".jar")) {
                name += ".jar" // adds an ends with for the file
            }
            val target = File(folder, name)
            if (target.exists()) {
                return null
            }
            FileUtils.copyFile(file, target)
            return target
        }
    }
}
