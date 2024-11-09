/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.IntelliJTheme
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.cubewhy.celestial.event.impl.APIReadyEvent
import org.cubewhy.celestial.event.impl.CreateLauncherEvent
import org.cubewhy.celestial.files.DownloadManager
import org.cubewhy.celestial.files.Downloadable
import org.cubewhy.celestial.game.AuthServer
import org.cubewhy.celestial.game.GameArgs
import org.cubewhy.celestial.game.NewAuthServer
import org.cubewhy.celestial.game.addon.JavaAgent
import org.cubewhy.celestial.game.thirdparty.CeleWrap
import org.cubewhy.celestial.gui.GuiLauncher
import org.cubewhy.celestial.utils.CrashReportType
import org.cubewhy.celestial.utils.GitUtils
import org.cubewhy.celestial.utils.OSEnum
import org.cubewhy.celestial.utils.currentJavaExec
import org.cubewhy.celestial.utils.game.MinecraftData
import org.cubewhy.celestial.utils.game.MinecraftManifest
import org.cubewhy.celestial.utils.lunar.GameArtifactInfo
import org.cubewhy.celestial.utils.lunar.LauncherData
import org.cubewhy.celestial.utils.lunar.LauncherMetadata
import org.slf4j.LoggerFactory
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JDialog
import javax.swing.JOptionPane
import kotlin.io.path.relativeTo
import kotlin.system.exitProcess

object Celestial

private var log = LoggerFactory.getLogger(Celestial::class.java)
val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }
val configDir = File(System.getProperty("user.home"), ".cubewhy/lunarcn")
val themesDir = File(configDir, "themes")
val configFile = configDir.resolve("celestial.json")
val config: BasicConfig = try {
    JSON.decodeFromString(configFile.readText())
} catch (e: FileNotFoundException) {
    log.info("Config not found, creating a new one...")
    log.debug("This is not a bug, please DO NOT report this to developers!")
    log.debug(e.stackTraceToString())
    BasicConfig()
}

val gameLogFile: File = File(configDir, "logs/game.log")
val launcherLogFile: File = File(configDir, "logs/launcher.log")

val gamePid: AtomicLong = AtomicLong()
lateinit var f: ResourceBundle
lateinit var launcherData: LauncherData
lateinit var metadata: LauncherMetadata
lateinit var launcherFrame: GuiLauncher
private var sessionFile: File = if (OSEnum.Windows.isCurrent) {
    // Microsoft Windows
    File(System.getenv("APPDATA"), "launcher/sentry/session.json")
} else if (OSEnum.Linux.isCurrent) {
    // Linux
    File(System.getProperty("user.home"), ".config/launcher/sentry/session.json")
} else {
    // Macos...
    // Not tested yet
    File(System.getProperty("user.home"), "Library/Application Support//launcher/sentry/session.json")
}


val launchScript: File = File(configDir, if (OSEnum.Windows.isCurrent) "launch.bat" else "launch.sh")
private lateinit var minecraftManifest: MinecraftManifest

private lateinit var locale: Locale
private lateinit var userLanguage: String

var runningOnGui = false
var jar = Celestial::class.java.getProtectionDomain().codeSource.location.path.toFile()

val minecraftFolder: File
    /**
     * Get the default .minecraft folder
     *
     *
     * Windows: %APPDATA%/.minecraft
     * Linux/MacOS: ~/.minecraft
     */
    get() {
        val os = OSEnum.current
        if (os == OSEnum.Windows) {
            return File(System.getenv("APPDATA"), ".minecraft")
        }
        return File(System.getProperty("user.home"), ".minecraft")
    }

fun main() {
    // set encoding
    System.setProperty("file.encoding", "UTF-8")
    log.info("Celestial v${GitUtils.buildVersion} build by ${GitUtils.buildUser}")
    log.info("Git remote: ${GitUtils.remote} (${GitUtils.branch})")
    log.info("Classpath: ${System.getProperty("java.class.path")}")
    try {
        System.setProperty("file.encoding", "UTF-8")
        run()
    } catch (e: Exception) {
        val trace = e.stackTraceToString()
        log.error(trace)
        // please share the crash report with developers to help us solve the problems of the Celestial Launcher
        val message = StringBuffer("Celestial Crashed\n")
        message.append("Launcher Version: ").append(GitUtils.buildVersion).append("\n")
        if (config.dataSharing) {
            log.info("Uploading crash report")
            val logString = FileUtils.readFileToString(launcherLogFile, StandardCharsets.UTF_8)
            val result = launcherData.uploadCrashReport(logString, CrashReportType.LAUNCHER, null)
            if (result == null) {
                log.info("Crash report update failed")
            } else {
                log.info("Upload success, reportID is " + result.id)
                message.append("Report id: ").append(result.id).append("\n").append("View the report: ")
                    .append(result.url).append("\n")
            }
        }
        message.append(trace)
        JOptionPane.showMessageDialog(null, message, "Oops, Celestial crashed", JOptionPane.ERROR_MESSAGE)
        exitProcess(1)
    }
}

