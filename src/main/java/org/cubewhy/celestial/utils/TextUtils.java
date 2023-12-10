package org.cubewhy.celestial.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TextUtils {
    private TextUtils() {
    }

    public static String dumpTrace(Exception e) {
        StringWriter s = new StringWriter();
        PrintWriter stream = new PrintWriter(s);
        e.printStackTrace(stream);
        return s.toString();
    }
}
