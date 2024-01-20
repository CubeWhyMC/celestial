package org.cubewhy.celestial.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.SneakyThrows;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

import static org.cubewhy.celestial.Celestial.proxy;

public final class RequestUtils {
    public static final OkHttpClient httpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json");


    @SneakyThrows
    public static @NotNull Call get(String url) {
        return get(new URL(url));
    }

    @SneakyThrows
    public static @NotNull Call get(URL url) {
        Request request = new Request.Builder()
                .url(proxy.useMirror(url))
                .build();

        return httpClient.newCall(request);
    }

    public static @NotNull Call request(Request request) {
        return httpClient.newCall(request);
    }

    public static @NotNull Call post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON); // MUST be JSON in the latest LC-API
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return httpClient.newCall(request);
    }

    public static @NotNull Call post(String url, JsonElement json) throws IOException {
        Gson gson = new Gson();
        String realJson = gson.toJson(json);
        return post(url, realJson);
    }
}
