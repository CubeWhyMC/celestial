/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements.help

import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.gui.elements.HelpPage
import org.cubewhy.celestial.gui.elements.readOnly
import javax.swing.JTextArea

class HelpLogin : HelpPage(f.getString("gui.help.login.title")) {

    init {
        this.add(JTextArea(f.getString("gui.help.login")).readOnly())
    }
}