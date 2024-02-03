/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.IntelliJTheme
import com.google.gson.*
import joptsimple.OptionParser
import org.apache.commons.io.FileUtils
import org.cubewhy.celestial.event.impl.CreateLauncherEvent
import org.cubewhy.celestial.files.ConfigFile
import org.cubewhy.celestial.files.DownloadManager
import org.cubewhy.celestial.files.Downloadable
import org.cubewhy.celestial.files.ProxyConfig
import org.cubewhy.celestial.game.AuthServer
import org.cubewhy.celestial.game.GameArgs
import org.cubewhy.celestial.game.GameArgsResult
import org.cubewhy.celestial.game.addon.JavaAgent
import org.cubewhy.celestial.gui.GuiLauncher
import org.cubewhy.celestial.utils.*
import org.cubewhy.celestial.utils.game.MinecraftData
import org.cubewhy.celestial.utils.game.ModrinthData
import org.cubewhy.celestial.utils.lunar.LauncherData
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.io.FileWriter
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JOptionPane
import kotlin.system.exitProcess

object Celestial {
    val configDir: File = File(System.getProperty("user.home"), ".cubewhy/lunarcn")
    val themesDir: File = File(configDir, "themes")

    val config: ConfigFile = ConfigFile(File(configDir, "celestial.json"))

    val proxy = ProxyConfig(File(configDir, "proxy.json"))
    val gameLogFile: File = File(configDir, "logs/game.log")
    val launcherLogFile: File = File(configDir, "logs/launcher.log")

    val gamePid: AtomicLong = AtomicLong()
    private lateinit var locale: Locale
    private lateinit var userLanguage: String
    lateinit var f: ResourceBundle
    lateinit var launcherData: LauncherData
    lateinit var metadata: JsonObject
    lateinit var modrinth: ModrinthData
    private lateinit var minecraftManifest: JsonObject
    lateinit var launcherFrame: GuiLauncher
    private var themed: Boolean = true

    val launchScript: File = File(configDir, if ((OSEnum.current == OSEnum.Windows)) "launch.bat" else "launch.sh")
    private var sessionFile: File = if (OSEnum.current == OSEnum.Windows) {
        // Microsoft Windows
        File(System.getenv("APPDATA"), "launcher/sentry/session.json")
    } else {
        // Linux, Macos... etc.
        File(System.getProperty("user.home"), ".config/launcher/sentry/session.json")
    }

    private var log = LoggerFactory.getLogger(Celestial::class.java)


    @JvmStatic
    fun main(args: Array<String>) {
        // set encoding
        System.setProperty("file.encoding", "UTF-8")
        log.info("Celestial v${GitUtils.buildVersion} build by ${GitUtils.buildUser}")
        log.info("Git remote: ${GitUtils.remote} (${GitUtils.branch})")
        try {
            System.setProperty("file.encoding", "UTF-8")
            run(args)
        } catch (e: Exception) {
            val trace = e.stackTraceToString()
            log.error(trace)
            // please share the crash report with developers to help us solve the problems of the Celestial Launcher
            val message = StringBuffer("Celestial Crashed\n")
            message.append("Launcher Version: ").append(GitUtils.buildVersion).append("\n")
            if (config.config.has("data-sharing") && config.getValue("data-sharing").asBoolean) {
                log.info("Uploading crash report")
                val logString = FileUtils.readFileToString(launcherLogFile, StandardCharsets.UTF_8)
                val map = launcherData.uploadCrashReport(logString, CrashReportType.LAUNCHER, null)
                if (map.isEmpty()) {
                    log.info("Crash report update failed")
                } else {
                    log.info("Upload success, reportID is " + map["id"])
                    message.append("Report id: ").append(map["id"]).append("\n").append("View the report: ")
                        .append(map["url"]).append("\n")
                }
            }
            message.append(trace)
            JOptionPane.showMessageDialog(null, message, "Oops, Celestial crashed", JOptionPane.ERROR_MESSAGE)
            exitProcess(1)
        }
    }


