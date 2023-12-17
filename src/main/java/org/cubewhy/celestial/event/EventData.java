/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event;

import java.lang.reflect.Method;

public class EventData {
    public final Object source;
    public final Method target;
    public final byte priority;

    public EventData(Object source, Method target, byte priority) {
        this.source = source;
        this.target = target;
        this.priority = priority;
    }
}
