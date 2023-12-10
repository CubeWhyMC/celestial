package org.cubewhy.celestial.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import okhttp3.*;

import java.io.IOException;

public final class RequestUtils {
    public static final OkHttpClient httpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json");


    public static Call get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        return httpClient.newCall(request);
    }

    public static Call request(Request request) {
        return httpClient.newCall(request);
    }

    public static Call post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON); // MUST be JSON in the latest LC-API
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return httpClient.newCall(request);
    }

    public static Call post(String url, JsonElement json) throws IOException {
        Gson gson = new Gson();
        String realJson = gson.toJson(json);
        return post(url, realJson);
    }


    /**
     * Download
     *
     * @param url target url
     * @return bytes
     */
    public static byte[] download(String url) throws IOException {
        try (Response response = get(url).execute()) {
            if (response.body() != null) {
                return response.body().bytes();
            }
        }
        return null;
    }
}
