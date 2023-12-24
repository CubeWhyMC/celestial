package org.cubewhy.celestial.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Slf4j
public class FileUtils {
    private FileUtils() {
    }

    public static InputStream inputStreamFromClassPath(String path) {
        return FileUtils.class.getResourceAsStream(path);
    }

    public static byte[] readBytes(@NotNull InputStream inputStream) throws IOException {
        return inputStream.readAllBytes();
    }

    public static void unzipNatives(File nativesZip, File baseDir) throws IOException {
        log.info("Unzipping natives...");
        File dir = new File(baseDir, "natives");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        unZip(nativesZip, dir);
    }

    public static void unZip(File input, File outputDir) throws IOException {
        ZipFile zipfile = new ZipFile(input);
        ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(input.toPath()));

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            File out = new File(outputDir, entry.getName());
            if (entry.isDirectory()) {
                out.mkdirs();
            } else {
                out.createNewFile();
                InputStream entryInputStream = zipfile.getInputStream(entry);
                try (FileOutputStream fileOutPutStream = new FileOutputStream(out)) {
                    fileOutPutStream.write(entryInputStream.readAllBytes());
                }
            }
        }
        zipfile.close();
    }
}
