package org.cubewhy.celestial.utils;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class TextUtils {
    private TextUtils() {
    }

    public static String dumpTrace(@NotNull Exception e) {
        StringWriter s = new StringWriter();
        PrintWriter stream = new PrintWriter(s);
        e.printStackTrace(stream);
        return s.toString();
    }
}
