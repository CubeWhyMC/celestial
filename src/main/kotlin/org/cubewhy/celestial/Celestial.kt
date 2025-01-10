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
import org.cubewhy.celestial.game.GameProperties
import org.cubewhy.celestial.game.LaunchCommand
import org.cubewhy.celestial.game.addon.JavaAgent
import org.cubewhy.celestial.gui.GuiLauncher
import org.cubewhy.celestial.utils.*
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
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JDialog
import javax.swing.JOptionPane
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

val launcherLogFile: File = File(configDir, "logs/launcher.log")

val gamePid: AtomicLong = AtomicLong()
lateinit var f: ResourceBundle
lateinit var launcherData: LauncherData
lateinit var metadata: LauncherMetadata
lateinit var launcherFrame: GuiLauncher
var sessionFile: File = if (OSEnum.Windows.isCurrent) {
    // Microsoft Windows
    File(System.getenv("APPDATA"), "launcher/sentry/session.json")
} else if (OSEnum.Linux.isCurrent) {
    // Linux
    File(System.getProperty("user.home"), ".config/launcher/sentry/session.json")
} else {
    // MacOS
    File(System.getProperty("user.home"), "Library/Application Support//launcher/sentry/session.json")
}

val launchJson = File(configDir, "launch-data.json")
val launchScript: File = File(configDir, if (OSEnum.Windows.isCurrent) "launch.bat" else "launch.sh")
private lateinit var minecraftManifest: MinecraftManifest

private lateinit var locale: Locale
private lateinit var userLanguage: String

var runningOnGui = false
var jar = Celestial::class.java.protectionDomain.codeSource.location.path.toFile()

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
    log.info("CPU Arch: $arch (nodejs)")
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

    // start the old auth server
    log.info("Starting LC auth server")
    AuthServer.instance.startServer()

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
        log.warn("Compatibility warning: The Java you are currently using may not be able to start LunarClient properly (Java 21 is recommended)")
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
 * Generate args
 */
fun getArgs(
    version: String,
    branch: String?,
    module: String?,
    installation: File,
    gameProperties: GameProperties,
    givenAgents: List<JavaAgent> = emptyList()
): LaunchCommand {
    val json = launcherData.getVersion(version, branch, module)
    // === JRE ===
    val wrapper = config.game.wrapper
    val customJre = config.jre
    val vmArgs = mutableListOf<String>()
    if (wrapper.isNotBlank()) {
        log.warn("Launch the game via the wrapper: $wrapper")
    }
    var java = currentJavaExec
    if (!java.exists()) {
        log.error("Java executable not found, please specify it in the config file")
    }
    if (customJre.isNotEmpty()) {
        log.info("Use custom jre: $customJre")
        val customJreFile = customJre.toFile()
        if (!customJreFile.exists()) {
            log.warn("Custom jre is not exist on the filesystem, launch may fail.")
        }
        java = customJreFile
    } else {
        log.info("Use default jre: $java")
    }
    // === default vm args ===
    vmArgs.addAll(LauncherData.getDefaultJvmArgs(json))
    // serviceOverride
    config.game.overrides.forEach { (ov, address) ->
        vmArgs.add("-DserviceOverride$ov=$address")
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
        log.warn("Stop using LunarQT! This feature is DEPRECATED")
        javaAgents.add(JavaAgent(file))
    }
    // === custom vm args ===
    vmArgs.addAll(config.game.vmArgs)
    // === classpath ===
    val classpath: MutableList<File> = ArrayList()
    val ichorPath: MutableList<File> = ArrayList()
    var natives = mutableListOf<File>()
    for (artifact in json.launchTypeData.artifacts) {
        when (artifact.type) {
            GameArtifactInfo.Artifact.ArtifactType.CLASS_PATH -> {
                // is ClassPath
                classpath.add(installation.resolve(artifact.name))
            }

            GameArtifactInfo.Artifact.ArtifactType.EXTERNAL_FILE -> {
                // is external file
                ichorPath.add(installation.resolve(artifact.name))
            }

            GameArtifactInfo.Artifact.ArtifactType.NATIVES -> {
                // natives
                natives.add(installation.resolve(artifact.name))
            }

            GameArtifactInfo.Artifact.ArtifactType.JAVAAGENT -> {
                javaAgents.add(JavaAgent(installation.resolve(artifact.name)))
            }
        }
    }
    // === finish ===
    return LaunchCommand(
        installation = installation, // LunarClient installation
        jre = java, // java executable
        wrapper = config.game.wrapper.ifEmpty { null },
        mainClass = LauncherData.getMainClass(json), // Genesis main class
        natives = natives!!, // Minecraft Natives
        vmArgs = vmArgs, // jvm args
        javaAgents = javaAgents,
        classpath = classpath, // jvm classpath
        ichorpath = ichorPath,
        gameVersion = version, // Minecraft version
        gameProperties = gameProperties, // Minecraft args
        programArgs = config.game.args, // custom game args
        ipcPort = 0
    )
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
        DownloadManager.download(
            Downloadable(
                artifact.url.toURI().toURL(),
                File(installation, artifact.name),
                artifact.sha1
            )
        )
    }

    // download textures
    val index = LauncherData.getLunarTexturesIndex(versionJson)!!
    index.forEach { (fileName: String, urlString: String) ->
        val url: URL
        try {
            url = urlString.toURI().toURL()
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
        val finalURL = String.format("%s/%s/%s", MinecraftData.texture, folder, hash).toURI().toURL()
        val finalFile = File(assetsFolder, "objects/$folder/$hash")
        DownloadManager.download(Downloadable(finalURL, finalFile, hash))
    }
}

private fun File.isReallyOfficial(): Boolean {
    val json = JsonParser.parseString(FileUtils.readFileToString(this, StandardCharsets.UTF_8)).asJsonObject
    return !json.has("celestial")
}
