/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.files;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;

public record Downloadable(URL url, File file, String sha1) implements Runnable {
    public static final int fallBack = 5;

    /**
     * Start download
     */
    @SneakyThrows
    @Override
    public void run() {
        // TODO multipart support
        for (int i = 0; i < fallBack; i++) {
            try {
                DownloadManager.download0(this.url, this.file, this.sha1);
            } catch (Exception e) {
                continue; // try again
            }
            break; // no error
        }
    }
}
