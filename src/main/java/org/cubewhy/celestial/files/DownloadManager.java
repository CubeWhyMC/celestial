package org.cubewhy.celestial.files;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.cubewhy.celestial.utils.RequestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.cubewhy.celestial.Celestial.configDir;

@Slf4j
public final class DownloadManager {
    public static final File cacheDir = new File(configDir, "cache");

    static {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }


    /**
     * Cache something
     *
     * @param url url to the target file (online)
     * @param name file name
     * @param override allow override?
     *
     * @return status (true=success, false=failure)
     * */
    public static boolean cache(URL url, String name, boolean override) throws IOException {
        File file = new File(cacheDir, name);
        if (file.exists() && !override) {
            return true;
        }
        log.info("Caching " + name + " (from " + url.toString() + ")");
        // download
        return download(url, file);
    }

    /**
     * Download a file
     *
     * @param url url to the target file (online)
     * @param file file instance of the local file
     * */
    public static boolean download(URL url, File file) throws IOException {
        // connect
        try (Response response = RequestUtils.get(url).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return false;
            }
            byte[] bytes = response.body().bytes();
            FileUtils.writeByteArrayToFile(file, bytes);
        }
        return true;
    }
}
