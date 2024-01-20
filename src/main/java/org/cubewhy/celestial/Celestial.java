package org.cubewhy.celestial;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.google.gson.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.event.impl.CreateLauncherEvent;
import org.cubewhy.celestial.files.ConfigFile;
import org.cubewhy.celestial.files.DownloadManager;
import org.cubewhy.celestial.files.Downloadable;
import org.cubewhy.celestial.game.AuthServer;
import org.cubewhy.celestial.game.GameArgs;
import org.cubewhy.celestial.game.GameArgsResult;
import org.cubewhy.celestial.game.addon.JavaAgent;
import org.cubewhy.celestial.gui.GuiLauncher;
import org.cubewhy.celestial.utils.*;
import org.cubewhy.celestial.utils.game.MinecraftData;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.cubewhy.celestial.gui.GuiLauncher.statusBar;

@Slf4j
public class Celestial {


    public static final File configDir = new File(System.getProperty("user.home"), ".cubewhy/lunarcn");
    public static final File themesDir = new File(configDir, "themes");
    public static final ConfigFile config = new ConfigFile(new File(configDir, "celestial.json"));
    public static Locale locale;
    public static String userLanguage;
    public static ResourceBundle f;

    public static LauncherData launcherData;
    public static JsonObject metadata;
    public static JsonObject minecraftManifest;
    public static GuiLauncher launcherFrame;
    public static boolean themed = true;
    public static String os = System.getProperty("os.name");
    public static final File launchScript = new File(configDir, (os.contains("Windows")) ? "launch.bat" : "launch.sh");
    public static final File gameLogFile = new File(configDir, "logs/game.log");
    public static final File launcherLogFile = new File(configDir, "logs/launcher.log");
    public static final boolean isDevelopMode = System.getProperties().containsKey("dev-mode");
    public static final AtomicLong gamePid = new AtomicLong();
    public static File sessionFile;

