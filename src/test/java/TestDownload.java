/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

import lombok.SneakyThrows;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.files.DownloadManager;
import org.cubewhy.celestial.files.Downloadable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class TestDownload {
    @Test
    @SneakyThrows
    public void testDownload() {
        for (int i = 0; i < 32; i++) {
            File file = new File(DownloadManager.cacheDir, "test" + i + ".bin");
            URL url = new URL("https://singapore.downloadtestfile.com/1GB.bin");
            DownloadManager.download(new Downloadable(url, file));
        }
        DownloadManager.waitForAll();
    }
}
