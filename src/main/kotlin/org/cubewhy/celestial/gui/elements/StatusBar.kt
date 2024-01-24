/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui.elements

import java.awt.event.ActionEvent
import javax.swing.JLabel
import javax.swing.Timer

class StatusBar : JLabel() {
    private val autoClearTimer = Timer(10000) { _: ActionEvent? ->
        this.clear()
    }

    fun clear() {
        this.text = ""
    }

    override fun setText(text: String?) {
        if (this.text.isNotEmpty()) {
            autoClearTimer.stop()
        }
        super.setText(text)
        if (!text.isNullOrEmpty()) {
            autoClearTimer.start()
        }
    }
}
