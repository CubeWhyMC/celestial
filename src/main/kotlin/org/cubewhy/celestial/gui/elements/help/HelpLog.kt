/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.f
import org.cubewhy.celestial.launcherLogFile
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.readOnly
import org.cubewhy.celestial.toJTextArea
import org.cubewhy.celestial.utils.createButtonOpenFolder

class HelpLog : HelpPage("Log") {
    init {
        this.layout = VerticalFlowLayout()
        this.add(f.getString("gui.help.log").toJTextArea().readOnly())
        this.add(createButtonOpenFolder(f.getString("gui.settings.folder.log"), launcherLogFile.parentFile))
    }
}