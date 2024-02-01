/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.Celestial
import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.readOnly
import javax.swing.JButton
import javax.swing.JTextArea

class HelpApi : HelpPage("API") {
    init {
        this.layout = VerticalFlowLayout()
        this.add(JTextArea(f.getString("gui.help.api")).readOnly())
        this.add(JButton(f.getString("gui.settings.title")).let {
            it.addActionListener {
                Celestial.launcherFrame.layoutX.show(Celestial.launcherFrame.mainPanel, "settings")
            }
            it
        })
    }
}
