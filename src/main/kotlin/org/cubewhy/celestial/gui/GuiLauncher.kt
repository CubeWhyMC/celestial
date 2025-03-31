/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui

import com.sun.tools.attach.AttachNotSupportedException
import org.cubewhy.celestial.*
import org.cubewhy.celestial.event.EventManager
import org.cubewhy.celestial.event.EventTarget
import org.cubewhy.celestial.event.impl.AuthEvent
import org.cubewhy.celestial.event.impl.GameStartEvent
import org.cubewhy.celestial.event.impl.GameTerminateEvent
import org.cubewhy.celestial.gui.dialogs.HelpDialog
import org.cubewhy.celestial.gui.elements.StatusBar
import org.cubewhy.celestial.utils.findJava
import org.cubewhy.celestial.utils.lunar.LauncherData.Companion.getMainClass
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.net.URI
import javax.swing.*

class GuiLauncher : JFrame() {
    lateinit var layoutX: CardLayout
    lateinit var mainPanel: JPanel

    init {
        EventManager.register(this)

        this.setBounds(100, 100, 1200, 700)
        this.title = f.getString("gui.launcher.title")

        // init icon
        this.resetIcon()

        this.initGui()
    }

    /**
     * Init Celestial Launcher (gui)
     */

    private fun initGui() {
        this.add(statusBar, BorderLayout.SOUTH)
        // menu
        val menu = Panel()
        val btnPrevious = JButton(f.getString("gui.previous"))
        val btnNext = JButton(f.getString("gui.next"))
        // For developers: It is not recommended to remove the Donate button in Celestial Launcher's derivative versions
        // 不建议在衍生版本中删除赞助按钮
        // Celestial 是免费开源的启动器, 请赞助来帮助我们走得更远 (收入会全部用于开发)
        // Celestial is an opensource launcher, please donate to let us go further (All money will be used for development)
        val btnDonate = JButton(f.getString("gui.donate"))
        val btnHelp = JButton(f.getString("gui.help"))
        val btnDiscord = JButton(f.getString("gui.discord"))
        btnDonate.addActionListener {
            try {
                URI.create("https://lunarclient.top/donate").open()
            } catch (ignored: IOException) {
            }
        }
        btnHelp.addActionListener {
            HelpDialog().isVisible = true
        }
        btnDiscord.addActionListener {
            try {
                Desktop.getDesktop().browse(URI("https://discord.gg/4EXXDUWHcR"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        menu.add(btnPrevious)
        menu.add(btnNext)
        menu.add(btnDonate)
        menu.add(btnHelp)
        menu.add(btnDiscord)

        menu.setSize(100, 20)

        this.add(menu, BorderLayout.NORTH)
        // main panel
        mainPanel = JPanel()
        layoutX = CardLayout()
        mainPanel.layout = layoutX

        // add pages
        config.pages.forEach { page ->
            mainPanel.add(page.pageName, page.clazz.getConstructor().newInstance())
        }

        // bind buttons
        btnPrevious.addActionListener { layoutX.previous(mainPanel) }
        btnNext.addActionListener { layoutX.next(mainPanel) }
        this.add(mainPanel) // add MainPanel

        // try to find the exist game process
        Thread {
            try {
                this.findExistGame()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }.start()
    }


    private fun findExistGame() {
        try {
            val java = /*if (config.celeWrap.state) findJava(CeleWrap.MAIN_CLASS) else */findJava(getMainClass(null))
            if (java != null) {
                val pid = java.id()
                log.info("Exist game process found! Pid: $pid")
                gamePid.set(pid.toLong())
                GameStartEvent(gamePid.get()).call()
                JOptionPane.showMessageDialog(
                    this,
                    f.format("gui.launcher.game.exist.message", pid),
                    f.getString("gui.launcher.game.exist.title"),
                    JOptionPane.INFORMATION_MESSAGE
                )
                java.detach()
            }
        } catch (e: AttachNotSupportedException) {
            log.error("Failed to find the game process, is launched with the official launcher? (attach not support)")
            log.error(e.stackTraceToString())
        }
    }


    /**
     * Load icon image from /images/icons
     *
     * @param name file name
     */
    private fun setIconImage(name: String) {
        this.iconImage = ImageIcon(
            "/images/icons/$name.png".getInputStream()!!.readAllBytes()
        ).image
    }

    /**
     * Reset the icon
     */

    private fun resetIcon() {
        when (config.theme) {
            "light" -> this.setIconImage("icon-dark")
            "dark" -> this.setIconImage("icon-light")
            else -> this.setIconImage("icon-light")
        }
    }

    @EventTarget
    fun onGameStart(e: GameStartEvent) {
        this.setIconImage("running")
    }

    @EventTarget
    fun onGameTerminate(e: GameTerminateEvent) {
        this.resetIcon()
    }

    @EventTarget
    fun onAuth(e: AuthEvent) {
        log.info("Request for login")
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(e.authURL.toString()), null)
        val link = JOptionPane.showInputDialog(
            this,
            f.getString("gui.launcher.auth.message"),
            f.getString("gui.launcher.auth.title"),
            JOptionPane.QUESTION_MESSAGE
        )
        e.put(link)
    }

    companion object {
        val statusBar = StatusBar()
        private val log: Logger = LoggerFactory.getLogger(GuiLauncher::class.java)
    }
}
