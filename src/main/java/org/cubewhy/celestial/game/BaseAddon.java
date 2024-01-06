/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class BaseAddon {
    @Contract(pure = true)
    public static List<? extends BaseAddon> findAll() {
        return null;
    }

    protected static File autoCopy(File file, File folder) throws IOException {
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
}
