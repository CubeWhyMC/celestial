/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public abstract class BaseAddon {


    protected static @Nullable File autoCopy(@NotNull File file, File folder) throws IOException {
        String name = file.getName();
        if (!name.endsWith(".jar")) {
            name += ".jar"; // adds an ends with for the file
        }
        File target = new File(folder, name);
        if (target.exists()) {
            return null;
        }
        FileUtils.copyFile(file, target);
        return target;
    }

    public abstract boolean isEnabled();

    /**
     * Toggle state
     *
     * @return true equals Enable, false equals Disable
     */
    public abstract boolean toggle();

    protected boolean toggle0(File file) {
        if (isEnabled()) {
            return !file.renameTo(new File(file.getPath() + ".disabled"));
        }
        return file.renameTo(new File(file.getPath().substring(0, file.getPath().length() - 9)));
    }
}
