/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl;

import org.cubewhy.celestial.event.Event;

import java.net.URL;

public class AuthEvent extends Event {

    public final URL authURL;
    private volatile String result = null;

    public AuthEvent(URL authURL) {
        this.authURL = authURL;
    }

    public String waitForAuth() {
        while (result == null) {
            Thread.onSpinWait();
        }
        return result;
    }

    public void put(String url) {
        this.result = url;
    }
}
