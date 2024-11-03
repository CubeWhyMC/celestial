/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl

import org.cubewhy.celestial.event.Event

class ChangeConfigEvent<T>(val configObject: Any, val key: String, val newValue: T?, val oldValue: T? = null) : Event()