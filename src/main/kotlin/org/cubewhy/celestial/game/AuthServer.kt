/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game

import co.gongzh.procbridge.IDelegate
import co.gongzh.procbridge.Server
import org.cubewhy.celestial.event.impl.AuthEvent
import org.cubewhy.celestial.utils.TextUtils.dumpTrace
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.net.URL

class AuthServer private constructor() {
    private val server: Server = Server(28189, IDelegate { method: String?, args: Any? ->
        try {
            return@IDelegate if (method != null) handleRequest(method, args) else null
        } catch (e: Exception) {
            log.error("Failed to start the auth server")
            log.error(dumpTrace(e))
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
    @Throws(MalformedURLException::class)
    private fun handleRequest(method: String, args: Any?): Map<String, String> {
        val result = HashMap<String, String>()
        if (method == "open-window" && args is JSONObject) {
            // Pop a token url
            val url = URL(args.getString("url"))
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
