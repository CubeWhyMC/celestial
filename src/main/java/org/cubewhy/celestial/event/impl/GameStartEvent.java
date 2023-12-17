/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl;

import org.cubewhy.celestial.event.Event;

public class GameStartEvent extends Event {

    public final long pid;

    public GameStartEvent(long pid) {
        this.pid = pid;
    }
}