private fun run() {
    // init config
    initConfig()
    initTheme() // init theme

    log.info("Language: $userLanguage")
    checkJava()
    launcherData = LauncherData(config.api)
    if (config.proxy.state) log.info("Use proxy ${config.proxy.proxyAddress}")
    while (true) {
        try {
            // I don't know why my computer crashed here if the connection takes too much time :(
            log.info("Starting connect to the api -> " + launcherData.api.toString())
            initLauncher()
            log.info("connected")
            break // success
        } catch (e: Exception) {
            log.warn("API is unreachable")
            log.error(e.stackTraceToString())
            // shell we switch an api?
            val input = JOptionPane.showInputDialog(f.getString("api.unreachable"), config.api)
            if (input == null) {
                exitProcess(1)
            } else {
                val proxyInput = JOptionPane.showInputDialog(
                    f.getString("api.unreachable.proxy"),
                    if (config.proxy.state) config.proxy.proxyAddress else ""
                )
                if (proxyInput.isNullOrBlank()) {
                    config.proxy.state = false
                } else {
                    config.proxy.state = true
                    config.proxy.proxyAddress = proxyInput
                }
                launcherData = LauncherData(input)
                config.api = input
            }
        }
    }

    // start auth server
    log.info("Starting LC auth server")
    AuthServer.instance.startServer()
    NewAuthServer.instance.start()

    // start gui launcher
    launcherFrame = GuiLauncher()
    CreateLauncherEvent(launcherFrame).call()
    launcherFrame.isVisible = true
    runningOnGui = true
    APIReadyEvent().call()

    launcherFrame.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
            log.info("Closing celestial, dumping configs...")
            // dump configs
            config.save()
            launcherFrame.dispose()
        }

        override fun windowClosed(e: WindowEvent) {
            log.info("Exiting Java...")
            exitProcess(0) // exit java
        }
    })
}

private class SwitchAPIDialog : JDialog() {
    init {
        this.title = f.getString("api.unreachable.title")
    }
}

private fun BasicConfig.save() {
    val string = JSON.encodeToString(BasicConfig.serializer(), this)
    configFile.writeText(string) // dump
}

private fun initConfig() {
    // init dirs
    if (configDir.mkdirs()) log.info("Making config dir")

    if (themesDir.mkdirs()) log.info("Making themes dir")
    // init language
    log.info("Initializing language manager")
    locale = config.language.locale
    userLanguage = locale.language
    f = ResourceBundle.getBundle("languages/launcher", locale)
}

private fun initTheme() {
    val themeType = config.theme
    log.info("Set theme -> $themeType")
    when (themeType) {
        "dark" -> FlatDarkLaf.setup()
        "light" -> FlatLightLaf.setup()
        "unset" ->  // do nothing
            log.info("Theme disabled")

        else -> {
            val themeFile = File(themesDir, themeType)
            if (!themeFile.exists()) {
                // cannot load custom theme without theme.json
                JOptionPane.showMessageDialog(
                    null,
                    f.getString("theme.custom.notfound.message"),
                    f.getString("theme.custom.notfound.title"),
                    JOptionPane.WARNING_MESSAGE
                )
                return
            }
            val stream = Files.newInputStream(themeFile.toPath())
            IntelliJTheme.setup(stream) // load theme
        }
    }
}

private fun checkJava() {
    val javaVersion = System.getProperty("java.specification.version")
    log.info(
        "Celestial is running on Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty(
            "java.vm.version"
        ) + "(" + System.getProperty("java.vendor") + ") Arch: " + System.getProperty("os.arch")
    )

    if (javaVersion != "17" && javaVersion != "21") {
        log.warn("Compatibility warning: The Java you are currently using may not be able to start LunarClient properly (Java 17 is recommended)")
        JOptionPane.showMessageDialog(
            null,
            f.getString("compatibility.warn.message"),
            f.getString("compatibility.warn.title"),
            JOptionPane.WARNING_MESSAGE
        )
    }

    if (sessionFile.exists() && sessionFile.isReallyOfficial()) {
        log.warn("session.json exists, did you forgot to close the official lc launcher?")
        // the latest lc launcher doesn't use port 28189, so we needn't let user knows this thing.
//        JOptionPane.showMessageDialog(
//            null,
//            f.getString("warn.official-launcher.message"),
//            f.getString("warn.official-launcher.title"),
//            JOptionPane.WARNING_MESSAGE
//        )
    }
}

