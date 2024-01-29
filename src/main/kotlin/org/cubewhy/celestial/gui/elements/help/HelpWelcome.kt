/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.Celestial
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import java.awt.Desktop
import java.net.URI
import javax.swing.JButton
import javax.swing.JLabel

class HelpWelcome : HelpPage("Welcome") {
    init {
        this.layout = VerticalFlowLayout()
        this.add(JLabel("Welcome to the Celestial internal document!"))

        val online = JButton(Celestial.f.getString("gui.help.document"))
        online.addActionListener {
            Desktop.getDesktop().browse(URI.create("https://www.lunarclient.top/help"))
        }
        this.add(online)
    }
}