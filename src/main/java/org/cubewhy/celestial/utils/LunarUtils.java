/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class LunarUtils {
    public static boolean isReallyOfficial(File session) throws IOException {
        JsonObject json = JsonParser.parseString(FileUtils.readFileToString(session, StandardCharsets.UTF_8)).getAsJsonObject();
        return !json.has("celestial");
    }
}
