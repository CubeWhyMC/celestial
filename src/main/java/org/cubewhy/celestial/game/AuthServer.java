/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game;

import co.gongzh.procbridge.Server;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.event.impl.AuthEvent;
import org.cubewhy.celestial.utils.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuthServer {
    public final Server server = new Server(28189, (method, args) -> {
        try {
            return method != null ? handleRequest(method, args) : null;
        } catch (Exception e) {

            log.info(TextUtils.dumpTrace(e));
        }
        return null;
    });
    @Getter
    private static final AuthServer instance = new AuthServer();

    private AuthServer() {
    }

    /**
     * Start the server
     */
    public void startServer() {
        log.info("Starting auth server at port 28189");
        new Thread(server::start).start();
    }

    /**
     * Handles LunarClient requests
     *
     * @return json of callbackInfo
     */
    @NotNull
    private Map<String, String> handleRequest(@NotNull String method, Object args) throws MalformedURLException {
        HashMap<String, String> result = new HashMap<>();
        if (method.equals("open-window") && args instanceof JSONObject args1) {
            // Pop a token url
            URL url = new URL(args1.getString("url"));
            String auth = ((AuthEvent) new AuthEvent(url).call()).waitForAuth();
            if (auth.isBlank()) {
                result.put("status", "CLOSED_WITH_NO_URL");
            } else {
                result.put("status", "MATCHED_TARGET_URL");
                result.put("url", auth);
            }
        }
        return result;
    }
}
