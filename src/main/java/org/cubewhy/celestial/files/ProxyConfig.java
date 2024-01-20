/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.files;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlQuery;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ProxyConfig extends ConfigFile {

    @Getter
    public static class Mirror {

        private final String host;
        private final int port;

        public Mirror(@NotNull String address) {
            host = address.split(":")[0];
            port = Integer.parseInt(address.split(":")[1]);
        }
    }

    public ProxyConfig(File file) {
        super(file);
        this.initValue("state", new JsonPrimitive(false));
        this.initValue("proxy", "http://127.0.0.1:8080");
        this.initValue("mirror", new JsonObject());
    }

    public ProxyConfig useProxy(@NotNull URL address) {
        this.setValue("proxy", address.toString());
        this.save();
        return this;
    }
    
    public ProxyConfig setState(boolean state) {
        this.setValue("state", state);
        return this;
    }
    
    public boolean getState() {
        return this.getValue("state").getAsBoolean();
    }

    public Proxy getProxy() throws MalformedURLException {
        URL address = new URL(this.getProxyAddress());
        return new Proxy(getType(address.getProtocol()), new InetSocketAddress(address.getHost(), address.getPort()));
    }

    public String getProxyAddress() {
        return this.getValue("proxy").getAsString();
    }

    private Proxy.Type getType(@NotNull String protocol) {
        return switch (protocol) {
            case "http" -> Proxy.Type.HTTP;
            case "socks" -> Proxy.Type.SOCKS;
            default -> throw new IllegalStateException("Unexpected value: " + protocol);
        };
    }

    public URL useMirror(@NotNull URL src) throws MalformedURLException, URISyntaxException {
        String host = src.getHost();
        int port = src.getPort();
        if (port == -1) {
            port = src.getDefaultPort();
        }
        String completed = host + ":" + port;
        if (this.getConfig().getAsJsonObject("mirror").has(completed)) {
            Mirror mirror = getMirror(completed);
            return new URL(src.getProtocol(), mirror.host, mirror.port, src.getFile());
        }
        return src;
    }

    public Mirror getMirror(String address) {
        JsonObject json = getValue("mirror").getAsJsonObject();
        return new Mirror(json.get(address).getAsString());
    }
}
