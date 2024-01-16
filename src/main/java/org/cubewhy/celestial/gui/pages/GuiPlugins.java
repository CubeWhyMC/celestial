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

package org.cubewhy.celestial.gui.pages;

import lombok.SneakyThrows;
import org.cubewhy.celestial.files.DownloadManager;
import org.cubewhy.celestial.files.Downloadable;
import org.cubewhy.celestial.game.RemoteAddon;
import org.cubewhy.celestial.game.addon.JavaAgent;
import org.cubewhy.celestial.game.addon.LunarCNMod;
import org.cubewhy.celestial.game.addon.WeaveMod;
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;

import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.Celestial.launcherData;

public class GuiPlugins extends JPanel {
    private final JTabbedPane tab;

    public GuiPlugins() {
        this.setBorder(new TitledBorder(null, f.getString("gui.plugin.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.tab = new JTabbedPane();

        this.initGui();
    }

    private void initGui() {
        this.add(tab);
        addTabs();

        // refresh addons
        JButton btnRefresh = new JButton(f.getString("gui.plugin.refresh"));
        btnRefresh.addActionListener((e) -> {
            tab.removeAll();
            addTabs();
        });
        this.add(tab);
        this.add(btnRefresh);
    }

    @SneakyThrows
    private void addTabs() {
        List<RemoteAddon> addons = launcherData.getPlugins();
        if (addons == null) {
            this.add(new JLabel(f.getString("gui.plugin.unsupported")));
            return;
        }
        JPanel panelWeave = new JPanel();
        panelWeave.setLayout(new VerticalFlowLayout());
        JPanel panelAgents = new JPanel();
        panelAgents.setLayout(new VerticalFlowLayout());
        JPanel panelCN = new JPanel();
        panelCN.setLayout(new VerticalFlowLayout());
        JScrollPane sp1 = new JScrollPane(panelWeave, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp1.getVerticalScrollBar().setUnitIncrement(30);
        JScrollPane sp2 = new JScrollPane(panelAgents, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp2.getVerticalScrollBar().setUnitIncrement(30);
        JScrollPane sp3 = new JScrollPane(panelCN, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp3.getVerticalScrollBar().setUnitIncrement(30);
        this.tab.addTab("Weave", sp1);
        this.tab.addTab("Agents", sp2);
        this.tab.addTab("LunarCN", sp3);
        for (RemoteAddon addon : addons) {
            if (addon.getCategory() == RemoteAddon.Category.WEAVE) {
                addPlugin(panelWeave, addon, WeaveMod.modFolder);
            } else if (addon.getCategory() == RemoteAddon.Category.AGENT) {
                addPlugin(panelAgents, addon, JavaAgent.javaAgentFolder);
            } else if (addon.getCategory() == RemoteAddon.Category.CN) {
                addPlugin(panelCN, addon, LunarCNMod.modFolder);
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
    private void addPlugin(JPanel panel, @NotNull RemoteAddon addon, File folder) {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout());
        p.add(new JLabel(addon.getName()));
        File file = new File(folder, addon.getName());
        if (file.exists()) {
            p.add(new JLabel(f.getString("gui.plugin.exist")));
        } else {
            p.add(getDownloadButton(addon.getDownloadURL(), file));
        }
        panel.add(p);
    }

    private @NotNull JButton getDownloadButton(URL url, @NotNull File file) {
        JButton btn = new JButton(String.format(f.getString("gui.plugins.download"), file.getName()));
        btn.addActionListener((e) -> {
            DownloadManager.download(new Downloadable(url, file, null));
            new Thread(() -> {
                try {
                    DownloadManager.waitForAll();
                } catch (InterruptedException err) {
                    throw new RuntimeException(err);
                }
            }).start();
        });
        return btn;
    }
}
