/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui.dialogs

import org.cubewhy.celestial.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.JTextArea

class ArgsConfigDialog(private val key: String, private val obj3ct: Any) : JDialog() {
    private val array: ArrayList<String> = obj3ct.getKotlinField(key)

    private var input: JTextArea

    init {
        this.layout = BorderLayout()
        this.setSize(600, 600)
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.isLocationByPlatform = true

        this.title = f.getString("gui.settings.args.title")
        input = array.toArgsString().toJTextArea()
        this.add(f.getString("gui.settings.args.tip").toJLabel(), BorderLayout.SOUTH)
        this.add(input.withScroller(), BorderLayout.CENTER)

        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                saveArgs()
            }
        })
    }

    private fun saveArgs() {
        log.info("Save args")
        val lines = input.text
            .split(" ", "\n")
            .filter { it.isNotBlank() }
        array.removeAll { true }
        array.addAll(lines)
        obj3ct.setKotlinField(key, array)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ArgsConfigDialog::class.java)
    }
}

private fun ArrayList<String>.toArgsString(): String {
    val sb = StringBuilder()
    for (element in this) if (element.isNotBlank()) sb.append(element).append("\n")
    return sb.toString()
}
