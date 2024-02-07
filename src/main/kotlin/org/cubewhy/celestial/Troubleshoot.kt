/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial

import org.cubewhy.celestial.proxy
import org.cubewhy.celestial.utils.GitUtils
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.net.URL
import java.util.*

object Troubleshoot {
    private val log = LoggerFactory.getLogger(Troubleshoot::class.java)

    private val f = ResourceBundle.getBundle("languages/troubleshoot")

    @JvmStatic
    fun main(args: Array<String>) {
        log.info("Starting trouble shooting")
        log.info("Document: https://www.lunarclient.top/help")
        System.setProperty("file.encoding", "UTF-8")
        println(f.format("welcome", GitUtils.buildVersion))
        println(
            """
                ============================
                | 1. Enable/Disable proxy  |
                | 2. Update celestial      |
                | 0. Quit                  |
                ============================
            """.trimIndent()
        )
        while (true) {
            print("\rSelect: ")
            val selection = readln()
            when (selection) {
                "0" -> {
                    break
                }

                "1" -> {
                    proxy.state = if (proxy.state) {
                        !confirm(f.getString("confirm.proxy.disable"))
                    } else {
                        print(f.getString("proxy.address"))
                        val address = readln()
                        if (address != "cancel") {
                            proxy.useProxy(URL(address))
                            true
                        } else {
                            false
                        }
                    }
                }

                "2" -> {
                    println("Please goto https://www.lunarclient.top/download to get a update!")
                    if (confirm("Open in browser?")) {
                        Desktop.getDesktop().browse("https://www.lunarclient.top/download".toURI())
                        println()
                    }
                }

                else -> {
                    print("\rWrong selection!")
                    Thread.sleep(3000)
                }
            }
        }
        proxy.save()
    }

    private fun confirm(prompt: String): Boolean {
        print("$prompt [y,N]: ")
        return readln().lowercase() == "y"
    }
}