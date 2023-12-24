/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Response;
import org.cubewhy.celestial.utils.RequestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public final class MinecraftData {
    public static final URL versionManifest;

    static {
        try {
            versionManifest = new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private MinecraftData() {
    }

    public static JsonObject manifest() throws IOException {
        try (Response response = RequestUtils.get(versionManifest).execute()) {
            assert response.body() != null;
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }

    /**
     * Get information of a Minecraft release
     *
     * @param version version id
     * @return version json
     */
    public static @Nullable JsonObject getVersion(String version, @NotNull JsonElement json) throws IOException {
        JsonArray versionsArray = json.getAsJsonObject().getAsJsonArray("versions");
        for (JsonElement element : versionsArray) {
            if (element.getAsJsonObject().get("id").getAsString().equals(version)) {
                String url = element.getAsJsonObject().get("url").getAsString();
                try (Response response = RequestUtils.get(url).execute()) {
                    assert response.body() != null;
                    return JsonParser.parseString(response.body().string()).getAsJsonObject();
                }
            }
        }
        return null;
    }

    /**
     * Get texture index (Minecraft)
     *
     * @param json json object from MinecraftData.getVersion
     * @return json of texture index
     * */
    public static JsonObject getTextureIndex(@NotNull JsonElement json) throws IOException{
        URL url = new URL(json.getAsJsonObject().getAsJsonObject("assetIndex").get("url").getAsString());
        try (Response response = RequestUtils.get(url).execute()) {
            assert response.body() != null;
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }
}
