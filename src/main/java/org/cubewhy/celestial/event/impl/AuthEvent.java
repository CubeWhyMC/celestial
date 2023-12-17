/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl;

import org.cubewhy.celestial.event.Event;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuthEvent extends Event {

    public final URL authURL;
    private String result = "";
    private final AtomicBoolean responded = new AtomicBoolean(false);

    public AuthEvent(URL authURL) {
        this.authURL = authURL;
    }

    public String waitForAuth() {
        while (!responded.get()) {
            Thread.onSpinWait();
        }
        return result;
    }

    public void put(String url) {
        if (url != null) {
            this.result = url;
        }
        this.responded.set(true);
    }
}
