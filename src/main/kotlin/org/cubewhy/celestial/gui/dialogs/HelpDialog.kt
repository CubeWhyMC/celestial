/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs

import org.cubewhy.celestial.f
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.elements.HelpPageX
import org.cubewhy.celestial.gui.elements.SearchableList
import org.cubewhy.celestial.utils.resolvePackage
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import javax.swing.DefaultListModel
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JPanel

class HelpDialog : JDialog() {
    init {
        this.title = f.getString("gui.help")
        this.layout = BorderLayout()
        this.size = Dimension(600, 600)
        this.isLocationByPlatform = true
        this.initGui()
    }

    private fun initGui() {
        // TODO Help dialog
        val panelDocument = JPanel()
        val layout = CardLayout()
        panelDocument.layout = layout

        val modelDocuments = DefaultListModel<HelpPageX>()
        val listDocuments = JList(modelDocuments)

        "org.cubewhy.celestial.gui.elements.help".resolvePackage(HelpPage::class.java).forEach {
            modelDocuments.addHelpPage(it.getDeclaredConstructor().newInstance())
        }

        for (page in modelDocuments.elements()) {
            panelDocument.add(page.name, page)
        }

        listDocuments.addListSelectionListener {
            val source = it.source as JList<HelpPageX>
            val page = source.selectedValue ?: return@addListSelectionListener
            layout.show(panelDocument, page.name)
        }

        this.add(SearchableList(modelDocuments, listDocuments), BorderLayout.NORTH)
        this.add(panelDocument, BorderLayout.CENTER)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

private fun DefaultListModel<HelpPageX>.addHelpPage(page: HelpPage) {
    this.addElement(HelpPageX(page))
}