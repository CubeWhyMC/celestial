/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event.impl

import org.cubewhy.celestial.event.Event
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class AuthEvent(@JvmField val authURL: URL) : Event() {
    private var result = ""
    private val responded = AtomicBoolean(false)

    fun waitForAuth(): String {
        while (!responded.get()) {
            Thread.onSpinWait()
        }
        return result
    }

    fun put(url: String?) {
        if (url != null) {
            this.result = url
        }
        responded.set(true)
    }
}
