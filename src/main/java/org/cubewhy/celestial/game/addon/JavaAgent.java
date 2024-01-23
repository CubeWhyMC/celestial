/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game.addon;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.game.BaseAddon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.cubewhy.celestial.Celestial.config;
import static org.cubewhy.celestial.Celestial.configDir;

@Getter
@Slf4j
public class JavaAgent extends BaseAddon {
    public static final File javaAgentFolder = new File(configDir, "javaagents"); // share with LunarCN Launcher

    static {
        if (!javaAgentFolder.exists()) {
            log.info("Making javaagents folder");
            javaAgentFolder.mkdirs(); // create the javaagents folder
        }
    }

    /**
     * -- GETTER --
     * Get agent arg
     */
    private String arg = "";
    /**
     * -- GETTER --
     * Get a file of the javaagent
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
     * Find all mods in the weave mods folder
     */
    @NotNull
    @Contract(pure = true)
    public static List<JavaAgent> findEnabled() {
        List<JavaAgent> list = new ArrayList<>();
        if (javaAgentFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(javaAgentFolder.listFiles())) {
                if (file.getName().endsWith(".jar") && file.isFile()) {
                    list.add(new JavaAgent(file, JavaAgent.findAgentArg(file.getName())));
                }
            }
        }
        return list;
    }

    public static @NotNull List<JavaAgent> findDisabled() {
        List<JavaAgent> list = new ArrayList<>();
        if (javaAgentFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(javaAgentFolder.listFiles())) {
                if (file.getName().endsWith(".jar.disabled") && file.isFile()) {
                    list.add(new JavaAgent(file, JavaAgent.findAgentArg(file.getName())));
                }
            }
        }
        return list;
    }

    public static @NotNull List<JavaAgent> findAll() {
        List<JavaAgent> list = findEnabled();
        list.addAll(findDisabled());
        return Collections.unmodifiableList(list);
    }

    /**
     * Set param for a Javaagent
     *
     * @param agent the agent
     * @param arg   param of the agent
     */
    public static void setArgFor(@NotNull JavaAgent agent, String arg) {
        setArgFor(agent.file.getName(), arg);
    }

    /**
     * Set param for a Javaagent
     *
     * @param name name of the agent
     * @param arg  param of the agent
     */
    public static void setArgFor(String name, String arg) {
        JsonObject ja = config.getValue("javaagents").getAsJsonObject();
        ja.addProperty(name, arg);
        config.setValue("javaagents", ja); // dump
    }

    public static String findAgentArg(String name) {
        JsonObject ja = config.getValue("javaagents").getAsJsonObject();
        if (!ja.has(name)) {
            // create config for the agent
            ja.addProperty(name, ""); // leave empty
            config.setValue("javaagents", ja); // dump
        }
        return ja.get(name).getAsString();
    }

    public static @Nullable JavaAgent add(@NotNull File file, String arg) throws IOException {
        File target = autoCopy(file, javaAgentFolder);
        if (arg != null) {
            setArgFor(file.getName(), arg);
        }
        return (target == null) ? null : new JavaAgent(target, arg);
    }

    /**
     * Migrate the arg of an agent
     *
     * @param old name of the old agent
     * @param n3w name of the new agent
     */
    public static void migrate(String old, String n3w) {
        JsonObject ja = config.getValue("javaagents").getAsJsonObject();
        String arg;
        if (ja.get(old) == null && ja.get(old).isJsonNull()) {
            arg = null;
        } else {
            arg = ja.get(old).getAsString();
        }
        ja.addProperty(n3w, arg); // leave empty
        ja.remove(old);
        config.setValue("javaagents", ja); // dump
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

    @Override
    public String toString() {
        String result = this.file.getName();
        if (!this.arg.isBlank()) {
            result += "=" + this.arg;
        }
        return result;
    }

    @Override
    public boolean isEnabled() {
        return this.file.getName().endsWith(".jar");
    }

    @Override
    public boolean toggle() {
        if (isEnabled()) {
            migrate(file.getName(), file.getName() + ".disabled");
        } else {
            migrate(file.getName(), file.getName().substring(0, file.getName().length() - 9));
        }
        return toggle0(file);
    }
}
