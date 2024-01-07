/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event.impl;

import org.cubewhy.celestial.event.Event;

import java.io.File;

public class FileDownloadEvent extends Event {

    public final File file;
    public final Type type;

    public enum Type {
        START,
        SUCCESS, FALURE;
    }

    public FileDownloadEvent(File file, Type type) {
        this.file = file;
        this.type = type;
    }
}
