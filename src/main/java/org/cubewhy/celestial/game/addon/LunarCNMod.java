/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import lombok.Getter;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.game.BaseAddon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class LunarCNMod implements BaseAddon {
    public static final File modFolder = new File(Celestial.configDir, "mods");
    private final File file;

    public LunarCNMod(File file) {
        this.file = file;
    }

    /**
     * Find all mods in the lunarcn mods folder
     * */
    @NotNull
    @Contract(pure = true)
    public static List<LunarCNMod> findAll() {
        List<LunarCNMod> list = new ArrayList<>();
        if (modFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(modFolder.listFiles())) {
                if (file.getName().endsWith(".jar") && file.isFile()) {
                    list.add(new LunarCNMod(file));
                }
            }
        }
        return list;
    }
}
