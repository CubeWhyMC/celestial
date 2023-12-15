package org.cubewhy.celestial.utils.lunar;

import com.google.gson.*;
import okhttp3.Response;
import org.cubewhy.celestial.utils.OSEnum;
import org.cubewhy.celestial.utils.RequestUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public final class LauncherData {
    public final URI api;

    /**
     * Create a LauncherData instance with special API resource
     *
     * @param api Launcher API
     */
    public LauncherData(URI api) {
        super();
        this.api = api;
    }

    /**
     * Create a LauncherData instance with the official Launcher API
     */
    public LauncherData() {
        this(URI.create("https://api.lunarclientprod.com")); // official API
    }

    /**
     * Create a LauncherData instance with special API resource
     *
     * @param api Launcher API
     */
    public LauncherData(String api) {
        this(URI.create(api));
    }

    /**
     * Create a LauncherData instance with special API resource
     *
     * @param api Launcher API
     */
    public LauncherData(@NotNull URL api) throws URISyntaxException {
        this(api.toURI());
    }

    /**
     * Get metadata
     *
     * @return Launcher Metadata
     */
    public JsonObject metadata() throws IOException {
        // do request with fake system info
        // TODO fix the launcher_version
        try (Response response = RequestUtils.get(api + "/launcher/metadata" + "?os=linux" + "&arch=x64" + "&launcher_version=v3.1.3-master").execute()) {
            assert response.code() == 200 : "Code = " + response.code(); // check success
            assert response.body() != null : "ResponseBody was null";
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }

    /**
     * Get alert message
     *
     * @param metadata metadata from api
     * @return a map of the alert (title, message)
     * */
    public static Map<String, String> getAlert(JsonObject metadata) {
        if (metadata.has("alert") && !metadata.get("alert").isJsonNull()) {
            JsonObject alert = metadata.getAsJsonObject("alert");
            Map<String, String> map = new HashMap<>();
            map.put("title", alert.get("name").getAsString());
            map.put("message", alert.get("text").getAsString());
            return map;
        } else {
            return null;
        }
    }

    /**
     * Get blog posts
     *
     * @param metadata metadata from api
     * @return a list of blog posts
     */
    public static JsonArray getBlogPosts(JsonObject metadata) {
        return metadata.getAsJsonArray("blogPosts");
    }

    public JsonObject getVersion(String version, String branch, String module) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("hwid", "HWID-PUBLIC");
        json.addProperty("installation_id", "INSTALLATION_ID");
        json.addProperty("os", Objects.requireNonNull(OSEnum.find(System.getProperty("os.name"))).jsName); // shit js
        json.addProperty("arch", "x64"); // example: x64
        json.addProperty("os_release", "19045.3086");
        json.addProperty("launcher_version", "2.15.1");
        json.addProperty("launch_type", "offline");
        json.addProperty("version", version);
        json.addProperty("branch", branch);
        json.addProperty("module", module);

        try (Response response = RequestUtils.post(api + "/launcher/launch", new Gson().toJson(json)).execute()) {
            assert response.body() != null : "ResponseBody was null";
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }

    /**
     * Get support versions
     *
     * @param metadata LC metadata
     * @return Support versions list
     */
    public static List<String> getSupportVersions(JsonElement metadata) throws IOException {
        List<String> versions = new ArrayList<>();
        JsonArray versionsJson = Objects.requireNonNull(metadata).getAsJsonObject().getAsJsonArray("versions");
        for (JsonElement version : versionsJson) {
            String versionId = version.getAsJsonObject().get("id").getAsString();
            if (version.getAsJsonObject().has("subversions")) {
                for (JsonElement subVersion : version.getAsJsonObject().get("subversions").getAsJsonArray()) {
                    versions.add(subVersion.getAsJsonObject().get("id").getAsString());
                }
            } else {
                versions.add(versionId);
            }
        }
        return versions;
    }

    /**
     * Get information of a subversion
     *
     * @param version version name
     * @return version json
     */
    public static JsonObject getVersionInMetadata(JsonElement metadata, String version) throws IOException {
        JsonObject metadata1 = metadata.getAsJsonObject();
        for (JsonElement version1 : metadata1.get("versions").getAsJsonArray()) {
            if (version.contains(version1.getAsJsonObject().get("id").getAsString())) {
                for (JsonElement subVersion : version1.getAsJsonObject().get("subversions").getAsJsonArray()) {
                    if (subVersion.getAsJsonObject().get("id").getAsString().equals(version)) {
                        return subVersion.getAsJsonObject();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get support addons
     *
     * @param metadata LC metadata
     * @param version  Minecraft version
     * @return Module List
     */
    public static List<String> getSupportModules(JsonElement metadata, String version) throws IOException {
        List<String> modules = new ArrayList<>();
//        boolean isSubVersion = StringUtils.count(version, '.') >= 2;
        JsonObject version1 = getVersionInMetadata(metadata, version);
        JsonArray modulesJson = Objects.requireNonNull(version1).getAsJsonArray("modules");
        for (JsonElement moduleJson : modulesJson) {
            modules.add(moduleJson.getAsJsonObject().get("id").getAsString());
        }
        return modules;
    }

    /**
     * Get a list of LunarClient Artifacts
     *
     * @param version artifacts json
     * @return artifact list
     */

    public static Map<String, Map<String, String>> getArtifacts(JsonElement version) throws IOException {
        Map<String, Map<String, String>> out = new HashMap<>();

        JsonObject versionJson = Objects.requireNonNull(version).getAsJsonObject();
        JsonObject launchTypeData = versionJson.getAsJsonObject("launchTypeData");
        JsonArray artifacts = launchTypeData.getAsJsonArray("artifacts");

        for (JsonElement artifact : artifacts) {
            Map<String, String> info = new HashMap<>();
            String key = artifact.getAsJsonObject().get("name").getAsString();
            String url = artifact.getAsJsonObject().get("url").getAsString();
            String sha1 = artifact.getAsJsonObject().get("sha1").getAsString();
            String type = artifact.getAsJsonObject().get("type").getAsString();
            info.put("url", url);
            info.put("sha1", sha1);
            info.put("type", type);
            out.put(key, info);
        }

        return out;
    }

    /**
     * Get the textures' index of LunarClient
     *
     * @param version version info
     * @return textures' index
     */
    @Nullable
    public static JsonElement getLunarTexturesIndex(@NotNull JsonElement version) throws IOException {
        JsonObject versionJson = version.getAsJsonObject();
        String indexUrl = versionJson.getAsJsonObject("textures").get("indexUrl").getAsString();
        // get index json
        String baseUrl = getTexturesBaseUrl(version);
        try (Response response = RequestUtils.get(indexUrl).execute()) {
            if (response.body() != null) {
                // parse
                JsonObject jsonObject = new JsonObject();
                for (String s : response.body().string().split("\n")) {
                    // filename hashcode
                    jsonObject.addProperty(baseUrl + s.split(" ")[0], s.split(" ")[1]);
                }
                return jsonObject;
            }
        }
        return null;
    }

    @NotNull
    @Contract(pure = true)
    public static String getTexturesBaseUrl() {
        return "https://textures.lunarclientcdn.com/file/";
    }

    public static String getTexturesBaseUrl(JsonElement version) throws IOException {
        JsonObject versionJson = Objects.requireNonNull(version).getAsJsonObject();
        return versionJson.get("baseUrl").getAsString();
    }


}
