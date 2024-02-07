/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui.pages

import org.cubewhy.celestial.f
import org.cubewhy.celestial.launcherData
import org.cubewhy.celestial.format
import org.cubewhy.celestial.game.RemoteAddon
import org.cubewhy.celestial.game.addon.JavaAgent
import org.cubewhy.celestial.game.addon.LunarCNMod
import org.cubewhy.celestial.game.addon.WeaveMod
import org.cubewhy.celestial.gui.dialogs.AddonInfoDialog
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.withScroller
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.GridLayout
import java.io.File
import javax.swing.*
import javax.swing.border.TitledBorder

class GuiPlugins : JPanel() {
    private val tab: JTabbedPane

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GuiPlugins::class.java)
    }

    init {
        this.border = TitledBorder(
            null,
            f.getString("gui.plugins.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)

        this.tab = JTabbedPane()

        this.initGui()
    }

    private fun initGui() {
        this.add(tab)
        addTabs()

        // refresh addons
        val btnRefresh = JButton(f.getString("gui.plugins.refresh"))
        btnRefresh.addActionListener {
            this.refresh()
        }
        this.add(tab)
        this.add(btnRefresh)
    }

    private fun refresh() {
        this.tab.removeAll()
        this.addTabs()
    }

    private fun addTabs() {
        val addons: List<RemoteAddon>? = launcherData.plugins
        if (addons == null) {
            this.add(JLabel(f.getString("gui.plugins.unsupported")))
            return
        }
        val panelWeave = JPanel()
        panelWeave.layout = VerticalFlowLayout()
        val panelAgents = JPanel()
        panelAgents.layout = VerticalFlowLayout()
        val panelCN = JPanel()
        panelCN.layout = VerticalFlowLayout()
        tab.addTab("Weave", panelWeave.withScroller())
        tab.addTab("Agents", panelAgents.withScroller())
        tab.addTab("LunarCN", panelCN.withScroller())
        for (addon in addons) {
            when (addon.category) {
                RemoteAddon.Category.WEAVE -> {
                    addPlugin(panelWeave, addon, WeaveMod.modFolder)
                }

                RemoteAddon.Category.AGENT -> {
                    addPlugin(panelAgents, addon, JavaAgent.javaAgentFolder)
                }

                RemoteAddon.Category.CN -> {
                    addPlugin(panelCN, addon, LunarCNMod.modFolder)
                }
            }
        }
    }

    /**
     * Add label and download button
     *
     * @param panel  the panel
     * @param addon  the addon
     * @param folder target folder
     */
    private fun addPlugin(panel: JPanel, addon: RemoteAddon, folder: File) {
        val p = JPanel()
        p.layout = GridLayout()
        p.add(JLabel(addon.name))
        val file = File(folder, addon.name)
        p.add(getInfoButton(addon, file))
        panel.add(p)
    }

    private fun getInfoButton(addon: RemoteAddon, file: File): JButton {
        val btn = JButton(f.format("gui.plugins.info", file.name))
        btn.addActionListener {
            log.info("Open plugin info dialog for " + addon.name)
            AddonInfoDialog(addon, file).isVisible = true // show info dialog
        }
        return btn
    }
}
