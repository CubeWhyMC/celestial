/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event

object EventPriority {
    const val FIRST: Byte = 0
    const val SECOND: Byte = 1
    const val THIRD: Byte = 2
    const val FOURTH: Byte = 3
    const val FIFTH: Byte = 4

    val valueArray: ByteArray = byteArrayOf(FIRST, SECOND, THIRD, FOURTH, FIFTH)
}
