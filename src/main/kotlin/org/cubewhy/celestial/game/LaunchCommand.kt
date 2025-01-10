/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game

import kotlinx.serialization.Serializable
import org.cubewhy.celestial.config
import org.cubewhy.celestial.game.addon.JavaAgent
import org.cubewhy.celestial.utils.GitUtils
import org.cubewhy.celestial.utils.OSEnum
import java.io.File
import java.util.*

data class LaunchCommand(
    val installation: File,

    val jre: File,
    val wrapper: String?,
    val mainClass: String,
    val natives: List<File>,
    val vmArgs: List<String>,
    val programArgs: List<String>,
    val javaAgents: List<JavaAgent>,
    val classpath: List<File>,
    val ichorpath: List<File>,

    var ipcPort: Int, // auth server port, 0=random
    val gameVersion: String,
    val gameProperties: GameProperties
) {
    fun startAuthServer(): NewAuthServer {
        val server = NewAuthServer(ipcPort)
        server.start()
        return server
    }

    fun generateCommand(ipcPort: Int = this.ipcPort): List<String> {
        val tempClasspath = mutableListOf<File>().apply {
            addAll(classpath)
        }
        val commands = mutableListOf<String>()
        val ram = config.game.ram
        if (wrapper != null) {
            commands.add(wrapper)
        }
        commands.add(jre.path)
        // ram
        commands.add("-Xms" + ram + "m")
        commands.add("-Xmx" + ram + "m")
        commands.addAll(vmArgs)
        // celestial special
        commands.add("-DcelestialVersion=${GitUtils.buildVersion}")
        // classpath
        // add javaagents to classpath
        tempClasspath.addAll(javaAgents.map { it.file }.toList())
        commands.add("-cp")
        if (OSEnum.Windows.isCurrent) {
            commands.add(tempClasspath.joinToString(";"))
        } else {
            commands.add(tempClasspath.joinToString(":"))
        }
        // javaagents
        commands.addAll(javaAgents.map { it.jvmArg }.toList())
        commands.add(mainClass)
        commands.add("--version")
        commands.add(gameVersion)
        commands.add("--accessToken")
        commands.add("0")
        commands.add("--userProperties")
        commands.add("{}")
        commands.add("--canaryToken")
        commands.add("control")
        commands.add("--launchId")
        commands.add(UUID.randomUUID().toString())
        commands.add("--launcherVersion")
        commands.add(config.api.versionSpoof) // latest version
        commands.add("--hwid")
        commands.add("PUBLIC-HWID")
        commands.add("--installationId")
        commands.add("INSTALLATION-ID")
        commands.add("--uiDir")
        commands.add("ui")
        commands.add("--texturesDir")
        commands.add("textures")
        commands.add("--workingDirectory")
        commands.add(installation.path)
        commands.add("--classpathDir")
        commands.add(installation.path)
        commands.add("--ipcPort")
        commands.add(ipcPort.toString())
        commands.add("--width")
        commands.add(gameProperties.width.toString())
        commands.add("--height")
        commands.add(gameProperties.height.toString())
        commands.add("--gameDir")
        commands.add(gameProperties.gameDir!!.path)
        commands.add("--assetIndex")
        commands.add(gameVersion.substring(0, gameVersion.lastIndexOf(".")))
        if (gameProperties.server != null) {
            commands.add("--server ") // Join server after launch
            commands.add(gameProperties.server!!)
        }
        // ichor
        commands.add("--ichorClassPath")
        commands.add(classpath.joinToString(","))
        commands.add("--ichorExternalFiles")
        commands.add(ichorpath.joinToString(","))
        commands.add("--webosrDir")
        commands.add(installation.resolve("natives").path)
        // custom args
        commands.addAll(programArgs)
        return commands
    }
}

@Serializable
data class LaunchCommandJson(
    val installation: String,

    val jre: String,
    val wrapper: String?,
    val mainClass: String,
    val natives: List<String>,
    val vmArgs: List<String>,
    val programArgs: List<String>,
    val classpath: List<String>,
    val ichorpath: List<String>,

    val ipcPort: Int,
    val gameVersion: String,
) {
    companion object {
        fun create(cmd: LaunchCommand) =
            LaunchCommandJson(
                installation = cmd.installation.path,
                jre = cmd.jre.path,
                wrapper = cmd.wrapper,
                mainClass = cmd.mainClass,
                natives = cmd.natives.map { it.path },
                vmArgs = cmd.vmArgs,
                programArgs = cmd.programArgs,
                classpath = cmd.classpath.map { it.path }.toList(),
                ichorpath = cmd.ichorpath.map { it.path }.toList(),
                ipcPort = cmd.ipcPort,
                gameVersion = cmd.gameVersion
            )
    }
}
