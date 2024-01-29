/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements

import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.border.TitledBorder

open class HelpPage(val documentName: String) : JPanel() {
    init {
        this.name = documentName
        this.border = TitledBorder(
            null,
            documentName,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
    }


    override fun toString(): String {
        return this.documentName
    }
}

/**
 * HelpPage with a scrollbar
 * */
internal class HelpPageX(base: HelpPage) : JScrollPane(base, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED) {
    private var documentName: String

    init {
        this.name = base.name
        this.documentName = base.documentName
        this.verticalScrollBar.unitIncrement = 30
    }

    override fun toString(): String {
        return this.documentName
    }
}

fun JTextArea.readOnly(): JTextArea {
    this.isEditable = false
    return this
}
