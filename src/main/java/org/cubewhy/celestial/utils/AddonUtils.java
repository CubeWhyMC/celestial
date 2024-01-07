/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public final class AddonUtils {
    private AddonUtils() {
    }

    public static boolean isWeaveMod(@NotNull File file) throws IOException {
        return isWeaveMod(new JarFile(file));
    }

    /**
     * Is weave mod
     *
     * @param jar file of the mod
     * @return yes or no
     */
    public static boolean isWeaveMod(@NotNull JarFile jar) {
        // find weave.mod.json
        return jar.getJarEntry("weave.mod.json") != null;
    }

    public static boolean isLunarCNMod(@NotNull File file) throws IOException {
        return isLunarCNMod(new JarFile(file));
    }

    /**
     * Is LunarCN mod
     *
     * @param jar file of the mod
     * @return yes or no
     */
    public static boolean isLunarCNMod(@NotNull JarFile jar) {
        // find lunarcn.mod.json
        return jar.getJarEntry("lunarcn.mod.json") != null;
    }
}
