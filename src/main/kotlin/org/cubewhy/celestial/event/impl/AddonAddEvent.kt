/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event.impl

import org.cubewhy.celestial.event.Event
import org.cubewhy.celestial.game.BaseAddon

class AddonAddEvent(val type: Type, val addon: BaseAddon) : Event() {
    enum class Type {
        JAVAAGENT,
        WEAVE,
        LUNARCN, FABRIC
    }
}
