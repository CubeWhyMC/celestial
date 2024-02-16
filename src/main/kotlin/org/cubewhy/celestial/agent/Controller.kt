/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.agent

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.InetSocketAddress

class Server: WebSocketServer(InetSocketAddress(28289)) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(Server::class.java)
    }

    override fun onOpen(p0: WebSocket?, p1: ClientHandshake?) {
        TODO("Not yet implemented")
    }

    override fun onClose(p0: WebSocket?, p1: Int, p2: String?, p3: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onMessage(p0: WebSocket?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun onError(p0: WebSocket?, p1: Exception?) {
        TODO("Not yet implemented")
    }

    override fun onStart() {
        log.info("Starting WS Server")
    }
}