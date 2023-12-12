package org.cubewhy.celestial.utils.lunar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Response;
import org.cubewhy.celestial.utils.RequestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

public final class SiteData {
    public final URI api;

    /**
     * Create a SiteData instance with special API resource
     *
     * @param api Launcher API
     */
    public SiteData(URI api) {
        super();
        this.api = api;
    }

    /**
     * Create a SiteData instance with the official Launcher API
     */
    public SiteData() {
        this(URI.create("https://api.lunarclientprod.com")); // official API
    }

    public JsonObject metadata() throws IOException {
        try (Response response = RequestUtils.get(api + "/site/metadata").execute()) {
            assert response.code() == 200 : "Code = " + response.code(); // check success
            assert response.body() != null : "ResponseBody was null";
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }

    @Nullable
    public String getAlert(@NotNull JsonObject metadata) {
        return metadata.get("alert").getAsString();
    }

    public int getPLayersInGame(JsonObject metadata) {
        return metadata.getAsJsonObject("statistics").get("game").getAsInt();
    }

    public int getPLayersInLauncher(JsonObject metadata) {
        return metadata.getAsJsonObject("statistics").get("launcher").getAsInt();
    }
}
