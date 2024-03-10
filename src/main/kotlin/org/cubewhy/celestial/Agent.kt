/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial

import java.lang.instrument.Instrumentation

fun premain(arg: String?, inst: Instrumentation) {

    patch(arg, inst)
}

fun agentmain(arg: String?, inst: Instrumentation) {
    patch(arg, inst)
}

private fun patch(arg: String?, inst: Instrumentation) {
    println("Celestial patcher, WIP!")
}