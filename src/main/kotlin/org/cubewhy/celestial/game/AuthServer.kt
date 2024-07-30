/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game

import co.gongzh.procbridge.IDelegate
import co.gongzh.procbridge.Server
import com.google.gson.JsonObject
import launcher.Mslogin
import launcher.Mslogin.GameRequest
import launcher.Mslogin.LoginResponse
import org.cubewhy.celestial.event.impl.AuthEvent
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer

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

class NewAuthServer private constructor() : WebSocketServer(InetSocketAddress("127.0.0.1", 28190)) {
    companion object {
        private val log = LoggerFactory.getLogger(NewAuthServer::class.java)
        val instance = NewAuthServer()
    }

    override fun onOpen(p0: WebSocket?, p1: ClientHandshake?) {
        log.debug("Open.")
    }

    override fun onClose(p0: WebSocket?, p1: Int, p2: String?, p3: Boolean) {
        log.debug("Closed.")
    }

    override fun onMessage(p0: WebSocket?, p1: String?) {
    }

    override fun onMessage(conn: WebSocket, message: ByteBuffer) {
        val request = GameRequest.parseFrom(message)
        if (request.method == "OpenMicrosoftPopup") {
            log.info("Client request login")
            val responseUrl =
                (AuthEvent(authURL = URL("https://login.live.com/oauth20_authorize.srf?client_id=00000000402b5328&response_type=code&scope=service::user.auth.xboxlive.com::MBI_SSL&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf")).call() as AuthEvent).waitForAuth()
            conn.send(
                LoginResponse.newBuilder()
                    .setAuth(
                        Mslogin.Auth.newBuilder()
                            .setUrl(responseUrl)
                            .build()
                    ).build().toByteArray()
            )
        }
    }

    override fun onError(p0: WebSocket?, p1: Exception?) {
        p1?.let {
            log.error(it.stackTraceToString())
        }
    }

    override fun onStart() {
        log.info("Lunar auth server started at port $port")
    }
}