private fun initLauncher() {
    metadata = launcherData.metadata()
    minecraftManifest = MinecraftData.manifest()
}

/**
 * Wipe $game-installation/cache/:id
 *
 * @param id cache id
 */

fun wipeCache(id: String?): Boolean {
    log.info("Wiping LC cache")
    val installation = File(config.installationDir)
    val cache = if (id == null) {
        File(installation, "cache")
    } else {
        File(installation, "cache/$id")
    }
    return cache.exists() && cache.deleteRecursively()
}

/**
 * Launch LunarClient offline
 */
fun launch(wrapLog: Boolean = true): ProcessBuilder {
    // wrapper was applied in the script
    log.info("Launching with script")
    log.info("delete the log file")
    gameLogFile.delete()
    if (OSEnum.Windows.isCurrent) {
        // Windows
        // delete the log file
        val builder = ProcessBuilder()
        if (wrapLog) {
            builder.command(
                System.getenv("WINDIR") + "/System32/cmd.exe",
                "/C \"" + launchScript.path + " 1>>\"%s\" 2>&1\"".format(gameLogFile.path)
            )
        } else {
            builder.command(
                System.getenv("WINDIR") + "/System32/cmd.exe",
                "/C \"" + launchScript.path
            )
        }
        return builder
    } else {
        // others
        // do chmod
        Runtime.getRuntime().exec("chmod 777 " + launchScript.path)
        val builder = ProcessBuilder()
        if (wrapLog) {
            builder.command("/bin/bash", "-c", "\"" + launchScript.path + "\" > \"" + gameLogFile.path + "\"")
        } else {
            builder.command("/bin/bash", "-c", "\"" + launchScript.path)
        }
        return builder
    }
}


/**
 * Get args
 */
