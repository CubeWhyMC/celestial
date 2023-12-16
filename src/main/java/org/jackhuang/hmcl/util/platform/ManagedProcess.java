
package org.jackhuang.hmcl.util.platform;

import lombok.Getter;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

/**
 * The managed process.
 *
 * @author huangyuhui
 * @see org.jackhuang.hmcl.launch.StreamPump
 */
public class ManagedProcess {

    /**
     * -- GETTER --
     * The raw system process that this instance manages.
     *
     * @return process
     */
    @Getter
    private final Process process;
    /**
     * -- GETTER --
     * The command line.
     *
     * @return the list of each part of command line separated by spaces.
     */
    @Getter
    private final List<String> commands;
    /**
     * -- GETTER --
     * The classpath.
     *
     * @return classpath
     */
    @Getter
    private final String classpath;
    /**
     * -- GETTER --
     * To save some information you need.
     */
    @Getter
    private final Map<String, Object> properties = new HashMap<>();
    private final List<String> lines = new ArrayList<>();
    private final List<Thread> relatedThreads = new ArrayList<>();

    public ManagedProcess(ProcessBuilder processBuilder) throws IOException {
        this.process = processBuilder.start();
        this.commands = processBuilder.command();
        this.classpath = null;
    }

    /**
     * Constructor.
     *
     * @param process  the raw system process that this instance manages.
     * @param commands the command line of {@code process}.
     */
    public ManagedProcess(Process process, List<String> commands) {
        this.process = process;
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
        this.classpath = null;
    }

    /**
     * Constructor.
     *
     * @param process   the raw system process that this instance manages.
     * @param commands  the command line of {@code process}.
     * @param classpath the classpath of java process
     */
    public ManagedProcess(Process process, List<String> commands, String classpath) {
        this.process = process;
        this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
        this.classpath = classpath;
    }

    /**
     * The (unmodifiable) standard output/error lines.
     * If you want to add lines, use {@link #addLine}
     *
     * @see #addLine
     */
    public synchronized List<String> getLines(Predicate<String> lineFilter) {
        if (lineFilter == null)
            return Collections.unmodifiableList(Arrays.asList(lines.toArray(new String[0])));

        ArrayList<String> res = new ArrayList<>();
        for (String line : this.lines) {
            if (lineFilter.test(line))
                res.add(line);
        }
        return Collections.unmodifiableList(res);
    }

    public synchronized void addLine(String line) {
        lines.add(line);
    }

    /**
     * Add related thread.
     * <p>
     * If a thread is monitoring this raw process,
     * you are required to add the instance by this method.
     */
    public synchronized void addRelatedThread(Thread thread) {
        relatedThreads.add(thread);
    }

//    public synchronized void pumpInputStream(Consumer<String> onLogLine) {
//        addRelatedThread(Lang.thread(new StreamPump(process.getInputStream(), onLogLine, OperatingSystem.NATIVE_CHARSET), "ProcessInputStreamPump", true));
//    }
//
//    public synchronized void pumpErrorStream(Consumer<String> onLogLine) {
//        addRelatedThread(Lang.thread(new StreamPump(process.getErrorStream(), onLogLine, OperatingSystem.NATIVE_CHARSET), "ProcessErrorStreamPump", true));
//    }

    /**
     * True if the managed process is running.
     */
    public boolean isRunning() {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    /**
     * The exit code of raw process.
     */
    public int getExitCode() {
        return process.exitValue();
    }

    /**
     * Destroys the raw process and other related threads that are monitoring this raw process.
     */
    public void stop() {
        process.destroy();
        destroyRelatedThreads();
    }

    public synchronized void destroyRelatedThreads() {
        relatedThreads.forEach(Thread::interrupt);
    }

    @Override
    public String toString() {
        return "ManagedProcess[commands=" + commands + ", isRunning=" + isRunning() + "]";
    }

}
