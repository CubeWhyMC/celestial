/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.event.EventTarget;
import org.cubewhy.celestial.event.impl.AddonAddEvent;
import org.cubewhy.celestial.event.impl.CreateLauncherEvent;
import org.cubewhy.celestial.game.BaseAddon;
import org.cubewhy.celestial.game.addon.FabricMod;
import org.cubewhy.celestial.game.addon.JavaAgent;
import org.cubewhy.celestial.game.addon.LunarCNMod;
import org.cubewhy.celestial.game.addon.WeaveMod;
import org.cubewhy.celestial.gui.GuiLauncher;
import org.cubewhy.celestial.utils.AddonUtils;
import org.cubewhy.celestial.utils.GuiUtils;
import org.cubewhy.celestial.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.utils.GuiUtils.createButtonOpenFolder;

@Slf4j
public class GuiAddonManager extends JPanel {
    private final JTabbedPane tab = new JTabbedPane();
    private final DefaultListModel<LunarCNMod> lunarcnList = new DefaultListModel<>();
    private final DefaultListModel<WeaveMod> weaveList = new DefaultListModel<>();
    private final DefaultListModel<JavaAgent> agentList = new DefaultListModel<>();
    private final DefaultListModel<FabricMod> fabricList = new DefaultListModel<>();

