/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event.impl

import org.cubewhy.celestial.event.Event
import org.cubewhy.celestial.game.AddonType
import org.cubewhy.celestial.game.BaseAddon

class AddonAddEvent(val type: AddonType, val addon: BaseAddon) : Event() {
}
