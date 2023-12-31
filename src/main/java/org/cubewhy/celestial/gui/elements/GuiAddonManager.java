/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.game.BaseAddon;
import org.cubewhy.celestial.game.addon.JavaAgent;
import org.cubewhy.celestial.game.addon.LunarCNMod;
import org.cubewhy.celestial.game.addon.WeaveMod;
import org.cubewhy.celestial.gui.GuiLauncher;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        DefaultListModel<LunarCNMod> lunarCN = new DefaultListModel<>();
        DefaultListModel<WeaveMod> weave = new DefaultListModel<>();
        DefaultListModel<JavaAgent> agents = new DefaultListModel<>();
        // load items
        loadAgents(agents);
        for (WeaveMod weaveMod : WeaveMod.findAll()) {
            weave.addElement(weaveMod);
        }
        for (LunarCNMod lunarCNMod : LunarCNMod.findAll()) {
            lunarCN.addElement(lunarCNMod);
        }
        JList<LunarCNMod> listLunarCN = new JList<>(lunarCN);
        JList<WeaveMod> listWeave = new JList<>(weave);
        JList<JavaAgent> listAgents = new JList<>(agents);
        // menus
        JPopupMenu agentMenu = new JPopupMenu();
        JMenuItem manageArg = new JMenuItem(f.getString("gui.addon.agents.arg"));
        JMenuItem removeAgent = new JMenuItem(f.getString("gui.addon.agents.remove"));
        agentMenu.add(manageArg);
        agentMenu.addSeparator();
        agentMenu.add(removeAgent);

        manageArg.addActionListener(e -> {
            // open a dialog
            JavaAgent currentAgent = listAgents.getSelectedValue();
            String newArg = JOptionPane.showInputDialog(this, f.getString("gui.addon.agents.arg.message"), currentAgent.getArg());
            if (newArg != null && !currentAgent.getArg().equals(newArg)) {
                JavaAgent.setArgFor(currentAgent, newArg);
                if (newArg.isBlank()) {
                    GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.agents.arg.remove.success"), currentAgent.getFile().getName()));
                } else {
                    GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.agents.arg.set.success"), currentAgent.getFile().getName(), newArg));
                }
                agents.clear();
                loadAgents(agents);
            }
        });

        JPopupMenu weaveMenu = new JPopupMenu();
        JMenuItem removeWeaveMod = new JMenuItem(f.getString("gui.addon.mods.weave.remove"));
        weaveMenu.add(removeWeaveMod);

        JPopupMenu lunarCNMenu = new JPopupMenu();
        JMenuItem removeLunarCNMod = new JMenuItem(f.getString("gui.addon.mods.cn.remove"));
        lunarCNMenu.add(removeLunarCNMod);
        // bind menus
        bingMenu(listLunarCN, lunarCNMenu);
        bingMenu(listWeave, weaveMenu);
        bingMenu(listAgents, agentMenu);


        // buttons
        JButton btnAddLunarCNMod = new JButton(f.getString("gui.addon.mods.add"));
        JButton btnAddWeaveMod = new JButton(f.getString("gui.addon.mods.add"));
        JButton btnAddAgent = new JButton(f.getString("gui.addon.agents.add"));
        // TODO Stretch to the right

        // panels
        final JPanel panelLunarCN = new JPanel();
        panelLunarCN.setLayout(new BoxLayout(panelLunarCN, BoxLayout.Y_AXIS));
        panelLunarCN.add(new JScrollPane(listLunarCN));
        panelLunarCN.add(btnAddLunarCNMod);
        final JPanel panelWeave = new JPanel();
        panelWeave.setLayout(new BoxLayout(panelWeave, BoxLayout.Y_AXIS));
        panelWeave.add(new JScrollPane(listWeave));
        panelWeave.add(btnAddWeaveMod);
        final JPanel panelAgents = new JPanel();
        panelAgents.setLayout(new BoxLayout(panelAgents, BoxLayout.Y_AXIS));
        panelAgents.add(new JScrollPane(listAgents));
        panelAgents.add(Box.createVerticalGlue());
        panelAgents.add(btnAddAgent);
        tab.addTab(f.getString("gui.addons.mods.cn"), panelLunarCN);
        tab.addTab(f.getString("gui.addons.mods.weave"), panelWeave);
        tab.addTab(f.getString("gui.addons.agents"), panelAgents);

        this.add(tab);
    }

    private static void loadAgents(DefaultListModel<JavaAgent> agents) {
        for (JavaAgent javaAgent : JavaAgent.findAll()) {
            agents.addElement(javaAgent);
        }
    }

    private void bingMenu(@NotNull JList<? extends BaseAddon> list, JPopupMenu menu) {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    list.setSelectedIndex(index);
                    menu.show(list, e.getX(), e.getY());
                }
            }
        });
    }
}
