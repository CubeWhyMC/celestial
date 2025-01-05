/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs

import org.cubewhy.celestial.LauncherPage
import org.cubewhy.celestial.config
import org.cubewhy.celestial.f
import org.cubewhy.celestial.gui.elements.GuiDraggableList
import org.cubewhy.celestial.withScroller
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JDialog
import javax.swing.JPanel

class SortPagesDialog : JDialog() {
    private val log: Logger = LoggerFactory.getLogger(SortPagesDialog::class.java)
    private val panel = JPanel()

    init {
        this.title = f.getString("gui.settings.pages.title")
        this.setSize(600, 600)
        this.layout = BorderLayout()
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.isLocationByPlatform = true
        this.initGui()
    }

    private fun initGui() {
        // todo save pages, translate
        val draggableList = GuiDraggableList<LauncherPage>()
        val model = draggableList.model as DefaultListModel<LauncherPage>
        model.addAll(config.pages)
        panel.add(draggableList)
        this.add(this.panel.withScroller())
    }
}