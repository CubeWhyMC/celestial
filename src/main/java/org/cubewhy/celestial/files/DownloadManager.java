package org.cubewhy.celestial.files;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.cubewhy.celestial.event.impl.FileDownloadEvent;
import org.cubewhy.celestial.utils.RequestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.cubewhy.celestial.Celestial.config;
import static org.cubewhy.celestial.Celestial.configDir;
import static org.cubewhy.celestial.gui.GuiLauncher.statusBar;

@Slf4j
public final class DownloadManager {
    public static final File cacheDir = new File(configDir, "cache");
    private static ExecutorService pool;

    static {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    private DownloadManager() {
    }

    public static void waitForAll() throws InterruptedException {
        pool.shutdown();
        while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
            Thread.onSpinWait();
        }
        // create a new pool (on invoking download)
        pool = null;
    }


    /**
     * Cache something
     *
     * @param url      url to the target file (online)
     * @param name     file name
     * @param override allow override?
     * @return status (true=success, false=failure)
     */
    public static boolean cache(URL url, String name, boolean override) throws IOException {
        File file = new File(cacheDir, name);
        if (file.exists() && !override) {
            return true;
        }
        log.info("Caching " + name + " (from " + url.toString() + ")");
        // download
        return download0(url, file);
    }

    /**
     * Download a file
     *
     * @param url  url to the target file (online)
     * @param file file instance of the local file
     * @return is success
     */
    public static boolean download0(URL url, @NotNull File file, String crcSha, Downloadable.Type type) throws IOException {
        // connect
        if (file.isFile() && crcSha != null) {
            // assert crcSha
            if (compareSha(file, crcSha, type)) {
                return true;
            }
        }
        new FileDownloadEvent(file, FileDownloadEvent.Type.START).call();
        log.info("Downloading " + url + " to " + file);
        try (Response response = RequestUtils.get(url).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                new FileDownloadEvent(file, FileDownloadEvent.Type.FAILURE).call();
                return false;
            }
            byte[] bytes = response.body().bytes();
            FileUtils.writeByteArrayToFile(file, bytes);
        }
        statusBar.setText("Download " + file.getName() + " success.");
        if (crcSha != null) {
            boolean result = compareSha(file, crcSha, type);
            if (!result) {
                new FileDownloadEvent(file, FileDownloadEvent.Type.FAILURE).call();
            }
            return result;
        }
        new FileDownloadEvent(file, FileDownloadEvent.Type.SUCCESS).call();
        return true;
    }

    private static boolean compareSha(@NotNull File file, String crcSha, Downloadable.@NotNull Type type) {
        return type.equals(Downloadable.Type.SHA1) && SecureUtil.sha1(file).equals(crcSha) || type.equals(Downloadable.Type.SHA256) && SecureUtil.sha256(file).equals(crcSha);
    }

    public static boolean download0(URL url, File file) throws IOException {
        return download0(url, file, null, Downloadable.Type.SHA1);
    }

    public static void download(Downloadable downloadable) {
        if (pool == null || pool.isTerminated()) {
            pool = Executors.newFixedThreadPool(config.getValue("max-threads").getAsInt(), new DownloadThreadFactory());
        }
        pool.execute(downloadable);
    }

    private static class DownloadThreadFactory implements ThreadFactory {
        private final AtomicInteger i = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("downloader-thread-" + i.getAndIncrement());
            return thread;
        }
    }
}
 