/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import com.sun.management.OperatingSystemMXBean
import com.sun.tools.attach.VirtualMachine
import java.io.File
import java.lang.management.ManagementFactory


fun findJava(mainClass: String?): VirtualMachine? {
    for (descriptor in VirtualMachine.list()) {
        if (descriptor.displayName().startsWith(mainClass!!)) {
            return descriptor.provider().attachVirtualMachine(descriptor)
        }
    }
    return null
}

val currentJavaExec: File
    get() {
        var exec = System.getProperty("java.home") + "/bin/java"
        if (OSEnum.Windows.isCurrent) {
            exec += ".exe"
        }
        return File(exec)
    }

val totalMem: Int
    /**
     * Get total RAM (MB)
     */
    get() {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        return (osBean.totalMemorySize / 1048576).toInt()
    }

val arch = System.getProperty("os.arch").let { if (it == "x86_64") "x64" else it }

