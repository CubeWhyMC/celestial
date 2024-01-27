/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs

import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import java.awt.Desktop
import java.awt.Dimension
import java.net.URI
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel

class HelpDialog : JDialog() {
    init {
        this.title = f.getString("gui.help")
        this.layout = VerticalFlowLayout()
        this.size = Dimension(600, 600)
        this.isLocationByPlatform = true
        this.initGui()
    }

    private fun initGui() {
        // TODO Help dialog
        val online = JButton(f.getString("gui.help.document"))
        online.addActionListener {
            Desktop.getDesktop().browse(URI.create("https://www.lunarclient.top/help"))
        }
        this.add(online)
        this.add(JLabel("Under Construction"))
    }
}