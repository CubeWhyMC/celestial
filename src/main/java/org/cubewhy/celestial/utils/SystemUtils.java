/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.Celestial;
import org.jackhuang.hmcl.util.platform.ManagedProcess;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// from hmcl launcher
@Slf4j
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
        Process p = managedProcess.getProcess();
        Celestial.gamePid = p.pid();
        log.info("Game pid: " + Celestial.gamePid);
        return p.waitFor();
    }
}
