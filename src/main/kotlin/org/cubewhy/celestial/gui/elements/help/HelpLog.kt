/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.Celestial.launcherLogFile
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.readOnly
import org.cubewhy.celestial.utils.GuiUtils
import javax.swing.JTextArea

class HelpLog : HelpPage("Log") {
    init {
        this.layout = VerticalFlowLayout()
        this.add(JTextArea(f.getString("gui.help.log")).readOnly())
        this.add(GuiUtils.createButtonOpenFolder(f.getString("gui.settings.folder.log"), launcherLogFile.parentFile))
    }
}