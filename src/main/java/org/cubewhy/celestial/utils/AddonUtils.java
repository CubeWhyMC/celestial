/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import okhttp3.Response;
import org.cubewhy.celestial.entities.Assets;
import org.cubewhy.celestial.entities.ReleaseEntity;
import org.cubewhy.celestial.files.DownloadManager;
import org.cubewhy.celestial.files.Downloadable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;

import static org.cubewhy.celestial.Celestial.config;
import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.files.Downloadable.Type.SHA256;

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

    public static boolean downloadLoader(String repo, File file) throws MalformedURLException {
        String apiJson;
        try (Response response = RequestUtils.get(String.format("https://%s/repos/%s/releases/latest", f.getString("url.github.api"), repo)).execute()) {
            assert response.body() != null;
            apiJson = response.body().string();
        } catch (Exception e) {
            return false;
        }
        ReleaseEntity releaseEntity = TextUtils.jsonToObj(apiJson, ReleaseEntity.class);
        String hash = null;
        URL loader = null;
        if (releaseEntity != null) {
            Assets[] assetsArray = releaseEntity.getAssets().toArray(new Assets[0]);
            for (Assets assets : assetsArray) {
                URL url = new URL(assets.getBrowser_download_url());
                url = new URL("https", f.getString("url.github"), 443, url.getPath());
                if (assets.getName().endsWith(".jar")) {
                    loader = url;
                }
                if (assets.getName().endsWith(".sha256")) {
                    try (Response response = RequestUtils.get(url).execute()) {
                        assert response.body() != null;
                        hash = response.body().string().split(" ")[0];
                    } catch (Exception ignored) {
                        // it's OK to be null
                    }
                }
            }
        }
        if (loader == null) {
            return false;
        }
        // send download
        DownloadManager.download(new Downloadable(loader, file, hash, SHA256));
        return true;
    }
}
