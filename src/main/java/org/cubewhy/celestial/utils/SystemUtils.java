/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import org.jackhuang.hmcl.util.platform.ManagedProcess;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// from hmcl launcher
public final class SystemUtils {
    private SystemUtils() {}

    public static int callExternalProcess(String... command) throws IOException, InterruptedException {
        return callExternalProcess(Arrays.asList(command));
    }

    public static int callExternalProcess(List<String> command) throws IOException, InterruptedException {
        return callExternalProcess(new ProcessBuilder(command));
    }

    public static int callExternalProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        ManagedProcess managedProcess = new ManagedProcess(processBuilder);
//        managedProcess.pumpInputStream(SystemUtils::onLogLine);
//        managedProcess.pumpErrorStream(SystemUtils::onLogLine);
        return managedProcess.getProcess().waitFor();
    }
}
