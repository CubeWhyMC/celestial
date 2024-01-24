/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import com.sun.management.OperatingSystemMXBean
import com.sun.tools.attach.AttachNotSupportedException
import com.sun.tools.attach.VirtualMachine
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory

// from hmcl launcher
object SystemUtils {
    @JvmStatic
    @Throws(IOException::class, InterruptedException::class)
    fun callExternalProcess(processBuilder: ProcessBuilder): Process {
        val managedProcess = ManagedProcess(processBuilder)
        //        managedProcess.pumpInputStream(SystemUtils::onLogLine);
//        managedProcess.pumpErrorStream(SystemUtils::onLogLine);
        return managedProcess.process
    }

    @JvmStatic
    @Throws(IOException::class, AttachNotSupportedException::class)
    fun findJava(mainClass: String?): VirtualMachine? {
        for (descriptor in VirtualMachine.list()) {
            if (descriptor.displayName().startsWith(mainClass!!)) {
                return descriptor.provider().attachVirtualMachine(descriptor)
            }
        }
        return null
    }

    @JvmStatic
    val currentJavaExec: File
        get() {
            var exec = System.getProperty("java.home") + "/bin/java"
            if (OSEnum.current == OSEnum.Windows) {
                exec += ".exe"
            }
            return File(exec)
        }

    @JvmStatic
    val totalMem: Int
        /**
         * Get total RAM (MB)
         */
        get() {
            val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
            return (osBean.totalMemorySize / 1048576).toInt()
        }
}
