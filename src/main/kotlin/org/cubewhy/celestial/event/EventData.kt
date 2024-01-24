/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event

import java.lang.reflect.Method

class EventData(@JvmField val source: Any, @JvmField val target: Method, @JvmField val priority: Byte)