fun getArgs(
    version: String,
    branch: String?,
    module: String?,
    installation: File,
    gameArgs: GameArgs,
    givenAgents: List<JavaAgent> = emptyList()
): GameArgsResult {
    val args: MutableList<String> = ArrayList()
    val json = launcherData.getVersion(version, branch, module)
    // === JRE ===
    val wrapper = config.game.wrapper
    val customJre = config.jre
    if (wrapper.isNotBlank()) {
        log.warn("Launch the game via the wrapper: $wrapper")
        args.add(wrapper)
    }
    if (customJre.isEmpty()) {
        val java = currentJavaExec
        if (!java.exists()) {
            log.error("Java executable not found, please specify it in the config file")
        }
        log.info("Use default jre: $java")
        args.add("\"" + java.path + "\"") // Note: Java may not be found through this method on some non-Windows computers. You can manually specify the Java executable file.
    } else {
        log.info("Use custom jre: $customJre")
        args.add("\"" + customJre + "\"")
    }
    // === default vm args ===
    val ram = config.game.ram
    log.info("RAM: " + ram + "MB")
    args.add("-Xms" + ram + "m")
    args.add("-Xmx" + ram + "m")
    args.addAll(LauncherData.getDefaultJvmArgs(json, installation))
    // serviceOverride
    config.game.overrides.forEach { ov, address ->
        args.add("-DserviceOverride$ov=$address")
    }
    // === javaagents ===
    val javaAgents = JavaAgent.findEnabled()
    javaAgents.addAll(givenAgents)
    val size = javaAgents.size
    if (size != 0) {
        log.info(
            "Found %s javaagent%s (Except loaders)".format(
                size, if ((size == 1)) "" else "s"
            )
        )
    }
    // ===     loaders    ===
    val weave = config.addon.weave
    val cn = config.addon.lunarcn
    val lcqt = config.addon.lcqt
    if (config.celeWrap.state) {
        javaAgents.add(JavaAgent(CeleWrap.installation)) // patch classloader and anti antiagent check
    }
    if (weave.state) {
        if (version == "1.8.9") {
            val file = weave.installationDir
            log.info("Weave enabled! $file")
            javaAgents.add(JavaAgent(file))
        } else {
            log.info("Weave disabled due to version is not 1.8.9!")
        }
    }
    if (cn.state) {
        val file = cn.installationDir
        log.info("LunarCN enabled! $file")
        log.warn("LunarCN might not working probably at the latest version of LunarClient")
        javaAgents.add(JavaAgent(file))
    }
    if (lcqt.state) {
        val file = lcqt.installationDir
        log.info("LunarQT enabled! $file")
        javaAgents.add(JavaAgent(file))
    }
    for (agent in javaAgents) {
        args.add(agent.jvmArg)
    }
    // === custom vm args ===
    val customVMArgs: MutableList<String> = ArrayList()
    for (element in config.game.vmArgs) {
        customVMArgs.add(element)
    }
    args.addAll(customVMArgs)
    // === classpath ===
    val classpath: MutableList<String> = ArrayList()
    val ichorPath: MutableList<String> = ArrayList()
    var natives: File? = null
    args.add("-cp")
    for (artifact in json.launchTypeData.artifacts) {
        when (artifact.type) {
            GameArtifactInfo.Artifact.ArtifactType.CLASS_PATH -> {
                // is ClassPath
                classpath.add(artifact.name)
            }

            GameArtifactInfo.Artifact.ArtifactType.EXTERNAL_FILE -> {
                // is external file
                ichorPath.add("\"" + File(artifact.name).path + "\"")
            }

            GameArtifactInfo.Artifact.ArtifactType.NATIVES -> {
                // natives
                natives = installation.resolve(artifact.name).toPath().relativeTo(installation.toPath()).toFile()
            }

            GameArtifactInfo.Artifact.ArtifactType.JAVAAGENT -> {
                javaAgents.add(JavaAgent(installation.resolve(artifact.name)))
            }
        }
    }
    if (config.celeWrap.state) {
        // [celewrap] add Celestial's classpath
        log.info("CeleWrap is enabled")
        if (config.celeWrap.checkUpdate && CeleWrap.checkUpdate()) {
            log.info("CeleWrap upgraded successful")
        }
        classpath.add("\"${CeleWrap.installation.path}\"")
    }
    if (OSEnum.Windows.isCurrent) {
        args.add(classpath.joinToString(";"))
    } else {
        args.add(classpath.joinToString(":"))
    }
    // === main class ===
    val mainClass = LauncherData.getMainClass(json)
    if (config.celeWrap.state) {
        // using celestial
        args.add("-DlunarMain=$mainClass")
        args.add("-DcelestialVersion=${GitUtils.buildVersion}")
        args.add("org.cubewhy.CeleWrapKt") // celestial wrapper
    } else {
        args.add(mainClass)
    }
    // === game args ===
    val ichorEnabled = LauncherData.getIchorState(json)
    args.add("--version $version") // what version will lunarClient inject to
    args.add("--accessToken 0")
    args.add("--userProperties {}")
    args.add("--launcherVersion 2.15.1")
    args.add("--hwid PUBLIC-HWID")
    args.add("--installationId INSTALLATION-ID")
    args.add("--workingDirectory .")
    args.add("--classpathDir .")
    args.add("--width " + gameArgs.width)
    args.add("--height " + gameArgs.height)
    args.add("--gameDir " + gameArgs.gameDir)
    args.add("--texturesDir textures")
    args.add("--uiDir ui")
    args.add("--ipcPort " + NewAuthServer.instance.port)
    if (gameArgs.server != null) {
        args.add("--server " + gameArgs.server) // Join server after launch
    }
    args.add("--assetIndex " + version.substring(0, version.lastIndexOf(".")))
    if (ichorEnabled) {
        args.add("--ichorClassPath")
        args.add(classpath.joinToString(","))
        args.add("--ichorExternalFiles")
        args.add(classpath.joinToString(","))
    }
    args.add("--webosrDir")
    args.add("\"" + natives!!.path + "\"")
    // === custom game args ===
    for (arg in config.game.args) {
        args.add(arg)
    }
    // === finish ===
    return GameArgsResult(args, File(installation, natives.path))
}

/**
 * Launch LunarClient with the online config
 *
 * @param version Minecraft version
 * @param module  LunarClient module
 * @param branch  Git branch (LunarClient)
 * @return natives file
 */
