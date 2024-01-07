/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl;

import org.cubewhy.celestial.event.Event;
import org.cubewhy.celestial.gui.GuiLauncher;

public class CreateLauncherEvent extends Event {

    public final GuiLauncher theLauncher;

    public CreateLauncherEvent(GuiLauncher theLauncher) {
        this.theLauncher = theLauncher;
    }
}