    static {
        if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
            // Microsoft Windows
            sessionFile = new File(System.getenv("APPDATA"), "launcher/sentry/session.json");
        } else {
            // Linux, Macos... etc.
            sessionFile = new File(System.getProperty("user.home"), ".config/launcher/sentry/session.json");
        }
    }

    public static void main(String[] args) throws Exception {
        // set encoding
        System.setProperty("file.encoding", "UTF-8");
        log.info("Celestial v" + GitUtils.getBuildVersion() + " build by " + GitUtils.getBuildUser());
        try {
            System.setProperty("file.encoding", "UTF-8");
            run(args);
        } catch (Exception e) {
            String trace = TextUtils.dumpTrace(e);
            log.error(trace);
            // please share the crash report with developers to help us solve the problems of the Celestial Launcher
            StringBuffer message = new StringBuffer("Celestial Crashed\n");
            message.append("Launcher Version: ").append(GitUtils.getBuildVersion()).append("\n");
            if (config.getConfig().has("data-sharing") && config.getValue("data-sharing").getAsBoolean()) {
                log.info("Uploading crash report");
                String logString = org.apache.commons.io.FileUtils.readFileToString(launcherLogFile, StandardCharsets.UTF_8);
                Map<String, String> map = launcherData.uploadCrashReport(logString, CrashReportType.LAUNCHER, null);
                if (map.isEmpty()) {
                    log.info("Crash report update failed");
                } else {
                    log.info("Upload success, reportID is " + map.get("id"));
                    message.append("Report id: ").append(map.get("id")).append("\n").append("View the report: ").append(map.get("url")).append("\n");
                }
            }
            message.append(trace);
            JOptionPane.showMessageDialog(null, message, "Oops, Celestial crashed", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }


    private static void run(String[] args) throws Exception {
        // init config
        initConfig();
        initTheme(); // init theme

        OptionParser optionParser = new OptionParser();
        optionParser.accepts("help", "Print help and exit")
                .withOptionalArg();
        optionParser.accepts("api", "LunarClient api")
                .withOptionalArg()
                .defaultsTo(config.getValue("api").getAsString())
                .ofType(String.class);
        optionParser.accepts("game", "version:module:branch")
                .withOptionalArg()
                .ofType(String.class);
        optionParser.accepts("offline", "Launch with offline")
                .availableIf("game");
        optionParser.accepts("no-update", "Disable game update")
                .availableIf("game")
                .availableUnless("offline");
        OptionSet options = optionParser.parse(args);
        if (options.has("help")) {
            optionParser.printHelpOn(System.out);
            System.exit(0);
        } else if (options.has("game")) {
            // launch game
            log.info("Celestial - CommandLine");
            log.warn("Command line startup is experimental. If any problems occur, please provide rational feedback.");
            launcherData = new LauncherData((String) options.valueOf("api"));
            String[] game = ((String) options.valueOf("game")).split(":");
            if (game.length != 3) {
                log.error("Arg game must be version:module:branch");
                System.exit(1);
            }
            String version = game[0];
            String module = game[1];
            String branch = game[2];
            if (options.has("offline")) {
                launch();
            } else {
                if (!options.has("no-update")) {
                    checkUpdate(version, module, branch);
                }
                launch(version, module, branch);
            }
            System.exit(0);
        }
        log.info("Language: " + userLanguage);
        checkJava();
        launcherData = new LauncherData(config.getValue("api").getAsString());
        while (true) {
            try {
                // I don't know why my computer crashed here if the connection takes too much time :(
                log.info("Starting connect to the api -> " + launcherData.api.toString());
                initLauncher();
                log.info("connected");
                break; // success
            } catch (Exception e) {
                String trace = TextUtils.dumpTrace(e);
                log.error(trace);
                // shell we switch a api?
                String input = JOptionPane.showInputDialog(f.getString("api.unreachable"), config.getValue("api").getAsString());
                if (input == null) {
                    System.exit(1);
                } else {
                    launcherData = new LauncherData(input);
                    config.setValue("api", input);
                }
            }
        }

        // start auth server
        AuthServer.getInstance().startServer();

        // start gui launcher

        launcherFrame = new GuiLauncher();
        new CreateLauncherEvent(launcherFrame).call();
        launcherFrame.setVisible(true);
        launcherFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static void checkJava() throws IOException {
        String javaVersion = System.getProperty("java.specification.version");
        log.info("Celestial is running on Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty("java.vm.version") + "(" + System.getProperty("java.vendor") + ") Arch: " + System.getProperty("os.arch"));

        if (!javaVersion.equals("17")) {
            log.warn("Compatibility warning: The Java you are currently using may not be able to start LunarClient properly (Java 17 is recommended)");
            JOptionPane.showMessageDialog(null, f.getString("compatibility.warn.message"), f.getString("compatibility.warn.title"), JOptionPane.WARNING_MESSAGE);
        }

        if (sessionFile.exists() && LunarUtils.isReallyOfficial(sessionFile)) {
            log.warn("Detected the official launcher");
            JOptionPane.showMessageDialog(null, f.getString("warn.official-launcher.message"), f.getString("warn.official-launcher.title"), JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void initLauncher() throws IOException {
        metadata = launcherData.metadata();
        minecraftManifest = MinecraftData.manifest();
        if (metadata.has("error")) {
            // trouble here
            log.error("Metadata info: " + metadata);
            throw new IllegalStateException("metadata API Error!");
        }
    }

    private static void initConfig() {
        // init dirs
        if (configDir.mkdirs()) {
            log.info("Making config dir");
        }
        if (themesDir.mkdirs()) {
            log.info("Making themes dir");
        }
        // init config
        JsonObject resize = new JsonObject();
        resize.addProperty("width", 854);
        resize.addProperty("height", 480);

        JsonObject addon = new JsonObject();
        // weave
        JsonObject weave = new JsonObject();
        weave.addProperty("enable", false);
        weave.addProperty("installation", new File(System.getProperty("user.home"), ".cubewhy/lunarcn/loaders/weave.jar").getPath());
        weave.addProperty("check-update", true);
        addon.add("weave", weave);
        // lccn
        JsonObject lunarcn = new JsonObject();
        lunarcn.addProperty("enable", false);
        lunarcn.addProperty("installation", new File(System.getProperty("user.home"), ".cubewhy/lunarcn/loaders/cn.jar").getPath());
        lunarcn.addProperty("check-update", true);
        addon.add("lunarcn", lunarcn);

        config.initValue("jre", "") // leave empty if you want to use the default one
                .initValue("language", "zh") // en, zh
                .initValue("installation-dir", new File(configDir, "game").getPath())
                .initValue("game-dir", getMinecraftFolder().getPath()) // the minecraft folder
                .initValue("game", (JsonElement) null)
                .initValue("addon", addon)
                .initValue("ram", 4096)
                .initValue("max-threads", Runtime.getRuntime().availableProcessors()) // recommend: same as your CPU core
                .initValue("api", "https://api.lunarclient.top") // only support the LunarCN api, Moonsworth's looks like shit :(
                .initValue("theme", "dark") // dark, light, unset, custom.
                .initValue("resize", resize) // (854, 480) for default
                .initValue("vm-args", new JsonArray()) // custom jvm args
                .initValue("wrapper", "") // like optirun on linux
                .initValue("program-args", new JsonArray()) // args of the game
                .initValue("javaagents", new JsonObject()); // lc addon
        // init language
        log.info("Initializing language manager");
        locale = Locale.forLanguageTag(config.getValue("language").getAsString());
        userLanguage = locale.getLanguage();
        f = ResourceBundle.getBundle("languages/launcher", locale);

        if (!config.getConfig().has("data-sharing")) {
            boolean b = JOptionPane.showConfirmDialog(null, f.getString("data-sharing.confirm.message"), f.getString("data-sharing.confirm.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
            config.initValue("data-sharing", new JsonPrimitive(b));
        }
    }

    /**
     * Get the default .minecraft folder
     * <p></p>
     * Windows: %APPDATA%/.minecraft
     * Linux/MacOS: ~/.minecraft
     */
    @NotNull
    @Contract(" -> new")
    private static File getMinecraftFolder() {
        OSEnum os = OSEnum.getCurrent();
        if (os.equals(OSEnum.Windows)) {
            return new File(System.getenv("APPDATA"), ".minecraft");
        }
        return new File(System.getProperty("user.home"), ".minecraft");
    }

    public static void initTheme() throws IOException {
        String themeType = config.getValue("theme").getAsString();
        log.info("Set theme -> " + themeType);
        switch (themeType) {
            case "dark" -> FlatDarkLaf.setup();
            case "light" -> FlatLightLaf.setup();
            case "unset" -> // do nothing
                    themed = false;
            default -> {
                File themeFile = new File(themesDir, themeType);
                if (!themeFile.exists()) {
                    // cannot load custom theme without theme.json
                    JOptionPane.showMessageDialog(null, f.getString("theme.custom.notfound.message"), f.getString("theme.custom.notfound.title"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                InputStream stream = Files.newInputStream(themeFile.toPath());
                IntelliJTheme.setup(stream); // load theme
            }
        }
    }

    /**
     * Launch LunarClient offline
     */
    @NotNull
    public static ProcessBuilder launch() throws IOException {
        // wrapper was applied in the script
        log.info("Launching with script");
        log.info("delete the log file");
        gameLogFile.delete();
        if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
            // Windows
            // delete the log file
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(System.getenv("WINDIR") + "/System32/cmd.exe", "/C \"" + launchScript.getPath() + String.format(" 1>>\"%s\" 2>&1\"", gameLogFile.getPath()));
            return builder;
        } else {
            // others
            // do chmod
            Runtime.getRuntime().exec("chmod 777 " + launchScript.getPath());
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("/bin/bash", "-c", "\"" + launchScript.getPath() + "\" > \"" + gameLogFile.getPath() + "\"");
            return builder;
        }
    }

    /**
     * Get args
     */
    @Nullable
    public static GameArgsResult getArgs(String version, String branch, String module, File installation, GameArgs gameArgs) throws IOException {
        List<String> args = new ArrayList<>();
        JsonObject json = launcherData.getVersion(version, branch, module);
        if (!json.get("success").getAsBoolean()) {
            return null;
        }
        // === JRE ===
        String wrapper = config.getValue("wrapper").getAsString();
        String customJre = config.getValue("jre").getAsString();
        if (!wrapper.isBlank()) {
            log.warn("Launch the game via the wrapper: " + wrapper);
            args.add(wrapper);
        }
        if (customJre.isEmpty()) {
            File java = SystemUtils.getCurrentJavaExec();
            if (!java.exists()) {
                log.error("Java executable not found, please specify it in " + config.file);
            }
            log.info("Use default jre: " + java);
            args.add("\"" + java.getPath() + "\""); // Note: Java may not be found through this method on some non-Windows computers. You can manually specify the Java executable file.
        } else {
            log.info("Use custom jre: " + customJre);
            args.add("\"" + customJre + "\"");
        }
        // === default vm args ===
        long ram = config.getValue("ram").getAsLong();
        log.info("RAM: " + ram + "MB");
        args.add("-Xms" + ram + "m");
        args.add("-Xmx" + ram + "m");
        args.addAll(LauncherData.getDefaultJvmArgs(json, installation));
        // === javaagents ===
        List<JavaAgent> javaAgents = JavaAgent.findEnabled();
        int size = javaAgents.size();
        if (size != 0) {
            log.info(String.format("Found %s javaagent%s (Except loaders)", size, (size == 1) ? "" : "s"));
        }
        // ===     loaders    ===
        JsonObject weave = config.getValue("addon").getAsJsonObject().getAsJsonObject("weave");
        JsonObject cn = config.getValue("addon").getAsJsonObject().getAsJsonObject("lunarcn");
        if (weave.get("enable").getAsBoolean()) {
            String file = weave.get("installation").getAsString();
            log.info("Weave enabled! " + file);
            javaAgents.add(new JavaAgent(file));
        }
        if (cn.get("enable").getAsBoolean()) {
            String file = cn.get("installation").getAsString();
            log.info("LunarCN enabled! " + file);
            javaAgents.add(new JavaAgent(file));
        }
        for (JavaAgent agent : javaAgents) {
            args.add(agent.getJvmArg());
        }
        // === custom vm args ===
        List<String> customVMArgs = new ArrayList<>();
        for (JsonElement jsonElement : config.getValue("vm-args").getAsJsonArray()) {
            customVMArgs.add(jsonElement.getAsString());
        }
        args.addAll(customVMArgs);
        // === classpath ===
        List<String> classpath = new ArrayList<>();
        List<String> ichorPath = new ArrayList<>();
        File natives = null;
        args.add("-cp");
        for (JsonElement artifact :
                json
                        .getAsJsonObject("launchTypeData")
                        .getAsJsonArray("artifacts")) {
            if (artifact.getAsJsonObject().get("type").getAsString().equals("CLASS_PATH")) {
                // is ClassPath
                classpath.add("\"" + new File(installation, artifact.getAsJsonObject().get("name").getAsString()).getPath() + "\"");
            } else if (artifact.getAsJsonObject().get("type").getAsString().equals("EXTERNAL_FILE")) {
                // is external file
                ichorPath.add("\"" + new File(artifact.getAsJsonObject().get("name").getAsString()).getPath() + "\"");
            } else if (artifact.getAsJsonObject().get("type").getAsString().equals("NATIVES")) {
                // natives
                natives = new File(installation, artifact.getAsJsonObject().get("name").getAsString());
            }
        }
        if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
            args.add(String.join(";", classpath));
        } else {
            args.add(String.join(":", classpath));
        }
        // === main class ===
        args.add(LauncherData.getMainClass(json));
        // === game args ===
        boolean ichorEnabled = LauncherData.getIchorState(json);
        args.add("--version " + version); // what version will lunarClient inject
        args.add("--accessToken 0");
        args.add("--userProperties {}");
        args.add("--launcherVersion 2.15.1");
        args.add("--hwid PUBLIC-HWID");
        args.add("--installationId INSTALLATION-ID");
        args.add("--workingDirectory " + installation);
        args.add("--classpathDir " + installation);
        args.add("--width " + gameArgs.getWidth());
        args.add("--height " + gameArgs.getHeight());
        args.add("--gameDir " + gameArgs.getGameDir());
        args.add("--texturesDir " + new File(installation, "textures").getPath());
        args.add("--uiDir " + new File(installation, "ui").getPath());
        if (gameArgs.getServer() != null) {
            args.add("--server " + gameArgs.getServer()); // Join server after launch
        }
        args.add("--assetIndex " + version.substring(0, version.lastIndexOf(".")));
        if (ichorEnabled) {
            args.add("--ichorClassPath");
            args.add(String.join(",", classpath));
            args.add("--ichorExternalFiles");
            args.add(String.join(",", ichorPath));
        }
        args.add("--webosrDir");
        args.add("\"" + natives.getPath() + "\"");
        // === custom game args ===
        for (JsonElement arg : config.getValue("program-args").getAsJsonArray()) {
            args.add(arg.getAsString());
        }
        // === finish ===
        return new GameArgsResult(args, natives);
    }

    /**
     * Launch LunarClient with the online config
     *
     * @param version Minecraft version
     * @param module  LunarClient module
     * @param branch  Git branch (LunarClient)
     * @return natives file
     */
    @Nullable
    public static File launch(String version, String branch, String module) throws IOException {
        File installationDir = new File(config.getValue("installation-dir").getAsString());

        log.info(String.format("Launching (%s, %s, %s)", version, module, branch));
        log.info("Generating launch params");
        JsonObject resize = config.getValue("resize").getAsJsonObject();
        int width = resize.get("width").getAsInt();
        int height = resize.get("height").getAsInt();
        log.info(String.format("Resize: (%d, %d)", width, height));
        GameArgs gameArgs = new GameArgs(width, height, new File(config.getValue("game-dir").getAsString()));
        GameArgsResult argsResult = Celestial.getArgs(version, branch, module, installationDir, gameArgs);
        if (argsResult == null) {
            return null;
        }
        List<String> args = argsResult.args();
        String argsString = String.join(" ", args);
        File natives = argsResult.natives();
        // dump launch script
        if (launchScript.delete()) {
            log.info("Delete launch script");
        }
        if (launchScript.createNewFile()) {
            log.info("Launch script was created");
        }
        try (FileWriter writer = new FileWriter(launchScript, StandardCharsets.UTF_8)) {
            if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
                // Microsoft Windows
                // NEW: Use CRLF (Windows 7)
                writer.write("@echo off\r\n");
                writer.write("chcp 65001\r\n"); // unicode support for Windows which uses Chinese
                writer.write("@rem Generated by LunarCN (Celestial Launcher)\r\n@rem Website: https://www.lunarclient.top/\r\n");
                writer.write(String.format("@rem Version %s\r\n", GitUtils.getBuildVersion()));
                writer.write("@rem Please donate to support us to continue develop https://www.lunarclient.top/donate\r\n");
                writer.write("@rem You can run this script to debug your game, or share this script to developers to resolve your launch problem\r\n");
                writer.write("cd /d " + installationDir + "\r\n");
            } else {
                // Others
                writer.write("#!/bin/bash\n");
                writer.write("# Generated by LunarCN (Celestial Launcher)\n# Website: https://www.lunarclient.top/\n");
                writer.write("# Please donate to support us to continue develop https://www.lunarclient.top/donate\n");
                writer.write("# You can run this script to debug your game, or share this script to developers to resolve your launch problem\n");
                writer.write("cd " + installationDir + "\n");
            }
            writer.write("\n");
            writer.write(argsString);
        }
        log.info("Args was dumped to " + launchScript);
        log.info("Natives file: " + natives);
        return natives; // success
    }

    /**
     * Patching network disabling for LunarClient
     */
    public static void completeSession() throws IOException {
        if (!sessionFile.exists()) {
            log.info("Completing session.json to fix the network error for LunarClient");
            byte[] json;
            try (InputStream stream = FileUtils.inputStreamFromClassPath("/game/session.json")) {
                json = FileUtils.readBytes(stream);
            }
            org.apache.commons.io.FileUtils.writeStringToFile(sessionFile, String.valueOf(JsonParser.parseString(new String(json, StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        }
    }

    /**
     * Check and download updates for game
     *
     * @param version Minecraft version
     * @param module  LunarClient module
     * @param branch  Git branch (LunarClient)
     */
    public synchronized static void checkUpdate(String version, String module, String branch) throws IOException {
        log.info("Checking update");
        File installation = new File(config.getValue("installation-dir").getAsString());
        JsonObject versionJson = launcherData.getVersion(version, branch, module);
        // download artifacts
        Map<String, Map<String, String>> artifacts = LauncherData.getArtifacts(versionJson);
        artifacts.forEach((name, info) -> {
            try {
                DownloadManager.download(new Downloadable(new URL(info.get("url")), new File(installation, name), info.get("sha1")));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        // download textures
        Map<String, String> index = LauncherData.getLunarTexturesIndex(versionJson);
        assert index != null;
        index.forEach((fileName, urlString) -> {
            URL url;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            String[] full = urlString.split("/");
            File file = new File(installation, "textures/" + fileName);
            DownloadManager.download(new Downloadable(url, file, full[full.length - 1]));
        });

        File minecraftFolder = new File(config.getValue("game-dir").getAsString());

        // TODO vanilla Minecraft textures
        statusBar.setText("Complete textures for vanilla Minecraft");
        JsonObject textureIndex = MinecraftData.getTextureIndex(Objects.requireNonNull(MinecraftData.getVersion(version, minecraftManifest)));
        // dump to .minecraft/assets/indexes
        File assetsFolder = new File(minecraftFolder, "assets");
        File indexFile = new File(assetsFolder, "indexes/" + String.join(".", Arrays.copyOfRange(version.split("\\."), 0, 2)) + ".json");
        org.apache.commons.io.FileUtils.writeStringToFile(indexFile, new Gson().toJson(textureIndex), StandardCharsets.UTF_8);

        Map<String, JsonElement> objects = textureIndex.getAsJsonObject("objects").asMap();
        // baseURL/hash[0:2]/hash
        for (JsonElement s : objects.values()) {
            JsonObject resource = s.getAsJsonObject();
            String hash = resource.get("hash").getAsString();
            String folder = hash.substring(0, 2);
            URL finalURL = new URL(String.format("%s/%s/%s", MinecraftData.texture, folder, hash));
            File finalFile = new File(assetsFolder, "objects/" + folder + "/" + hash);
            DownloadManager.download(new Downloadable(finalURL, finalFile, hash));
        }
    }
}