    public GuiAddonManager() {
        this.setBorder(new TitledBorder(null, f.getString("gui.addons.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.initGui();
    }

    private void initGui() {
        // load items
        loadAgents(agentList);
        loadWeaveMods(weaveList);
        loadLunarCNMods(lunarcnList);
        loadFabricMods(fabricList);

        JList<LunarCNMod> jListLunarCN = new JList<>(lunarcnList);
        JList<WeaveMod> jListWeave = new JList<>(weaveList);
        JList<JavaAgent> jListAgents = new JList<>(agentList);
        JList<FabricMod> jListFabric = new JList<>(fabricList);
        // menus
        JPopupMenu agentMenu = new JPopupMenu();
        JMenuItem manageArg = new JMenuItem(f.getString("gui.addon.agents.arg"));
        JMenuItem removeAgent = new JMenuItem(f.getString("gui.addon.agents.remove"));
        JMenuItem renameAgent = new JMenuItem(f.getString("gui.addon.rename"));
        agentMenu.add(manageArg);
        agentMenu.add(renameAgent);
        agentMenu.addSeparator();
        agentMenu.add(removeAgent);

        manageArg.addActionListener(e -> {
            // open a dialog
            JavaAgent currentAgent = jListAgents.getSelectedValue();
            String newArg = JOptionPane.showInputDialog(this, f.getString("gui.addon.agents.arg.message"), currentAgent.getArg());
            if (newArg != null && !currentAgent.getArg().equals(newArg)) {
                JavaAgent.setArgFor(currentAgent, newArg);
                if (newArg.isBlank()) {
                    GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.agents.arg.remove.success"), currentAgent.getFile().getName()));
                } else {
                    GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.agents.arg.set.success"), currentAgent.getFile().getName(), newArg));
                }
                agentList.clear();
                loadAgents(agentList);
            }
        });

        removeAgent.addActionListener(e -> {
            JavaAgent currentAgent = jListAgents.getSelectedValue();
            String name = currentAgent.getFile().getName();
            if (JOptionPane.showConfirmDialog(this, String.format(f.getString("gui.addon.agents.remove.confirm.message"), name), f.getString("gui.addon.agents.remove.confirm.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && currentAgent.getFile().delete()) {
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.agents.remove.success"), name));
                agentList.clear();
                loadAgents(agentList);
            }
        });

        renameAgent.addActionListener(e -> {
            JavaAgent currentAgent = jListAgents.getSelectedValue();
            File file = currentAgent.getFile();
            String name = file.getName();
            String newName = JOptionPane.showInputDialog(this, f.getString("gui.addon.rename.dialog.message"), name.substring(0, name.length() - 4));
            if (newName != null && file.renameTo(new File(file.getParentFile(), newName + ".jar"))) {
                log.info(String.format("Rename agent %s -> %s", name, newName + ".jar"));
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.rename.success"), newName));
                // rename the name in the config
                JavaAgent.migrate(name, newName + ".jar");
                agentList.clear();
                loadAgents(agentList);
            }
        });

        // weave menu
        JPopupMenu weaveMenu = new JPopupMenu();
        JMenuItem renameWeaveMod = new JMenuItem(f.getString("gui.addon.rename"));
        JMenuItem removeWeaveMod = new JMenuItem(f.getString("gui.addon.mods.weave.remove"));
        weaveMenu.add(renameWeaveMod);
        weaveMenu.addSeparator();
        weaveMenu.add(removeWeaveMod);

        renameWeaveMod.addActionListener(e -> {
            WeaveMod currentMod = jListWeave.getSelectedValue();
            File file = currentMod.getFile();
            String name = file.getName();
            String newName = JOptionPane.showInputDialog(this, f.getString("gui.addon.rename.dialog.message"), name.substring(0, name.length() - 4));
            if (newName != null && file.renameTo(new File(file.getParentFile(), newName + ".jar"))) {
                log.info(String.format("Rename weave mod %s -> %s", name, newName + ".jar"));
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.rename.success"), newName));
                weaveList.clear();
                loadWeaveMods(weaveList);
            }
        });

        removeWeaveMod.addActionListener(e -> {
            WeaveMod currentMod = jListWeave.getSelectedValue();
            String name = currentMod.getFile().getName();
            if (JOptionPane.showConfirmDialog(this, String.format(f.getString("gui.addon.mods.weave.remove.confirm.message"), name), f.getString("gui.addon.mods.weave.remove.confirm.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && currentMod.getFile().delete()) {
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.mods.weave.remove.success"), name));
                weaveList.clear();
                loadWeaveMods(weaveList);
            }
        });

        JPopupMenu lunarCNMenu = new JPopupMenu();
        JMenuItem renameLunarCNMod = new JMenuItem(f.getString("gui.addon.rename"));
        JMenuItem removeLunarCNMod = new JMenuItem(f.getString("gui.addon.mods.cn.remove"));
        lunarCNMenu.add(renameLunarCNMod);
        lunarCNMenu.addSeparator();
        lunarCNMenu.add(removeLunarCNMod);

        renameLunarCNMod.addActionListener(e -> {
            LunarCNMod currentMod = jListLunarCN.getSelectedValue();
            File file = currentMod.getFile();
            String name = file.getName();
            String newName = JOptionPane.showInputDialog(this, f.getString("gui.addon.rename.dialog.message"), name.substring(0, name.length() - 4));
            if (newName != null && file.renameTo(new File(file.getParentFile(), newName + ".jar"))) {
                log.info(String.format("Rename LunarCN mod %s -> %s", name, newName + ".jar"));
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.rename.success"), newName));
                lunarcnList.clear();
                loadLunarCNMods(lunarcnList);
            }
        });

        removeLunarCNMod.addActionListener((e) -> {
            LunarCNMod currentMod = jListLunarCN.getSelectedValue();
            String name = currentMod.getFile().getName();
            if (JOptionPane.showConfirmDialog(this, String.format(f.getString("gui.addon.mods.cn.remove.confirm.message"), name), f.getString("gui.addon.mods.cn.remove.confirm.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && currentMod.getFile().delete()) {
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.mods.cn.remove.success"), name));
                lunarcnList.clear();
                loadLunarCNMods(lunarcnList);
            }
        });

        JPopupMenu fabricMenu = new JPopupMenu();
        JMenuItem renameFabricMod = new JMenuItem(f.getString("gui.addon.rename"));
        JMenuItem removeFabricMod = new JMenuItem(f.getString("gui.addon.mods.fabric.remove"));

        renameFabricMod.addActionListener(e -> {
            FabricMod currentMod = jListFabric.getSelectedValue();
            File file = currentMod.getFile();
            String name = file.getName();
            String newName = JOptionPane.showInputDialog(this, f.getString("gui.addon.rename.dialog.message"), name.substring(0, name.length() - 4));
            if (newName != null && file.renameTo(new File(file.getParentFile(), newName + ".jar"))) {
                log.info(String.format("Rename Fabric mod %s -> %s", name, newName + ".jar"));
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.rename.success"), newName));
                fabricList.clear();
                loadFabricMods(fabricList);
            }
        });

        removeFabricMod.addActionListener((e) -> {
            FabricMod currentMod = jListFabric.getSelectedValue();
            String name = currentMod.getFile().getName();
            if (JOptionPane.showConfirmDialog(this, String.format(f.getString("gui.addon.mods.fabric.remove.confirm.message"), name), f.getString("gui.addon.mods.fabric.remove.confirm.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && currentMod.getFile().delete()) {
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.mods.fabric.remove.success"), name));
                fabricList.clear();
                loadFabricMods(fabricList);
            }
        });

        fabricMenu.add(renameFabricMod);
        fabricMenu.addSeparator();
        fabricMenu.add(removeFabricMod);

        // bind menus
        bindMenu(jListLunarCN, lunarCNMenu);
        bindMenu(jListWeave, weaveMenu);
        bindMenu(jListFabric, fabricMenu);
        bindMenu(jListAgents, agentMenu);


        // buttons
        JButton btnAddLunarCNMod = new JButton(f.getString("gui.addon.mods.add"));
        JButton btnAddWeaveMod = new JButton(f.getString("gui.addon.mods.add"));
        JButton btnAddFabric = new JButton(f.getString("gui.addon.mods.add"));
        JButton btnAddAgent = new JButton(f.getString("gui.addon.agents.add"));

        btnAddAgent.addActionListener(e -> {
            File file = GuiUtils.chooseFile(new FileNameExtensionFilter("Agent", "jar"));
            if (file == null) {
                log.info("Cancel add agent because file == null");
                return;
            }
            String arg = JOptionPane.showInputDialog(this, f.getString("gui.addon.agents.add.arg"));
            try {
                JavaAgent agent = JavaAgent.add(file, arg);
                if (agent != null) {
                    // success
                    new AddonAddEvent(AddonAddEvent.Type.JAVAAGENT, agent);
                    GuiLauncher.statusBar.setText(f.getString("gui.addon.agents.add.success"));
                    agentList.clear();
                    loadAgents(agentList);
                } else {
                    JOptionPane.showMessageDialog(this, f.getString("gui.addon.agents.add.failure.exists"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
                JOptionPane.showMessageDialog(this, String.format(f.getString("gui.addon.agents.add.failure.io"), trace), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAddWeaveMod.addActionListener(e -> {
            File file = GuiUtils.chooseFile(new FileNameExtensionFilter("Weave Mod", "jar"));
            if (file == null) {
                return;
            }
            try {
                if (!AddonUtils.isWeaveMod(file)) {
                    JOptionPane.showMessageDialog(this, String.format(f.getString("gui.addon.mods.incorrect"), file), "Warning | Type incorrect", JOptionPane.WARNING_MESSAGE);
                }
                WeaveMod mod = WeaveMod.add(file);
                if (mod != null) {
                    // success
                    new AddonAddEvent(AddonAddEvent.Type.WEAVE, mod);
                    GuiLauncher.statusBar.setText(f.getString("gui.addon.mods.weave.add.success"));
                    weaveList.clear();
                    loadWeaveMods(weaveList);
                } else {
                    JOptionPane.showMessageDialog(this, f.getString("gui.addon.mods.weave.add.failure.exists"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
                JOptionPane.showMessageDialog(this, String.format(f.getString("gui.addon.mods.weave.add.failure.io"), trace), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAddLunarCNMod.addActionListener(e -> {
            File file = GuiUtils.chooseFile(new FileNameExtensionFilter("LunarCN Mod", "jar"));
            if (file == null) {
                return;
            }
            try {
                if (!AddonUtils.isLunarCNMod(file)) {
                    JOptionPane.showMessageDialog(this, String.format(f.getString("gui.addon.mods.incorrect"), file), "Warning | Type incorrect", JOptionPane.WARNING_MESSAGE);
                }
                LunarCNMod mod = LunarCNMod.add(file);
                if (mod != null) {
                    // success
                    new AddonAddEvent(AddonAddEvent.Type.LUNARCN, mod);
                    GuiLauncher.statusBar.setText(f.getString("gui.addon.mods.cn.add.success"));
                    weaveList.clear();
                    loadWeaveMods(weaveList);
                } else {
                    JOptionPane.showMessageDialog(this, f.getString("gui.addon.mods.cn.add.failure.exists"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
                JOptionPane.showMessageDialog(this, String.format(f.getString("gui.addon.mods.cn.add.failure.io"), trace), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAddFabric.addActionListener((e) -> {
            File file = GuiUtils.chooseFile(new FileNameExtensionFilter("Fabric Mod", "jar"));
            if (file == null) {
                log.info("Cancel add fabric mod because file == null");
                return;
            }
            try {
                FabricMod mod = FabricMod.add(file);
                if (mod != null) {
                    // success
                    new AddonAddEvent(AddonAddEvent.Type.FABRIC, mod);
                    GuiLauncher.statusBar.setText(f.getString("gui.addon.mods.fabric.add.success"));
                    fabricList.clear();
                    loadFabricMods(fabricList);
                } else {
                    JOptionPane.showMessageDialog(this, f.getString("gui.addon.mods.fabric.add.failure.exists"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
                JOptionPane.showMessageDialog(this, String.format(f.getString("gui.addon.mods.fabric.add.failure.io"), trace), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // panels
        final JPanel panelLunarCN = new JPanel();
        panelLunarCN.setName("cn");
        panelLunarCN.setLayout(new BoxLayout(panelLunarCN, BoxLayout.Y_AXIS));
        panelLunarCN.add(new JScrollPane(jListLunarCN));
        final JPanel btnPanel1 = new JPanel();
        btnPanel1.setLayout(new BoxLayout(btnPanel1, BoxLayout.X_AXIS));
        btnPanel1.add(btnAddLunarCNMod);
        btnPanel1.add(createButtonOpenFolder(f.getString("gui.addon.folder"), LunarCNMod.modFolder));
        panelLunarCN.add(btnPanel1);

        final JPanel panelWeave = new JPanel();
        panelWeave.setName("weave");
        panelWeave.setLayout(new BoxLayout(panelWeave, BoxLayout.Y_AXIS));
        panelWeave.add(new JScrollPane(jListWeave));
        final JPanel btnPanel2 = new JPanel();
        btnPanel2.setLayout(new BoxLayout(btnPanel2, BoxLayout.X_AXIS));
        btnPanel2.add(btnAddWeaveMod);
        btnPanel2.add(createButtonOpenFolder(f.getString("gui.addon.folder"), WeaveMod.modFolder));
        panelWeave.add(btnPanel2);

        final JPanel panelAgents = new JPanel();
        panelAgents.setName("agents");
        panelAgents.setLayout(new BoxLayout(panelAgents, BoxLayout.Y_AXIS));
        panelAgents.add(new JScrollPane(jListAgents));
        final JPanel btnPanel3 = new JPanel();
        btnPanel3.setLayout(new BoxLayout(btnPanel3, BoxLayout.X_AXIS));
        btnPanel3.add(btnAddAgent);
        btnPanel3.add(createButtonOpenFolder(f.getString("gui.addon.folder"), JavaAgent.javaAgentFolder));
        panelAgents.add(btnPanel3);

        final JPanel panelFabric = new JPanel();
        panelFabric.setName("fabric");
        panelFabric.setLayout(new BoxLayout(panelFabric, BoxLayout.Y_AXIS));
        panelFabric.add(new JLabel("Not fully support Fabric yet, please add mods by your self"));
//        panelFabric.add(new JScrollPane(jListFabric));
        final JPanel btnPanel4 = new JPanel();
        btnPanel4.setLayout(new BoxLayout(btnPanel4, BoxLayout.X_AXIS));
        btnPanel4.add(btnAddFabric);
//        btnPanel4.add(createButtonOpenFolder(f.getString("gui.addon.folder"), FabricMod.modFolder));
        panelFabric.add(btnPanel4);

        tab.addTab(f.getString("gui.addons.agents"), panelAgents);
        tab.addTab(f.getString("gui.addons.mods.cn"), panelLunarCN);
        tab.addTab(f.getString("gui.addons.mods.weave"), panelWeave);
        tab.addTab(f.getString("gui.addons.mods.fabric"), panelFabric);

        this.add(tab);
        this.tab.addChangeListener(e -> {
            // refresh a mod list
            autoRefresh((JPanel) this.tab.getSelectedComponent());
        });
    }

//    @EventTarget
//    public void onCreateLauncher(@NotNull CreateLauncherEvent event) {
//        // when window get focus, try to reload
//        event.theLauncher.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowActivated(WindowEvent e) {
//                autoRefresh((JPanel) tab.getSelectedComponent());
//            }
//        });
//    }

    private void autoRefresh(@NotNull JPanel panel) {
        String name = panel.getName();
        log.debug(String.format("Refreshing mod list %s (Focus changed)", name));
        switch (name) {
            case "agents" -> {
                agentList.clear();
                loadAgents(agentList);
            }
            case "weave" -> {
                weaveList.clear();
                loadWeaveMods(weaveList);
            }
            case "cn" -> {
                lunarcnList.clear();
                loadLunarCNMods(lunarcnList);
            }
            case "fabric" -> {
                fabricList.clear();
                loadFabricMods(fabricList);
            }
        }
    }

    private void loadFabricMods(DefaultListModel<FabricMod> modList) {
        for (FabricMod mod : FabricMod.findAll()) {
            modList.addElement(mod);
        }
    }

    private void loadLunarCNMods(DefaultListModel<LunarCNMod> modList) {
        for (LunarCNMod lunarCNMod : LunarCNMod.findAll()) {
            modList.addElement(lunarCNMod);
        }
    }

    private static void loadWeaveMods(DefaultListModel<WeaveMod> weave) {
        for (WeaveMod weaveMod : WeaveMod.findAll()) {
            weave.addElement(weaveMod);
        }
    }

    private static void loadAgents(DefaultListModel<JavaAgent> agentList) {
        for (JavaAgent javaAgent : JavaAgent.findAll()) {
            agentList.addElement(javaAgent);
        }
    }

    private void bindMenu(@NotNull JList<? extends BaseAddon> list, JPopupMenu menu) {
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
