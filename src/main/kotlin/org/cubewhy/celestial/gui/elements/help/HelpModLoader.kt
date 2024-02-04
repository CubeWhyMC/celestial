/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.readOnly
import org.cubewhy.celestial.toJTextArea

class HelpModLoader : HelpPage(f.getString("gui.help.loader.title")) {
    init {
        this.add(f.getString("gui.help.loader").toJTextArea().readOnly())
    }
}