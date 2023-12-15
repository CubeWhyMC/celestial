package org.cubewhy.celestial;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.files.ConfigFile;
import org.cubewhy.celestial.gui.GuiLauncher;
import org.cubewhy.celestial.utils.GitUtils;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.cubewhy.celestial.utils.TextUtils;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
public class Celestial {


    public static final ConfigFile config = new ConfigFile(new File(System.getProperty("user.home"), ".cubewhy/lunarcn/celestial.json"));
    public static Locale locale;
    public static String userLanguage;
    public static ResourceBundle f;

    public static LauncherData launcherData;
    public static JsonObject metadata;
    public static GuiLauncher launcherFrame;
    public static boolean themed = true;

    public static void main(String[] args) {
        log.info("Celestial v" + GitUtils.getBuildVersion() + " build by " + GitUtils.getBuildUser());
        try {
            System.setProperty("file.encoding", "UTF-8");
            run(args);
        } catch (Exception e) {
            String trace = TextUtils.dumpTrace(e);
            log.error(trace);
            if (config.getConfig().has("data-sharing") && config.getValue("data-sharing").getAsBoolean()) {
                log.info("Uploading crash report");
            }
            JOptionPane.showMessageDialog(null, trace, "Oops, Celestial crashed", JOptionPane.ERROR_MESSAGE);
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
        // start gui launcher
        initTheme(); // init theme

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
        config.initValue("jre", "") // leave empty if you want to use the default one
                .initValue("language", "zh") // en, zh
                .initValue("api", "https://api.lunarclient.top") // only support the LunarCN api, Moonsworth's looks like shit :(
                .initValue("vm-args", new JsonArray()) // custom jvm args
                .initValue("program-args", new JsonArray()) // args of the game
                .initValue("javaagents", new JsonObject()) // lc addon
                .initValue("theme", "dark"); // dark, light, unset, custom.
        // init language
        locale = Locale.forLanguageTag(config.getValue("language").getAsString());
        userLanguage = locale.getLanguage();
        f = ResourceBundle.getBundle("launcher", locale);

        if (!config.getConfig().has("data-sharing")) {
            boolean b = JOptionPane.showConfirmDialog(null, f.getString("data-sharing.confirm.message"), f.getString("data-sharing.confirm.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION;
            config.initValue("data-sharing", new JsonPrimitive(b));
        }
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
                File themeFile = new File(System.getProperty("user.home"), ".cubewhy/lunarcn/themes/" + themeType);
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
    public static void launch() {

    }

    /**
     * Launch LunarClient with the online config
     *
     * @param version Minecraft version
     * @param module  LunarClient module
     * @param branch  Git branch (LunarClient)
     */
    public static void launch(String version, String module, String branch) {

    }

    /**
     * Check and download updates for game
     *
     * @param version Minecraft version
     * @param module  LunarClient module
     * @param branch  Git branch (LunarClient)
     */
    public static void checkUpdate(String version, String module, String branch) {

    }
}
