/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game.addon

import com.google.gson.JsonObject
import org.cubewhy.celestial.Celestial.config
import org.cubewhy.celestial.Celestial.configDir
import org.cubewhy.celestial.game.BaseAddon
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*

class JavaAgent : BaseAddon {
    /**
     * -- GETTER --
     * Get agent arg
     */
    var arg: String? = ""
        private set

    /**
     * -- GETTER --
     * Get a file of the javaagent
     */
    @JvmField
    val file: File

    /**
     * Create an instance
     *
     * @param path Path to JavaAgent
     * @param arg  Arg of the JavaAgent
     */
    constructor(path: String?, arg: String?) {
        this.file = File(path)
        this.arg = arg
    }

    /**
     * Create a instance
     *
     * @param file File of the JavaAgent
     * @param arg  Arg of the JavaAgent
     */
    constructor(file: File, arg: String?) {
        this.file = file
        this.arg = arg
    }

    /**
     * Create a instance
     *
     * @param file File of the JavaAgent
     */
    constructor(file: File) {
        this.file = file
    }

    /**
     * Create a instance
     *
     * @param path Path to JavaAgent
     */
    constructor(path: String?) {
        this.file = File(path)
    }

    val jvmArg: String
        /**
         * Get args which add to the jvm
         *
         * @return args for jvm
         */
        get() {
            var jvmArgs = "-javaagent:\"" + file.absolutePath + "\""
            if (!arg!!.isEmpty()) {
                jvmArgs += if (arg!!.contains(" ")) {
                    "=\"" + this.arg + "\""
                } else {
                    "=" + this.arg
                }
            }
            return jvmArgs
        }

    override fun toString(): String {
        var result = file.name
        if (!arg!!.isBlank()) {
            result += "=" + this.arg
        }
        return result
    }

    override val isEnabled: Boolean
        get() = file.name.endsWith(".jar")

    override fun toggle(): Boolean {
        if (isEnabled) {
            migrate(file.name, file.name + ".disabled")
        } else {
            migrate(file.name, file.name.substring(0, file.name.length - 9))
        }
        return toggle0(file)
    }

    companion object {
        @JvmField
        val javaAgentFolder: File = File(configDir, "javaagents") // share with LunarCN Launcher
        private val log: Logger = LoggerFactory.getLogger(JavaAgent::class.java)

        init {
            if (!javaAgentFolder.exists()) {
                log.info("Making javaagents folder")
                javaAgentFolder.mkdirs() // create the javaagents folder
            }
        }

        /**
         * Find all mods in the weave mods folder
         */
        @Contract(pure = true)
        fun findEnabled(): MutableList<JavaAgent> {
            val list: MutableList<JavaAgent> = ArrayList()
            if (javaAgentFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(javaAgentFolder.listFiles())) {
                    if (file.name.endsWith(".jar") && file.isFile) {
                        list.add(JavaAgent(file, findAgentArg(file.name)))
                    }
                }
            }
            return list
        }

        fun findDisabled(): List<JavaAgent> {
            val list: MutableList<JavaAgent> = ArrayList()
            if (javaAgentFolder.isDirectory) {
                for (file in Objects.requireNonNull<Array<File>>(javaAgentFolder.listFiles())) {
                    if (file.name.endsWith(".jar.disabled") && file.isFile) {
                        list.add(JavaAgent(file, findAgentArg(file.name)))
                    }
                }
            }
            return list
        }

        @JvmStatic
        fun findAll(): List<JavaAgent> {
            val list = findEnabled()
            list.addAll(findDisabled())
            return Collections.unmodifiableList(list)
        }

        /**
         * Set param for a Javaagent
         *
         * @param agent the agent
         * @param arg   param of the agent
         */
        @JvmStatic
        fun setArgFor(agent: JavaAgent, arg: String?) {
            setArgFor(agent.file.name, arg)
        }

        /**
         * Set param for a Javaagent
         *
         * @param name name of the agent
         * @param arg  param of the agent
         */
        fun setArgFor(name: String?, arg: String?) {
            val ja: JsonObject = config.getValue("javaagents").asJsonObject
            ja.addProperty(name, arg)
            config.setValue("javaagents", ja) // dump
        }

        fun findAgentArg(name: String?): String {
            val ja: JsonObject = config.getValue("javaagents").asJsonObject
            if (!ja.has(name)) {
                // create config for the agent
                ja.addProperty(name, "") // leave empty
                config.setValue("javaagents", ja) // dump
            }
            return ja[name].asString
        }

        @JvmStatic
        
        fun add(file: File, arg: String?): JavaAgent? {
            val target = autoCopy(file, javaAgentFolder)
            if (arg != null) {
                setArgFor(file.name, arg)
            }
            return if ((target == null)) null else JavaAgent(target, arg)
        }

        /**
         * Migrate the arg of an agent
         *
         * @param old name of the old agent
         * @param n3w name of the new agent
         */
        @JvmStatic
        fun migrate(old: String?, n3w: String?) {
            val ja: JsonObject = config.getValue("javaagents").asJsonObject
            val arg = if (ja[old] == null && ja[old].isJsonNull) {
                null
            } else {
                ja[old].asString
            }
            ja.addProperty(n3w, arg) // leave empty
            ja.remove(old)
            config.setValue("javaagents", ja) // dump
        }
    }
}
