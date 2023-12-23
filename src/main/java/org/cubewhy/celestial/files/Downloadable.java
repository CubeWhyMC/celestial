/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.files;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;

public class Downloadable implements Runnable {

    public final URL url;
    public final File file;

    public Downloadable(URL url, File file) {
        this.url = url;
        this.file = file;
    }

    /**
     * Start download
     * */
    @SneakyThrows
    @Override
    public void run() {
        // TODO multipart support
        DownloadManager.download(this.url, this.file);
    }
}
