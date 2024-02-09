/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event

import java.lang.reflect.Method

class EventData(val source: Any, val target: Method, val priority: Byte)
