/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import com.sun.management.OperatingSystemMXBean;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

// from hmcl launcher
@Slf4j
public final class SystemUtils {
    private SystemUtils() {
    }

    public static Process callExternalProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        ManagedProcess managedProcess = new ManagedProcess(processBuilder);
//        managedProcess.pumpInputStream(SystemUtils::onLogLine);
//        managedProcess.pumpErrorStream(SystemUtils::onLogLine);
        return managedProcess.getProcess();
    }

    @Nullable
    public static VirtualMachine findJava(String mainClass) throws IOException, AttachNotSupportedException {
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (descriptor.displayName().startsWith(mainClass)) {
                return descriptor.provider().attachVirtualMachine(descriptor);
            }
        }
        return null;
    }

    public static File getCurrentJavaExec() {
        String exec = System.getProperty("java.home") + "/bin/java";
        if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
            exec += ".exe";
        }
        return new File(exec);
    }

    /**
     * Get total RAM (MB)
     */
    public static int getTotalMem() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return (int) (osBean.getTotalMemorySize() / 1048576);
    }
}
