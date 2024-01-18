/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.game.BaseAddon;
import org.cubewhy.celestial.utils.AddonUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.cubewhy.celestial.Celestial.config;

@Getter
@Slf4j
public class WeaveMod extends BaseAddon {
    public static final String build = "Weave-MC/Weave-Loader"; // https://github.com/<build>/releases/latest
    public static final File modFolder = new File(System.getProperty("user.home"), ".weave/mods");
    private final File file;

    public WeaveMod(File file) {
        this.file = file;
    }

    /**
     * Find all mods in the weave mods folder
     */
    @NotNull
    @Contract(pure = true)
    public static List<WeaveMod> findEnabled() {
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

    public static @NotNull List<WeaveMod> findDisabled() {
        List<WeaveMod> list = new ArrayList<>();
        if (modFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(modFolder.listFiles())) {
                if (file.getName().endsWith(".jar.disabled") && file.isFile()) {
                    list.add(new WeaveMod(file));
                }
            }
        }
        return list;
    }

    public static @NotNull List<WeaveMod> findAll() {
        List<WeaveMod> list = findEnabled();
        list.addAll(findDisabled());
        return Collections.unmodifiableList(list);
    }

    public static @Nullable WeaveMod add(@NotNull File file) throws IOException {
        File target = autoCopy(file, modFolder);
        return (target == null) ? null : new WeaveMod(target);
    }

    @Contract(" -> new")
    public static @NotNull File getInstallation() {
        return new File(config.getValue("addon").getAsJsonObject().get("weave").getAsJsonObject().get("installation").getAsString());
    }

    @Override
    public String toString() {
        return this.file.getName();
    }

    public static boolean checkUpdate() throws MalformedURLException {
        log.info("Updating Weave Loader");
        return AddonUtils.downloadLoader("Weave-MC/Weave-Loader", new File(config.getValue("addon").getAsJsonObject().getAsJsonObject("weave").get("installation").getAsString()));
    }

    @Override
    public boolean isEnabled() {
        return file.getName().endsWith(".jar");
    }

    @Override
    public boolean toggle() {
        return toggle0(file);
    }
}
