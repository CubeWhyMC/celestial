package org.cubewhy.celestial.game;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.cubewhy.celestial.Celestial.config;

@Getter
@Slf4j
public class JavaAgent {
    public static final File javaAgentFolder = new File(System.getProperty("user.home"), ".cubewhy/lunarcn/javaagents"); // share with LunarCN Launcher

    static {
        if (!javaAgentFolder.exists()) {
            log.info("Making javaagents folder");
            javaAgentFolder.mkdirs(); // create the javaagents folder
        }
    }

    /**
     * -- GETTER --
     *  Get agent arg
     *
     */
    private String arg = "";
    /**
     * -- GETTER --
     *  Get a file of the javaagent
     *
     */
    private final File file;

    /**
     * Create an instance
     *
     * @param path Path to JavaAgent
     * @param arg  Arg of the JavaAgent
     */
    public JavaAgent(String path, String arg) {
        this.file = new File(path);
        this.arg = arg;
    }

    /**
     * Create a instance
     *
     * @param file File of the JavaAgent
     * @param arg  Arg of the JavaAgent
     */
    public JavaAgent(File file, String arg) {
        this.file = file;
        this.arg = arg;
    }

    /**
     * Create a instance
     *
     * @param file File of the JavaAgent
     */
    public JavaAgent(File file) {
        this.file = file;
    }

    /**
     * Create a instance
     *
     * @param path Path to JavaAgent
     */
    public JavaAgent(String path) {
        this.file = new File(path);
    }

    /**
     * Find all javaagents in the javaagent folder
     * */
    @NotNull
    @Contract(pure = true)
    public static List<JavaAgent> findAll() {
        List<JavaAgent> list = new ArrayList<>();
        for (File file : Objects.requireNonNull(javaAgentFolder.listFiles())) {
            if (file.getName().endsWith(".jar") && file.isFile()) {
                list.add(new JavaAgent(file, findAgentArg(file.getName())));
            }
        }
        return list;
    }

    private static String findAgentArg(String name) {
        JsonObject ja = config.getValue("javaagents").getAsJsonObject();
        if (!ja.has(name)) {
            // create config for the agent
            ja.addProperty(name, ""); // leave empty
            config.setValue("javaagents", ja); // dump
        }
        return ja.get(name).getAsString();
    }

    /**
     * Get args which add to the jvm
     *
     * @return args for jvm
     */
    public String getJvmArg() {
        String jvmArgs = "-javaagent:\"" + this.file.getAbsolutePath() + "\"";
        if (!this.arg.isEmpty()) {
            if (this.arg.contains(" ")) {
                jvmArgs += "=\"" + this.arg + "\"";
            } else {
                jvmArgs += "=" + this.arg;
            }
        }
        return jvmArgs;
    }
}