    private fun run(args: Array<String>) {
        // init config
        initConfig()
        initTheme() // init theme

        val optionParser = OptionParser()
        optionParser.accepts("help", "Print help and exit")
            .withOptionalArg()
        optionParser.accepts("api", "LunarClient api")
            .withOptionalArg()
            .defaultsTo(config.getValue("api").asString)
            .ofType(String::class.java)
        optionParser.accepts("game", "version:module:branch")
            .withOptionalArg()
            .ofType(String::class.java)
        optionParser.accepts("offline", "Launch with offline")
            .availableIf("game")
        optionParser.accepts("no-update", "Disable game update")
            .availableIf("game")
            .availableUnless("offline")
        val options = optionParser.parse(*args)
        if (options.has("help")) {
            optionParser.printHelpOn(System.out)
            exitProcess(1)
        } else if (options.has("game")) {
            // launch game
            log.info("Celestial - CommandLine")
            log.warn("Command line startup is experimental. If any problems occur, please provide rational feedback.")
            launcherData = LauncherData(options.valueOf("api") as String)
            val game = (options.valueOf("game") as String).split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (game.size != 3) {
                log.error("Arg game must be version:module:branch")
                exitProcess(1)
            }
            val version = game[0]
            val module = game[1]
            val branch = game[2]
            if (options.has("offline")) {
                launch()
            } else {
                if (!options.has("no-update")) {
                    checkUpdate(version, module, branch)
                }
                launch(version, module, branch)
            }
            exitProcess(0)
        }
        log.info("Language: $userLanguage")
        checkJava()
        launcherData = LauncherData(config.getValue("api").asString)
        if (proxy.state) log.info("Use proxy ${proxy.proxy}")
        while (true) {
            try {
                // I don't know why my computer crashed here if the connection takes too much time :(
                log.info("Starting connect to the api -> " + launcherData.api.toString())
                initLauncher()
                log.info("connected")
                break // success
            } catch (e: Exception) {
                log.error(e.stackTraceToString())
                // shell we switch an api?
                val input =
                    JOptionPane.showInputDialog(f.getString("api.unreachable"), config.getValue("api").asString)
                if (input == null) {
                    exitProcess(1)
                } else {
                    launcherData = LauncherData(input)
                    config.setValue("api", input)
                }
            }
        }

        // start auth server
        AuthServer.instance.startServer()


        // start gui launcher
        launcherFrame = GuiLauncher()
        CreateLauncherEvent(launcherFrame).call()
        launcherFrame.isVisible = true

        launcherFrame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                log.info("Closing celestial, dumping configs...")
                // dump configs
                proxy.save()
                config.save()
                launcherFrame.dispose()
            }

