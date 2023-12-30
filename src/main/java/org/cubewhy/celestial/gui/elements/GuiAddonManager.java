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
        // buttons
        JButton btnAddLunarCNMod = new JButton("Add mod");
        JButton btnAddWeaveMod = new JButton("Add mod");
        JButton btnAddAgent = new JButton("Add agent");
        // TODO Stretch to the right

        // panels
        final JPanel panelLunarCN = new JPanel();
        panelLunarCN.setLayout(new BoxLayout(panelLunarCN, BoxLayout.Y_AXIS));
        panelLunarCN.add(new JScrollPane(list1));
        panelLunarCN.add(btnAddLunarCNMod);
        final JPanel panelWeave = new JPanel();
        panelWeave.setLayout(new BoxLayout(panelWeave, BoxLayout.Y_AXIS));
        panelWeave.add(new JScrollPane(list2));
        panelWeave.add(btnAddWeaveMod);
        final JPanel panelAgents = new JPanel();
        panelAgents.setLayout(new BoxLayout(panelAgents, BoxLayout.Y_AXIS));
        panelAgents.add(new JScrollPane(list3));
        panelAgents.add(Box.createVerticalGlue());
        panelAgents.add(btnAddAgent);
        tab.addTab(f.getString("gui.addons.mods.cn"), panelLunarCN);
        tab.addTab(f.getString("gui.addons.mods.weave"), panelWeave);
        tab.addTab(f.getString("gui.addons.agents"), panelAgents);

        this.add(tab);
    }
}
