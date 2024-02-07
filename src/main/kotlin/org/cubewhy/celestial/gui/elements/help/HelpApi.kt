/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.launcherFrame
import org.cubewhy.celestial.f
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.readOnly
import org.cubewhy.celestial.toJTextArea
import javax.swing.JButton

class HelpApi : HelpPage("API") {
    init {
        this.layout = VerticalFlowLayout()
        this.add(f.getString("gui.help.api").toJTextArea().readOnly())
        this.add(JButton(f.getString("gui.settings.title")).let {
            it.addActionListener {
                launcherFrame.layoutX.show(launcherFrame.mainPanel, "settings")
            }
            it
        })
    }
}
