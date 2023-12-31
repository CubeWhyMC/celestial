/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import lombok.Getter;
import org.cubewhy.celestial.game.BaseAddon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class WeaveMod implements BaseAddon {
    public static final File modFolder = new File(System.getProperty("user.home"), ".weave/mods");
    private final File file;

    public WeaveMod(File file) {
        this.file = file;
    }

    /**
     * Find all mods in the lunarcn mods folder
     */
    @NotNull
    @Contract(pure = true)
    public static List<WeaveMod> findAll() {
        List<WeaveMod> list = new ArrayList<>();
        if (modFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(modFolder.listFiles())) {
                if (file.getName().endsWith(".jar") && file.isFile()) {
                    list.add(new WeaveMod(file));
                }
            }
        }
        return list;
    }
}