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
    public static final int fallBack = 5;
    private final URL url;
    private final File file;
    private final String crcSha;
    private final Type type;

    public Downloadable(URL url, File file, String sha1) {
        this(url, file, sha1, Type.SHA1);
    }


    public Downloadable(URL url, File file, String crcSha, Type type) {
        this.url = url;
        this.file = file;
        this.crcSha = crcSha;
        this.type = type;
    }

    public enum Type {
        SHA1,
        SHA256
    }

    /**
     * Start download
     */
    @SneakyThrows
    @Override
    public void run() {
        // TODO multipart support
        for (int i = 0; i < fallBack; i++) {
            try {
                DownloadManager.download0(this.url, this.file, this.crcSha, this.type);
            } catch (Exception e) {
                continue; // try again
            }
            break; // no error
        }
    }
}
