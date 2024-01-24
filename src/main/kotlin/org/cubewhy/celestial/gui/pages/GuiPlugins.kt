/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui.pages

import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.Celestial.launcherData
import org.cubewhy.celestial.files.DownloadManager.download
import org.cubewhy.celestial.files.DownloadManager.waitForAll
import org.cubewhy.celestial.files.Downloadable
import org.cubewhy.celestial.game.RemoteAddon
import org.cubewhy.celestial.game.addon.JavaAgent
import org.cubewhy.celestial.game.addon.LunarCNMod
import org.cubewhy.celestial.game.addon.WeaveMod
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.io.File
import java.net.URL
import javax.swing.*
import javax.swing.border.TitledBorder

class GuiPlugins : JPanel() {
    private val tab: JTabbedPane

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
            tab.removeAll()
            addTabs()
        }
        this.add(tab)
        this.add(btnRefresh)
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
        val sp1 = JScrollPane(
            panelWeave,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        sp1.verticalScrollBar.unitIncrement = 30
        val sp2 = JScrollPane(
            panelAgents,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        sp2.verticalScrollBar.unitIncrement = 30
        val sp3 = JScrollPane(
            panelCN,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
        sp3.verticalScrollBar.unitIncrement = 30
        tab.addTab("Weave", sp1)
        tab.addTab("Agents", sp2)
        tab.addTab("LunarCN", sp3)
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
        if (file.exists()) {
            p.add(JLabel(f.getString("gui.plugins.exist")))
        } else {
            p.add(getDownloadButton(addon.downloadURL, file))
        }
        panel.add(p)
    }

    private fun getDownloadButton(url: URL, file: File): JButton {
        val btn = JButton(String.format(f.getString("gui.plugins.download"), file.name))
        btn.addActionListener {
            download(Downloadable(url, file, null))
            Thread {
                try {
                    waitForAll()
                } catch (err: InterruptedException) {
                    throw RuntimeException(err)
                }
            }.start()
        }
        return btn
    }
}
