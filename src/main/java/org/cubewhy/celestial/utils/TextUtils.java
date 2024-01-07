package org.cubewhy.celestial.utils;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class TextUtils {
    private TextUtils() {
    }

    public static String dumpTrace(@NotNull Exception e) {
        StringWriter s = new StringWriter();
        PrintWriter stream = new PrintWriter(s);
        e.printStackTrace(stream);
        return s.toString();
    }

    public static <T> T jsonToObj(String json, Class<T> clz) {
        Gson gson = new Gson();
        if (Objects.isNull(json)) return null;
        T obj = gson.fromJson(json, clz);
        if (Objects.isNull(obj)) {
            return null;
        } else {
            return obj;
        }
    }
}
