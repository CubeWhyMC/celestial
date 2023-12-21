package org.cubewhy.celestial;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.google.gson.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.files.ConfigFile;
import org.cubewhy.celestial.game.AuthServer;
import org.cubewhy.celestial.game.GameArgs;
import org.cubewhy.celestial.game.GameArgsResult;
import org.cubewhy.celestial.game.JavaAgent;
import org.cubewhy.celestial.gui.GuiLauncher;
import org.cubewhy.celestial.utils.*;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

@Slf4j
public class Celestial {


    public static final File configDir = new File(System.getProperty("user.home"), ".cubewhy/lunarcn");
    public static final ConfigFile config = new ConfigFile(new File(configDir, "celestial.json"));
    public static Locale locale;
    public static String userLanguage;
    public static ResourceBundle f;

    public static LauncherData launcherData;
    public static JsonObject metadata;
    public static GuiLauncher launcherFrame;
    public static boolean themed = true;
    public static String os = System.getProperty("os.name");
    public static final File launchScript = new File(configDir, (os.contains("Windows")) ? "launch.bat" : "launch.sh");
    public static final File logFile = new File(configDir, "latest.log");
    public static final boolean isDevelopMode = System.getProperties().containsKey("dev-mode");
    public static long gamePid = 0;

    public static void main(String[] args) throws Exception {
        log.info("Celestial v" + GitUtils.getBuildVersion() + " build by " + GitUtils.getBuildUser());
        try {
            System.setProperty("file.encoding", "UTF-8");
            run(args);
        } catch (Exception e) {
            String trace = TextUtils.dumpTrace(e);
            log.error(trace);
            StringBuffer message = new StringBuffer("Celestial Crashed");
            if (config.getConfig().has("data-sharing") && config.getValue("data-sharing").getAsBoolean()) {
                log.info("Uploading crash report");
                Map<String, String> map = launcherData.uploadCrashReport(trace, CrashReportType.LAUNCHER, null);
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
        initTheme(); // init theme
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
        launcherFrame.setVisible(true);
        launcherFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static void checkJava() {
        String javaVersion = System.getProperty("java.specification.version");
        log.info("Celestial is running on Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty("java.vm.version") + "(" + System.getProperty("java.vendor") + ") Arch: " + System.getProperty("os.arch"));

        if (!javaVersion.equals("17")) {
            log.warn("Compatibility warning: The Java you are currently using may not be able to start LunarClient properly (Java 17 is recommended)");
            JOptionPane.showMessageDialog(null, f.getString("compatibility.warn.message"), f.getString("compatibility.warn.title"), JOptionPane.WARNING_MESSAGE);
        }

        // detect the official launcher (Windows only)
        if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
            File sessionFile = new File(System.getenv("APPDATA"), "launcher/sentry/session.json");
            if (sessionFile.exists()) {
                log.warn("Detected the official launcher");
                JOptionPane.showMessageDialog(null, f.getString("warn.official-launcher.message"), f.getString("warn.official-launcher.title"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public static void initLauncher() throws IOException {
        metadata = launcherData.metadata();
        if (metadata.has("error")) {
            // trouble here
            log.error("Metadata info: " + metadata);
            throw new IllegalStateException("metadata API Error!");
        }
    }

    private static void initConfig() {
        JsonObject resize = new JsonObject();
        resize.addProperty("width", 854);
        resize.addProperty("height", 480);

        config.initValue("jre", "") // leave empty if you want to use the default one
                .initValue("language", "zh") // en, zh
                .initValue("installation-dir", new File(configDir, "game").getPath())
                .initValue("game-dir", getMinecraftFolder().getPath()) // the minecraft folder
                .initValue("game", new JsonNull())
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
        return new File(System.getProperty("user.home", ".minecraft"));
    }

    public static void initTheme() throws IOException {
        String themeType = config.getValue("theme").getAsString();
        log.info("Set theme -> " + themeType);
        switch (themeType) {
            case "dark" -> {
                FlatDarkLaf.setup();
            }
            case "light" -> {
                FlatLightLaf.setup();
            }
            case "unset" -> {
                // do nothing
                themed = false;
            }
            default -> {
                File themeFile = new File(configDir, "themes/" + themeType);
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
        if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
            // Windows
            // delete the log file
            log.info("delete the log file");
            logFile.delete();
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(System.getenv("WINDIR") + "/System32/cmd.exe", "/C \"" + launchScript.getPath() + String.format(" 1>>\"%s\" 2>&1\"", logFile.getPath()));
            return builder;
        } else {
            // others
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("/bin/bash", "\"" + launchScript.getPath() + "\"");
            return builder;
        }
    }

    /**
     * Get args
     */
    @NotNull
    public static GameArgsResult getArgs(String version, String branch, String module, File installation, GameArgs gameArgs) throws IOException {
        List<String> args = new ArrayList<>();
        JsonObject json = launcherData.getVersion(version, branch, module);
        // === JRE ===
        String wrapper = config.getValue("wrapper").getAsString();
        String customJre = config.getValue("jre").getAsString();
        if (!wrapper.isBlank()) {
            log.warn("Launch the game via the wrapper: " + wrapper);
            args.add(wrapper);
        }
        if (customJre.isEmpty()) {
            args.add("\"" + System.getProperty("java.home") + "/bin/java\""); // Note: Java may not be found through this method on some non-Windows computers. You can manually specify the Java executable file.
        } else {
            args.add("\"" + customJre + "\"");
        }
        // === default vm args ===
        args.addAll(LauncherData.getDefaultJvmArgs(json, installation));
        // === javaagents ===
        List<JavaAgent> javaAgents = JavaAgent.findAll();
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
        args.add(String.join(";", classpath));
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
        args.add("--texturesDir " + gameArgs.getTexturesDir());
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
     * @return error message
     */
    @Nullable
    public static ProcessBuilder launch(String version, String branch, String module) throws IOException {
        File installationDir = new File(config.getValue("installation-dir").getAsString());

        log.info(String.format("Launching (%s, %s, %s)", version, module, branch));
        log.info("Generating launch params");
        JsonObject resize = config.getValue("resize").getAsJsonObject();
        int width = resize.get("width").getAsInt();
        int height = resize.get("height").getAsInt();
        log.info(String.format("Resize: (%d, %d)", width, height));
        GameArgs gameArgs = new GameArgs(width, height, new File(config.getValue("game-dir").getAsString()), new File(config.getValue("game-dir").getAsString(), "textures"));
        Celestial.launcherData = new LauncherData("https://api.lunarclientprod.com");
        GameArgsResult argsResult = Celestial.getArgs(version, branch, module, installationDir, gameArgs);
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
        try (FileWriter writer = new FileWriter(launchScript)) {
            if (OSEnum.getCurrent().equals(OSEnum.Windows)) {
                // Microsoft Windows
                writer.write("@echo off\n");
                writer.write("rem Generated by LunarCN (Celestial Launcher)\nrem Website: https://www.lunarclient.top/\n");
                writer.write("rem Please donate to support us to continue develop https://www.lunarclient.top/donate\n");
                writer.write("rem You can run this script to debug your game, or share this script to developers to resolve your launch problem\n");
                writer.write("cd /d " + installationDir + "\n");
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
        log.info("Unzipping natives...");
        try {
            FileUtils.unzipNatives(natives, installationDir);
        } catch (Exception e) {
            String trace = TextUtils.dumpTrace(e);
            log.error("Is game launched? Failed to unzip natives.");
            log.error(trace);
            return null;
        }
        // exec, run
        log.info("Everything is OK, starting game...");
        return launch(); // success
    }

    /**
     * Check and download updates for game
     *
     * @param version Minecraft version
     * @param module  LunarClient module
     * @param branch  Git branch (LunarClient)
     */
    public static void checkUpdate(String version, String module, String branch) throws IOException {

    }
}
