/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.game.BaseAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FabricMod extends BaseAddon {
    public static final File modFolder = new File(Celestial.config.getValue("installation-dir").getAsString(), "mods");
    public final File file;

    public FabricMod(File file) {
        this.file = file;
    }


    public static @NotNull List<FabricMod> findAll() {
        List<FabricMod> list = new ArrayList<>();
        if (modFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(modFolder.listFiles())) {
                if (file.getName().endsWith(".jar") && file.isFile()) {
                    list.add(new FabricMod(file));
                }
            }
        }
        return list;
    }

    public static @Nullable FabricMod add(File file) throws IOException {
        File target = autoCopy(file, modFolder);
        return (target == null) ? null : new FabricMod(target);
    }

    @Override
    public String toString() {
        return this.file.getName();
    }
}
