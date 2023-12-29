/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.game.addon.JavaAgent;
import org.cubewhy.celestial.game.addon.LunarCNMod;
import org.cubewhy.celestial.game.addon.WeaveMod;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static org.cubewhy.celestial.Celestial.f;

@Slf4j
public class GuiAddonManager extends JPanel {
    private final JTabbedPane tab = new JTabbedPane();

    public GuiAddonManager() {
        this.setBorder(new TitledBorder(null, f.getString("gui.addons.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.initGui();
    }

    private void initGui() {
        DefaultListModel<String> lunarCN = new DefaultListModel<>();
        DefaultListModel<String> weave = new DefaultListModel<>();
        DefaultListModel<String> agents = new DefaultListModel<>();
        // load items
        for (JavaAgent javaAgent : JavaAgent.findAll()) {
            agents.addElement(javaAgent.getFile().getName());
        }
        for (WeaveMod weaveMod : WeaveMod.findAll()) {
            weave.addElement(weaveMod.getFile().getName());
        }
        for (LunarCNMod lunarCNMod : LunarCNMod.findAll()) {
            lunarCN.addElement(lunarCNMod.getFile().getName());
        }

        JList<String> list1 = new JList<>(lunarCN);
        JList<String> list2 = new JList<>(weave);
        JList<String> list3 = new JList<>(agents);
        tab.addTab(f.getString("gui.addons.mods.cn"), new JScrollPane(list1));
        tab.addTab(f.getString("gui.addons.mods.weave"), new JScrollPane(list2));
        tab.addTab(f.getString("gui.addons.agents"), new JScrollPane(list3));

        this.add(tab);
    }
}
