/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.pages

import org.cubewhy.celestial.f
import org.cubewhy.celestial.gui.elements.GuiAddonManager
import org.cubewhy.celestial.gui.elements.GuiVersionSelect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import javax.swing.JPanel
import javax.swing.border.TitledBorder


class GuiVersion : JPanel() {
    init {
        this.border = TitledBorder(
            null,
            f.getString("gui.version.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        //        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.initGui()
    }


    private fun initGui() {
        this.add(GuiVersionSelect())
        this.add(GuiAddonManager())
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GuiVersion::class.java)
    }
}
