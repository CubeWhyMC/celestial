/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event

import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class Event {
    private var canceled: Boolean = false

    /**
     * Call a event
     *
     * @return isCanceled
     */
    fun call(): Boolean {
        val dataList = EventManager.get(this.javaClass)

        if (dataList != null) {
            for (data in dataList) {
                try {
                    data.target.invoke(data.source, this)
                } catch (e: Exception) {
                    log.error(e.stackTraceToString())
                }
            }
        }
        return canceled
    }

    fun cancel() {
        this.canceled = true
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Event::class.java)
    }
}
