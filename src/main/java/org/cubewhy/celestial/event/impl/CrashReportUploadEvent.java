/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl;

import org.cubewhy.celestial.event.Event;

public class CrashReportUploadEvent extends Event {

    public final String crashID;
    public final String url;

    public CrashReportUploadEvent(String crashID, String url) {
        this.crashID = crashID;
        this.url = url;
    }
}
