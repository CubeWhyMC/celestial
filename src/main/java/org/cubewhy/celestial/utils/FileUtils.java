package org.cubewhy.celestial.utils;

import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    private FileUtils() {
    }

    public static InputStream inputStreamFromClassPath(String path) {
        return FileUtils.class.getResourceAsStream(path);
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        return inputStream.readAllBytes();
    }
}
