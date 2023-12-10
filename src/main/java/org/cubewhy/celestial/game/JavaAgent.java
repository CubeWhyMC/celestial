package org.cubewhy.celestial.game;

import java.io.File;

public class JavaAgent {
    private String arg = "";
    private final File file;

    /**
     * Create a instance
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
     * Get agent arg
     *
     * @return args
     */
    public String getArg() {
        return arg;
    }

    /**
     * Get a file of the javaagent
     *
     * @return file
     */
    public File getFile() {
        return file;
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
