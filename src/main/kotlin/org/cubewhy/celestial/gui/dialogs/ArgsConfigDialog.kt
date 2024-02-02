/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui.dialogs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.withScroller
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.JTextArea

class ArgsConfigDialog(private val key: String, json: JsonObject) : JDialog() {
    private val array: JsonArray = json.getAsJsonArray(key)

    private var input: JTextArea

    init {
        this.layout = BorderLayout()
        this.setSize(600, 600)
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.isLocationByPlatform = true

        this.title = f.getString("gui.settings.args.title")
        input = JTextArea(array.toSplitArgs())
        this.add(input.withScroller())

        this.addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                saveArgs()
            }
        })
    }

    private fun saveArgs() {
        log.info("Save args")
        val lines = input.text.split("\n")
        array.removeAll {
            true
        }
        array.addAll(lines.asJsonArray())
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ArgsConfigDialog::class.java)
    }
}

private fun <E> List<E>.asJsonArray(): JsonArray {
    val array = JsonArray()
    this.forEach {
        val string = it.toString()
        if (string.isNotBlank()) array.add(string)
    }
    return array
}

private fun JsonArray.toSplitArgs(): String {
    val sb = StringBuilder()
    for (element in this) if (element.asString.isNotBlank()) sb.append(element.asString).append("\n")
    return sb.toString()
}
