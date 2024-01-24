/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * The managed process.
 *
 * @author huangyuhui
 */
class ManagedProcess {
    /**
     * -- GETTER --
     * The raw system process that this instance manages.
     *
     * @return process
     */
    val process: Process

    /**
     * -- GETTER --
     * The command line.
     *
     * @return the list of each part of command line separated by spaces.
     */
    private val commands: List<String>

    /**
     * -- GETTER --
     * The classpath.
     *
     * @return classpath
     */
    private val classpath: String?

    /**
     * -- GETTER --
     * To save some information you need.
     */
    val properties: Map<String, Any> = HashMap()
    private val lines: MutableList<String> = ArrayList()
    private val relatedThreads: MutableList<Thread> = ArrayList()

    constructor(processBuilder: ProcessBuilder) {
        this.process = processBuilder.start()
        this.commands = processBuilder.command()
        this.classpath = null
    }

    /**
     * Constructor.
     *
     * @param process  the raw system process that this instance manages.
     * @param commands the command line of `process`.
     */
    constructor(process: Process, commands: List<String>) {
        this.process = process
        this.commands = Collections.unmodifiableList(ArrayList(commands))
        this.classpath = null
    }

    /**
     * Constructor.
     *
     * @param process   the raw system process that this instance manages.
     * @param commands  the command line of `process`.
     * @param classpath the classpath of java process
     */
    constructor(process: Process, commands: List<String>, classpath: String?) {
        this.process = process
        this.commands = Collections.unmodifiableList(ArrayList(commands))
        this.classpath = classpath
    }

    /**
     * The (unmodifiable) standard output/error lines.
     * If you want to add lines, use [.addLine]
     *
     * @see .addLine
     */
    @Synchronized
    fun getLines(lineFilter: Predicate<String?>?): List<String> {
        if (lineFilter == null) return Collections.unmodifiableList(Arrays.asList(*lines.toTypedArray<String>()))

        val res = ArrayList<String>()
        for (line in this.lines) {
            if (lineFilter.test(line)) res.add(line)
        }
        return Collections.unmodifiableList(res)
    }

    @Synchronized
    fun addLine(line: String) {
        lines.add(line)
    }

    /**
     * Add related thread.
     *
     *
     * If a thread is monitoring this raw process,
     * you are required to add the instance by this method.
     */
    @Synchronized
    fun addRelatedThread(thread: Thread) {
        relatedThreads.add(thread)
    }

    //    public synchronized void pumpInputStream(Consumer<String> onLogLine) {
    //        addRelatedThread(Lang.thread(new StreamPump(process.getInputStream(), onLogLine, OperatingSystem.NATIVE_CHARSET), "ProcessInputStreamPump", true));
    //    }
    //
    //    public synchronized void pumpErrorStream(Consumer<String> onLogLine) {
    //        addRelatedThread(Lang.thread(new StreamPump(process.getErrorStream(), onLogLine, OperatingSystem.NATIVE_CHARSET), "ProcessErrorStreamPump", true));
    //    }
    private val isRunning: Boolean
        /**
         * True if the managed process is running.
         */
        get() {
            try {
                process.exitValue()
                return false
            } catch (e: IllegalThreadStateException) {
                return true
            }
        }

    val exitCode: Int
        /**
         * The exit code of raw process.
         */
        get() = process.exitValue()

    /**
     * Destroys the raw process and other related threads that are monitoring this raw process.
     */
    fun stop() {
        process.destroy()
        destroyRelatedThreads()
    }

    @Synchronized
    fun destroyRelatedThreads() {
        relatedThreads.forEach(Consumer { obj: Thread -> obj.interrupt() })
    }

    override fun toString(): String {
        return "ManagedProcess[commands=" + commands + ", isRunning=" + isRunning + "]"
    }
}
