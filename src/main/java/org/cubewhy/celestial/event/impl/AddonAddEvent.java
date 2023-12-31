/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl;

import org.cubewhy.celestial.event.Event;
import org.cubewhy.celestial.game.BaseAddon;

import java.io.File;

public class AddonAddEvent extends Event {

    public final Type type;
    public final BaseAddon addon;

    public enum Type {
        JAVAAGENT,
        WEAVE,
        LUNARCN;
    }

    public AddonAddEvent(Type type, BaseAddon addon) {
        this.type = type;
        this.addon = addon;
    }

}
