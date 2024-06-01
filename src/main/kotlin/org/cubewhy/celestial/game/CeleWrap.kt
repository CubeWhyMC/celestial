/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game

import co.gongzh.procbridge.IDelegate
import co.gongzh.procbridge.Server
import com.google.gson.JsonObject
import org.cubewhy.celestial.event.impl.AuthEvent
import org.cubewhy.celestial.utils.GitUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import kotlin.system.exitProcess

// celewrap: https:/github.com/CubeWhyMC/celewrap

// Please notice that: Moonsworth changed its auth method since 2024/4, AuthServer is now deprecated
class AuthServer private constructor() {
    private val server: Server = Server(28189, IDelegate { method: String?, args: Any? ->
        try {
            return@IDelegate if (method != null) handleRequest(method, args) else null
        } catch (e: Exception) {
            log.error("Failed to start the auth server")
            log.error(e.stackTraceToString())
        }
        null
    })

    /**
     * Start the server
     */
    fun startServer() {
        log.info("Starting auth server at port 28189")
        Thread { server.start() }.start()
    }

    /**
     * Handles LunarClient requests
     *
     * @return json of callbackInfo
     */
    private fun handleRequest(method: String, args: Any?): Map<String, String> {
        val result = HashMap<String, String>()
        log.info("Received request! Method: $method")
        if (method == "open-window" && args is JsonObject) {
            // Old auth function
            // Pop a token url
            val url = URL(args.get("url").asString)
            val auth = (AuthEvent(url).call() as AuthEvent).waitForAuth()
            if (auth.isBlank()) {
                result["status"] = "CLOSED_WITH_NO_URL"
            } else {
                result["status"] = "MATCHED_TARGET_URL"
                result["url"] = auth
            }
        }
        return result
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AuthServer::class.java)
        val instance: AuthServer = AuthServer()
    }
}
