/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event.impl

import org.cubewhy.celestial.event.Event
import java.io.File

class FileDownloadEvent(val file: File, val type: Type) : Event() {
    enum class Type {
        START,
        SUCCESS, FAILURE
    }
}
