/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui.dialogs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.cubewhy.celestial.Celestial.f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class ArgsConfigDialog(private val key: String, private val json: JsonObject) : JDialog() {
    private val array: JsonArray = json.getAsJsonArray(key)

    init {
        this.layout = BorderLayout()
        this.initGui()
        this.setSize(600, 600)
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.isLocationByPlatform = true
    }

    private fun initGui() {
        this.title = f.getString("gui.settings.args.title")

        val model = DefaultListModel<String>()
        val args = JList(model)
        // load args from config
        if (array.isEmpty) {
            // TODO remove this label
            this.add(JLabel("Looks nothing here, try to add something?"))
        } else {
            for (element in array) {
                model.addElement(element.asString)
            }
        }
        this.add(args, BorderLayout.CENTER)
        val panelButtons = JPanel()
        panelButtons.layout = GridLayout(1, 2)
        // btnAdd
        val btnAdd: JButton = JButton(f.getString("gui.settings.args.add"))
        btnAdd.addActionListener {
            val arg = JOptionPane.showInputDialog(this, f.getString("gui.settings.args.add.message"))
            if (arg != null) {
                this.addArg(arg, model)
            }
        }
        // btnRemove
        val btnRemove: JButton = JButton(f.getString("gui.settings.args.remove"))
        btnRemove.addActionListener {
            val index = args.selectedIndex
            if (index == -1 || JOptionPane.showConfirmDialog(
                    this,
                    String.format(f.getString("gui.settings.args.remove.confirm"), args.selectedValue),
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.NO_OPTION
            ) {
                return@addActionListener
            }
            this.removeArg(index, model)
        }
        panelButtons.add(btnAdd)
        panelButtons.add(btnRemove)
        this.add(panelButtons, BorderLayout.SOUTH)
    }

    private fun addArg(arg: String, model: DefaultListModel<String>) {
        array.add(arg)
        model.addElement(arg)
        log.info("Add a arg to " + this.key + " -> " + arg)
    }

    private fun removeArg(index: Int, model: DefaultListModel<String>) {
        val arg = model[index]
        model.remove(index)
        array.remove(index)
        log.info("Remove a arg to " + this.key + " -> " + arg)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ArgsConfigDialog::class.java)
    }
}
