/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs

import org.cubewhy.celestial.readOnly
import javax.swing.JDialog
import javax.swing.JTextArea

class LogsDialog(gaveTitle: String) : JDialog() {
    private val area = JTextArea().readOnly()
    private val sb = StringBuilder()

    init {
        this.title = gaveTitle
        this.setSize(600, 600)
        this.isLocationByPlatform = true
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.contentPane = area
    }

    fun addMessage(msg: String) {
        sb.append(msg).append("\n")
        area.text = sb.toString()
    }
}