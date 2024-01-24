/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event

import org.cubewhy.celestial.utils.TextUtils.dumpTrace
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class Event {
    /**
     * Call a event
     *
     * @return Event
     */
    fun call(): Event {
        val dataList = EventManager.get(this.javaClass)

        if (dataList != null) {
            for (data in dataList) {
                try {
                    data.target.invoke(data.source, this)
                } catch (e: Exception) {
                    log.error(dumpTrace(e))
                }
            }
        }
        return this
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Event::class.java)
    }
}