fun launch(
    version: String,
    branch: String?,
    module: String?,
    javaagents: List<JavaAgent> = emptyList(),
    beforeLaunch: () -> Unit = {}
): File {
    completeSession()
    beforeLaunch()
    val installationDir = File(config.installationDir)

    log.info(String.format("Launching (%s, %s, %s)", version, module, branch))
    log.info("Generating launch params")
    val resize = config.game.resize
    val width = resize.width
    val height = resize.height
    log.info(String.format("Resize: (%d, %d)", width, height))
    val gameArgs = GameArgs(width, height, File(config.game.gameDir))
    val argsResult = getArgs(version, branch, module, installationDir, gameArgs, javaagents)
    val args = argsResult.args
    val argsString = args.joinToString(" ")
    val natives = argsResult.natives
    // dump launch script
    if (launchScript.delete()) {
        log.info("Delete launch script")
    }
    if (launchScript.createNewFile()) {
        log.info("Launch script was created")
    }
    FileWriter(launchScript, StandardCharsets.UTF_8).use { writer ->
        if (OSEnum.Windows.isCurrent) {
            // Microsoft Windows
            // NEW: Use CRLF (Windows 7)
            writer.write("@echo off\r\n")
            writer.write("chcp 65001\r\n") // unicode support for Windows which uses Chinese
//            writer.write("@rem Generated by LunarCN (Celestial Launcher)\r\n@rem Website: https://lunarclient.top/\r\n")
//            writer.write("@rem Version %s\r\n".format(GitUtils.buildVersion))
//            writer.write("@rem Please donate to support us to continue develop https://lunarclient.top/donate\r\n")
//            writer.write("@rem You can run this script to debug your game, or share this script to developers to resolve your launch problem\r\n")
            writer.write("cd /d $installationDir\r\n")
        } else {
            // Others
            writer.write("#!/bin/bash\n")
            writer.write("# Generated by LunarCN (Celestial Launcher)\n# Website: https://lunarclient.top/\n")
            writer.write("# Please donate to support us to continue develop https://lunarclient.top/donate\n")
            writer.write("# You can run this script to debug your game, or share this script to developers to resolve your launch problem\n")
            writer.write("cd $installationDir\n")
        }
        writer.write("\n")
        writer.write(argsString)
    }
    log.info("Args was dumped to $launchScript")
    log.info("Natives file: $natives")
    return natives // success
}

/**
 * Patching network disabling for LunarClient
 */
fun completeSession() {
    if (!sessionFile.exists()) {
        log.info("Completing session.json to fix the network error for LunarClient")
        var json: ByteArray?
        "/game/session.json".getInputStream().use { stream ->
            json = stream?.readAllBytes()
        }
        FileUtils.writeStringToFile(
            sessionFile, JsonParser.parseString(
                String(
                    json!!, StandardCharsets.UTF_8
                )
            ).toString(), StandardCharsets.UTF_8
        )
    }
}

/**
 * Check and download updates for game
 *
 * @param version Minecraft version
 * @param module  LunarClient module
 * @param branch  Git branch (LunarClient)
 */

@Synchronized
fun checkUpdate(version: String, module: String?, branch: String?) {
    log.info("Checking update")
    val installation = File(config.installationDir)
    val versionJson = launcherData.getVersion(version, branch, module)
    // download artifacts
    val artifacts = versionJson.launchTypeData.artifacts
    for (artifact in artifacts) {
        DownloadManager.download(Downloadable(URL(artifact.url), File(installation, artifact.name), artifact.sha1))
    }

    // download textures
    val index = LauncherData.getLunarTexturesIndex(versionJson)!!
    index.forEach { (fileName: String, urlString: String) ->
        val url: URL
        try {
            url = URL(urlString)
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
        val full = urlString.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val file = File(installation, "textures/$fileName")
        DownloadManager.download(Downloadable(url, file, full[full.size - 1]))
    }

    val minecraftFolder = File(config.game.gameDir)

    GuiLauncher.statusBar.text = "Complete textures for vanilla Minecraft"
    val textureIndex = MinecraftData.getVersion(
        version, minecraftManifest
    )!!.let {
        MinecraftData.getTextureIndex(
            it
        )
    }
    // dump to .minecraft/assets/indexes
    val assetsFolder = minecraftFolder.resolve("assets")
    val indexFile = File(
        assetsFolder,
        "indexes/" + Arrays.copyOfRange(
            version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(),
            0,
            2
        ).joinToString(".") + ".json"
    )
    FileUtils.writeStringToFile(indexFile, Gson().toJson(textureIndex), StandardCharsets.UTF_8)

    val objects = textureIndex.objects
    // baseURL/hash[0:2]/hash
    for (resource in objects.values) {
        val hash = resource.hash
        val folder = hash.substring(0, 2)
        val finalURL = URL(String.format("%s/%s/%s", MinecraftData.texture, folder, hash))
        val finalFile = File(assetsFolder, "objects/$folder/$hash")
        DownloadManager.download(Downloadable(finalURL, finalFile, hash))
    }
}

private fun File.isReallyOfficial(): Boolean {
    val json = JsonParser.parseString(FileUtils.readFileToString(this, StandardCharsets.UTF_8)).asJsonObject
    return !json.has("celestial")
}

data class GameArgsResult(val args: List<String>, val natives: File)
