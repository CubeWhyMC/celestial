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

    /**
     * Start download
     */
    @SneakyThrows
    @Override
    public void run() {
        // TODO multipart support
        DownloadManager.download(this.url, this.file, this.sha1);
    }
}
