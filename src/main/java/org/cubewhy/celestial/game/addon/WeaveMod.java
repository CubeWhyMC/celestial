/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.cubewhy.celestial.entities.Assets;
import org.cubewhy.celestial.entities.ReleaseEntity;
import org.cubewhy.celestial.files.DownloadManager;
import org.cubewhy.celestial.files.Downloadable;
import org.cubewhy.celestial.game.BaseAddon;
import org.cubewhy.celestial.utils.AddonUtils;
import org.cubewhy.celestial.utils.RequestUtils;
import org.cubewhy.celestial.utils.TextUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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

    public static @Nullable WeaveMod add(@NotNull File file) throws IOException {
        File target = autoCopy(file, modFolder);
        return (target == null) ? null : new WeaveMod(target);
    }

    public static File getInstallation() {
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
}
