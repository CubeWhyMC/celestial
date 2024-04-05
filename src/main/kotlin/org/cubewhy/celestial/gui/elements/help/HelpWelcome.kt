/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.f
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.toJLabel
import java.awt.Desktop
import java.net.URI
import javax.swing.JButton

class HelpWelcome : HelpPage("Welcome") {
    init {
        this.layout = VerticalFlowLayout()
        this.add("Welcome to the Celestial internal document!".toJLabel())

        val online = JButton(f.getString("gui.help.document"))
        online.addActionListener {
            Desktop.getDesktop().browse(URI.create("https://lunarclient.top/help"))
        }
        this.add(online)
    }
}