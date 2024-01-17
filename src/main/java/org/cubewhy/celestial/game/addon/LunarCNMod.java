/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.game.BaseAddon;
import org.cubewhy.celestial.utils.AddonUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.cubewhy.celestial.Celestial.config;
import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.gui.GuiLauncher.statusBar;

@Getter
@Slf4j
public class LunarCNMod extends BaseAddon {
    public static final File modFolder = new File(Celestial.configDir, "mods");
    private final File file;

    static {
        if (modFolder.mkdirs()) {
            log.info("Making lunarCN mods folder");
        }
    }

    public LunarCNMod(File file) {
        this.file = file;
    }

    /**
     * Find all mods in the lunarcn mods folder
     */
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

    @Contract(" -> new")
    public static @NotNull File getInstallation() {
        return new File(config.getValue("addon").getAsJsonObject().get("lunarcn").getAsJsonObject().get("installation").getAsString());
    }

    public static @Nullable LunarCNMod add(File file) throws IOException {
        File target = autoCopy(file, modFolder);
        return (target == null) ? null : new LunarCNMod(target);
    }

    public static boolean checkUpdate() throws MalformedURLException {
        log.info("Updating LunarCN Loader...");
        statusBar.setText(f.getString("gui.addon.mods.cn.warn"));
        return AddonUtils.downloadLoader("CubeWhyMC/LunarClient-CN", new File(config.getValue("addon").getAsJsonObject().getAsJsonObject("lunarcn").get("installation").getAsString()));
    }

    @Override
    public String toString() {
        return this.file.getName();
    }
}
