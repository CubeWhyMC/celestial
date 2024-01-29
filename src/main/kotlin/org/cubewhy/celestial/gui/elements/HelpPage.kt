/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements

import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.TitledBorder

open class HelpPage(private val documentName: String) : JPanel() {
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