            override fun windowClosed(e: WindowEvent) {
                exitProcess(0) // exit java
            }
        })
    }


    private fun checkJava() {
        val javaVersion = System.getProperty("java.specification.version")
        log.info(
            "Celestial is running on Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty(
                "java.vm.version"
            ) + "(" + System.getProperty("java.vendor") + ") Arch: " + System.getProperty("os.arch")
        )

        if (javaVersion != "17") {
            log.warn("Compatibility warning: The Java you are currently using may not be able to start LunarClient properly (Java 17 is recommended)")
            JOptionPane.showMessageDialog(
                null,
                f.getString("compatibility.warn.message"),
                f.getString("compatibility.warn.title"),
                JOptionPane.WARNING_MESSAGE
            )
        }

        if (sessionFile.exists() && sessionFile.isReallyOfficial()) {
            log.warn("Detected the official launcher")
            JOptionPane.showMessageDialog(
                null,
                f.getString("warn.official-launcher.message"),
                f.getString("warn.official-launcher.title"),
                JOptionPane.WARNING_MESSAGE
            )
        }
    }


    private fun initLauncher() {
        metadata = launcherData.metadata()
        minecraftManifest = MinecraftData.manifest()
        modrinth = ModrinthData("https://api.modrinth.com".toURL())
        if (metadata.has("error")) {
            // trouble here
            log.error("Metadata info: $metadata")
            throw IllegalStateException("metadata API Error!")
        }
    }

    private fun initConfig() {
        // init dirs
        if (configDir.mkdirs()) {
            log.info("Making config dir")
        }
        if (themesDir.mkdirs()) {
            log.info("Making themes dir")
        }
        // init config
        val resize = JsonObject()
        resize.addProperty("width", 854)
        resize.addProperty("height", 480)

        val addon = JsonObject()
        // weave
        val weave = JsonObject()
        weave.addProperty("enable", false)
        weave.addProperty(
            "installation",
            File(System.getProperty("user.home"), ".cubewhy/lunarcn/loaders/weave.jar").path
        )
        weave.addProperty("check-update", true)
        addon.add("weave", weave)
        // lccn
        val lunarcn = JsonObject()
        lunarcn.addProperty("enable", false)
        lunarcn.addProperty(
            "installation",
            File(System.getProperty("user.home"), ".cubewhy/lunarcn/loaders/cn.jar").path
        )
        lunarcn.addProperty("check-update", true)
        addon.add("lunarcn", lunarcn)

        config.initValue("jre", "") // leave empty if you want to use the default one
            .initValue("language", Locale.getDefault().language) // en, zh, ja, ko
            .initValue("installation-dir", File(configDir, "game").path)
            .initValue("game-dir", minecraftFolder.path) // the minecraft folder
            .initValue("game", null)
            .initValue("addon", addon)
            .initValue("ram", totalMem / 4)
            .initValue("max-threads", Runtime.getRuntime().availableProcessors()) // recommend: same as your CPU core
            .initValue(
                "api",
                "https://api.lunarclient.top"
            ) // only support the LunarCN api, Moonsworth's looks like shit :(
            .initValue("theme", "dark") // dark, light, unset, custom.
            .initValue("resize", resize) // (854, 480) for default
            .initValue("vm-args", JsonArray()) // custom jvm args
            .initValue("wrapper", "") // like optirun on linux
            .initValue("program-args", JsonArray()) // args of the game
            .initValue("javaagents", JsonObject()) // lc addon
        config.save()
        // init language
        log.info("Initializing language manager")
        locale = Locale.forLanguageTag(config.getValue("language").asString)
        userLanguage = locale.language
        f = ResourceBundle.getBundle("languages/launcher", locale)

        if (!config.config.has("data-sharing")) {
            val b = JOptionPane.showConfirmDialog(
                null,
                f.getString("data-sharing.confirm.message"),
                f.getString("data-sharing.confirm.title"),
                JOptionPane.YES_NO_OPTION
            ) == JOptionPane.OK_OPTION
            config.initValue("data-sharing", JsonPrimitive(b))
        }
    }

    @get:Contract(" -> new")
    private val minecraftFolder: File
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


    private fun initTheme() {
        val themeType = config.getValue("theme").asString
        log.info("Set theme -> $themeType")
        when (themeType) {
            "dark" -> FlatDarkLaf.setup()
            "light" -> FlatLightLaf.setup()
            "unset" ->  // do nothing
                themed = false
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

    /**
     * Wipe $game-installation/cache/:id
     *
     * @param id cache id
     */

    fun wipeCache(id: String?): Boolean {
        log.info("Wiping LC cache")
        val installation = File(config.getValue("installation-dir").asString)
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


    fun launch(): ProcessBuilder {
        // wrapper was applied in the script
        log.info("Launching with script")
        log.info("delete the log file")
        gameLogFile.delete()
        if (OSEnum.current == OSEnum.Windows) {
            // Windows
            // delete the log file
            val builder = ProcessBuilder()
            builder.command(
                System.getenv("WINDIR") + "/System32/cmd.exe",
                "/C \"" + launchScript.path + String.format(" 1>>\"%s\" 2>&1\"", gameLogFile.path)
            )
            return builder
        } else {
            // others
            // do chmod
            Runtime.getRuntime().exec("chmod 777 " + launchScript.path)
            val builder = ProcessBuilder()
            builder.command("/bin/bash", "-c", "\"" + launchScript.path + "\" > \"" + gameLogFile.path + "\"")
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
        gameArgs: GameArgs
    ): GameArgsResult? {
        val args: MutableList<String> = ArrayList()
        val json = launcherData.getVersion(version, branch, module)
        if (!json["success"].asBoolean) {
            return null
        }
        // === JRE ===
        val wrapper = config.getValue("wrapper").asString
        val customJre = config.getValue("jre").asString
        if (wrapper.isNotBlank()) {
            log.warn("Launch the game via the wrapper: $wrapper")
            args.add(wrapper)
        }
        if (customJre.isEmpty()) {
            val java = currentJavaExec
            if (!java.exists()) {
                log.error("Java executable not found, please specify it in " + config.file)
            }
            log.info("Use default jre: $java")
            args.add("\"" + java.path + "\"") // Note: Java may not be found through this method on some non-Windows computers. You can manually specify the Java executable file.
        } else {
            log.info("Use custom jre: $customJre")
            args.add("\"" + customJre + "\"")
        }
        // === default vm args ===
        val ram = config.getValue("ram").asLong
        log.info("RAM: " + ram + "MB")
        args.add("-Xms" + ram + "m")
        args.add("-Xmx" + ram + "m")
        args.addAll(LauncherData.getDefaultJvmArgs(json, installation))
        // === javaagents ===
        val javaAgents = JavaAgent.findEnabled()
        val size = javaAgents.size
        if (size != 0) {
            log.info(
                String.format(
                    "Found %s javaagent%s (Except loaders)",
                    size,
                    if ((size == 1)) "" else "s"
                )
            )
        }
        // ===     loaders    ===
        val weave = config.getValue("addon").asJsonObject.getAsJsonObject("weave")
        val cn = config.getValue("addon").asJsonObject.getAsJsonObject("lunarcn")
        if (weave["enable"].asBoolean) {
            val file = weave["installation"].asString
            log.info("Weave enabled! $file")
            javaAgents.add(JavaAgent(file))
        }
        if (cn["enable"].asBoolean) {
            val file = cn["installation"].asString
            log.info("LunarCN enabled! $file")
            javaAgents.add(JavaAgent(file))
        }
        for (agent in javaAgents) {
            args.add(agent.jvmArg)
        }
        // === custom vm args ===
        val customVMArgs: MutableList<String> = ArrayList()
        for (jsonElement in config.getValue("vm-args").asJsonArray) {
            customVMArgs.add(jsonElement.asString)
        }
        args.addAll(customVMArgs)
        // === classpath ===
        val classpath: MutableList<String> = ArrayList()
        val ichorPath: MutableList<String> = ArrayList()
        var natives: File? = null
        args.add("-cp")
        for (artifact in json
            .getAsJsonObject("launchTypeData")
            .getAsJsonArray("artifacts")) {
            when (artifact.asJsonObject["type"].asString) {
                "CLASS_PATH" -> {
                    // is ClassPath
                    classpath.add("\"" + File(installation, artifact.asJsonObject["name"].asString).path + "\"")
                }

                "EXTERNAL_FILE" -> {
                    // is external file
                    ichorPath.add("\"" + File(artifact.asJsonObject["name"].asString).path + "\"")
                }

                "NATIVES" -> {
                    // natives
                    natives = File(installation, artifact.asJsonObject["name"].asString)
                }
            }
        }
        if (OSEnum.current == OSEnum.Windows) {
            args.add(java.lang.String.join(";", classpath))
        } else {
            args.add(java.lang.String.join(":", classpath))
        }
        // === main class ===
        args.add(LauncherData.getMainClass(json))
        // === game args ===
        val ichorEnabled = LauncherData.getIchorState(json)
        args.add("--version $version") // what version will lunarClient inject
        args.add("--accessToken 0")
        args.add("--userProperties {}")
        args.add("--launcherVersion 2.15.1")
        args.add("--hwid PUBLIC-HWID")
        args.add("--installationId INSTALLATION-ID")
        args.add("--workingDirectory $installation")
        args.add("--classpathDir $installation")
        args.add("--width " + gameArgs.width)
        args.add("--height " + gameArgs.height)
        args.add("--gameDir " + gameArgs.gameDir)
        args.add("--texturesDir " + File(installation, "textures").path)
        args.add("--uiDir " + File(installation, "ui").path)
        if (gameArgs.server != null) {
            args.add("--server " + gameArgs.server) // Join server after launch
        }
        args.add("--assetIndex " + version.substring(0, version.lastIndexOf(".")))
        if (ichorEnabled) {
            args.add("--ichorClassPath")
            args.add(java.lang.String.join(",", classpath))
            args.add("--ichorExternalFiles")
            args.add(java.lang.String.join(",", ichorPath))
        }
        args.add("--webosrDir")
        args.add("\"" + natives!!.path + "\"")
        // === custom game args ===
        for (arg in config.getValue("program-args").asJsonArray) {
            args.add(arg.asString)
        }
        // === finish ===
        return GameArgsResult(args, natives)
    }

    /**
     * Launch LunarClient with the online config
     *
     * @param version Minecraft version
     * @param module  LunarClient module
     * @param branch  Git branch (LunarClient)
     * @return natives file
     */


    fun launch(version: String, branch: String?, module: String?): File? {
        val installationDir = File(config.getValue("installation-dir").asString)

        log.info(String.format("Launching (%s, %s, %s)", version, module, branch))
        log.info("Generating launch params")
        val resize = config.getValue("resize").asJsonObject
        val width = resize["width"].asInt
        val height = resize["height"].asInt
        log.info(String.format("Resize: (%d, %d)", width, height))
        val gameArgs = GameArgs(width, height, File(config.getValue("game-dir").asString))
        val argsResult = getArgs(version, branch, module, installationDir, gameArgs) ?: return null
        val args = argsResult.args
        val argsString = java.lang.String.join(" ", args)
        val natives = argsResult.natives
        // dump launch script
        if (launchScript.delete()) {
            log.info("Delete launch script")
        }
        if (launchScript.createNewFile()) {
            log.info("Launch script was created")
        }
        FileWriter(launchScript, StandardCharsets.UTF_8).use { writer ->
            if (OSEnum.current == OSEnum.Windows) {
                // Microsoft Windows
                // NEW: Use CRLF (Windows 7)
                writer.write("@echo off\r\n")
                writer.write("chcp 65001\r\n") // unicode support for Windows which uses Chinese
                writer.write("@rem Generated by LunarCN (Celestial Launcher)\r\n@rem Website: https://www.lunarclient.top/\r\n")
                writer.write(String.format("@rem Version %s\r\n", GitUtils.buildVersion))
                writer.write("@rem Please donate to support us to continue develop https://www.lunarclient.top/donate\r\n")
                writer.write("@rem You can run this script to debug your game, or share this script to developers to resolve your launch problem\r\n")
                writer.write("cd /d $installationDir\r\n")
            } else {
                // Others
                writer.write("#!/bin/bash\n")
                writer.write("# Generated by LunarCN (Celestial Launcher)\n# Website: https://www.lunarclient.top/\n")
                writer.write("# Please donate to support us to continue develop https://www.lunarclient.top/donate\n")
                writer.write("# You can run this script to debug your game, or share this script to developers to resolve your launch problem\n")
                writer.write("cd $installationDir\n")
            }
            writer.write("\n")
            writer.write(argsString)
        }
        log.info("Args was dumped to $launchScript")
        log.info("Natives file: $natives")
        // check non-ascii chars on Microsoft Windows
        if (OSEnum.current == OSEnum.Windows && argsString.hasNonAscii()) {
            JOptionPane.showMessageDialog(this.launcherFrame, f.getString("gui.non-ascii.warn"), "Warning", JOptionPane.WARNING_MESSAGE)
        }
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
        val installation = File(config.getValue("installation-dir").asString)
        val versionJson = launcherData.getVersion(version, branch, module)
        // download artifacts
        val artifacts = LauncherData.getArtifacts(versionJson)
        artifacts.forEach { (name: String?, info: Map<String, String>) ->
            try {
                DownloadManager.download(Downloadable(URL(info["url"]), File(installation, name), info["sha1"]!!))
            } catch (e: MalformedURLException) {
                throw RuntimeException(e)
            }
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

        val minecraftFolder = File(config.getValue("game-dir").asString)

        GuiLauncher.statusBar.text = "Complete textures for vanilla Minecraft"
        val textureIndex = MinecraftData.getVersion(
            version,
            minecraftManifest
        )!!.let {
            MinecraftData.getTextureIndex(
                it
            )
        }
        // dump to .minecraft/assets/indexes
        val assetsFolder = File(minecraftFolder, "assets")
        val indexFile = File(
            assetsFolder,
            "indexes/" + java.lang.String.join(
                ".",
                *Arrays.copyOfRange(version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray(), 0, 2)) + ".json")
        FileUtils.writeStringToFile(indexFile, Gson().toJson(textureIndex), StandardCharsets.UTF_8)

        val objects = textureIndex.getAsJsonObject("objects").asMap()
        // baseURL/hash[0:2]/hash
        for (s in objects.values) {
            val resource = s.asJsonObject
            val hash = resource["hash"].asString
            val folder = hash.substring(0, 2)
            val finalURL = URL(String.format("%s/%s/%s", MinecraftData.texture, folder, hash))
            val finalFile = File(assetsFolder, "objects/$folder/$hash")
            DownloadManager.download(Downloadable(finalURL, finalFile, hash))
        }
    }
}

private fun File.isReallyOfficial(): Boolean {
    val json = JsonParser.parseString(FileUtils.readFileToString(this, StandardCharsets.UTF_8)).asJsonObject
    return !json.has("celestial")
}
