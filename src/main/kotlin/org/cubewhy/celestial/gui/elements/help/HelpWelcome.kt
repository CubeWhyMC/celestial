/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.f
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.open
import org.cubewhy.celestial.toJLabel
import java.net.URI
import javax.swing.JButton

class HelpWelcome : HelpPage("Welcome") {
    init {
        this.layout = VerticalFlowLayout()
        this.add("Welcome to the Celestial internal document!".toJLabel())

        val online = JButton(f.getString("gui.help.document"))
        online.addActionListener {
            URI.create("https://github.com/earthsworth/celestial/wiki").open()
        }
        this.add(online)
    }
}
