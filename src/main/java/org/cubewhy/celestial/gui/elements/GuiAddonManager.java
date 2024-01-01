/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.event.impl.AddonAddEvent;
import org.cubewhy.celestial.game.BaseAddon;
import org.cubewhy.celestial.game.addon.JavaAgent;
import org.cubewhy.celestial.game.addon.LunarCNMod;
import org.cubewhy.celestial.game.addon.WeaveMod;
import org.cubewhy.celestial.gui.GuiLauncher;
import org.cubewhy.celestial.utils.GuiUtils;
import org.cubewhy.celestial.utils.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

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
        loadWeaveMods(weave);
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
        JMenuItem renameAgent = new JMenuItem(f.getString("gui.addon.rename"));
        agentMenu.add(manageArg);
        agentMenu.add(renameAgent);
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

        removeAgent.addActionListener(e -> {
            JavaAgent currentAgent = listAgents.getSelectedValue();
            String name = currentAgent.getFile().getName();
            if (JOptionPane.showConfirmDialog(this, String.format(f.getString("gui.addon.agents.remove.confirm.message"), name), f.getString("gui.addon.agents.remove.confirm.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && currentAgent.getFile().delete()) {
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.agents.remove.success"), name));
                agents.clear();
                loadAgents(agents);
            }
        });

        renameAgent.addActionListener(e -> {
            JavaAgent currentAgent = listAgents.getSelectedValue();
            File file = currentAgent.getFile();
            String name = file.getName();
            String newName = JOptionPane.showInputDialog(this, f.getString("gui.addon.rename.dialog.message"), name.substring(0, name.length() - 4));
            if (newName != null && file.renameTo(new File(file.getParentFile(), newName + ".jar"))) {
                log.info(String.format("Rename agent %s -> %s", name, newName + ".jar"));
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.rename.success"), newName));
                // rename the name in the config
                JavaAgent.migrate(name, newName + ".jar");
                agents.clear();
                loadAgents(agents);
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
            WeaveMod currentMod = listWeave.getSelectedValue();
            File file = currentMod.getFile();
            String name = file.getName();
            String newName = JOptionPane.showInputDialog(this, f.getString("gui.addon.rename.dialog.message"), name.substring(0, name.length() - 4));
            if (newName != null && file.renameTo(new File(file.getParentFile(), newName + ".jar"))) {
                log.info(String.format("Rename weave mod %s -> %s", name, newName + ".jar"));
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.rename.success"), newName));
                weave.clear();
                loadWeaveMods(weave);
            }
        });

        removeWeaveMod.addActionListener(e -> {
            WeaveMod currentMod = listWeave.getSelectedValue();
            String name = currentMod.getFile().getName();
            if (JOptionPane.showConfirmDialog(this, String.format(f.getString("gui.addon.mods.weave.remove.confirm.message"), name), f.getString("gui.addon.mods.weave.remove.confirm.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && currentMod.getFile().delete()) {
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.mods.weave.remove.success"), name));
                weave.clear();
                loadWeaveMods(weave);
            }
        });

        JPopupMenu lunarCNMenu = new JPopupMenu();
        JMenuItem renameLunarCNMod = new JMenuItem(f.getString("gui.addon.rename"));
        JMenuItem removeLunarCNMod = new JMenuItem(f.getString("gui.addon.mods.cn.remove"));
        lunarCNMenu.add(renameLunarCNMod);
        lunarCNMenu.addSeparator();
        lunarCNMenu.add(removeLunarCNMod);

        renameLunarCNMod.addActionListener(e -> {
            LunarCNMod currentMod = listLunarCN.getSelectedValue();
            File file = currentMod.getFile();
            String name = file.getName();
            String newName = JOptionPane.showInputDialog(this, f.getString("gui.addon.rename.dialog.message"), name.substring(0, name.length() - 4));
            if (newName != null && file.renameTo(new File(file.getParentFile(), newName + ".jar"))) {
                log.info(String.format("Rename LunarCN mod %s -> %s", name, newName + ".jar"));
                GuiLauncher.statusBar.setText(String.format(f.getString("gui.addon.rename.success"), newName));
            }
        });

        // bind menus
        bindMenu(listLunarCN, lunarCNMenu);
        bindMenu(listWeave, weaveMenu);
        bindMenu(listAgents, agentMenu);


        // buttons
        JButton btnAddLunarCNMod = new JButton(f.getString("gui.addon.mods.add"));
        JButton btnAddWeaveMod = new JButton(f.getString("gui.addon.mods.add"));
        JButton btnAddAgent = new JButton(f.getString("gui.addon.agents.add"));
        // TODO Stretch to the right

        btnAddAgent.addActionListener(e -> {
            File file = GuiUtils.chooseFile(new SuffixFileFilter(".jar"));
            if (file == null) {
                return;
            }
            String arg = JOptionPane.showInputDialog(this, f.getString("gui.addon.agents.add.arg"));
            try {
                JavaAgent agent = JavaAgent.add(file, arg);
                if (agent != null) {
                    // success
                    new AddonAddEvent(AddonAddEvent.Type.JAVAAGENT, agent);
                    GuiLauncher.statusBar.setText(f.getString("gui.addon.agents.add.success"));
                    agents.clear();
                    loadAgents(agents);
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
            File file = GuiUtils.chooseFile(new SuffixFileFilter(".jar"));
            if (file == null) {
                return;
            }
            try {
                WeaveMod mod = WeaveMod.add(file);
                if (mod != null) {
                    // success
                    new AddonAddEvent(AddonAddEvent.Type.WEAVE, mod);
                    GuiLauncher.statusBar.setText(f.getString("gui.addon.mods.weave.add.success"));
                    weave.clear();
                    loadWeaveMods(weave);
                } else {
                    JOptionPane.showMessageDialog(this, f.getString("gui.addon.mods.weave.add.failure.exists"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
                JOptionPane.showMessageDialog(this, String.format(f.getString("gui.addon.mods.weave.add.failure.io"), trace), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // panels
        final JPanel panelLunarCN = new JPanel();
        panelLunarCN.setLayout(new BoxLayout(panelLunarCN, BoxLayout.Y_AXIS));
        panelLunarCN.add(new JScrollPane(listLunarCN));
        final JPanel btnPanel1 = new JPanel();
        btnPanel1.setLayout(new BoxLayout(btnPanel1, BoxLayout.X_AXIS));
        btnPanel1.add(btnAddLunarCNMod);
        btnPanel1.add(createButtonOpenFolder(f.getString("gui.addon.folder"), LunarCNMod.modFolder));
        panelLunarCN.add(btnPanel1);

        final JPanel panelWeave = new JPanel();
        panelWeave.setLayout(new BoxLayout(panelWeave, BoxLayout.Y_AXIS));
        panelWeave.add(new JScrollPane(listWeave));
        final JPanel btnPanel2 = new JPanel();
        btnPanel2.setLayout(new BoxLayout(btnPanel2, BoxLayout.X_AXIS));
        btnPanel2.add(btnAddWeaveMod);
        btnPanel2.add(createButtonOpenFolder(f.getString("gui.addon.folder"), WeaveMod.modFolder));
        panelWeave.add(btnPanel2);

        final JPanel panelAgents = new JPanel();
        panelAgents.setLayout(new BoxLayout(panelAgents, BoxLayout.Y_AXIS));
        panelAgents.add(new JScrollPane(listAgents));
        final JPanel btnPanel3 = new JPanel();
        btnPanel3.setLayout(new BoxLayout(btnPanel3, BoxLayout.X_AXIS));
        btnPanel3.add(btnAddAgent);
        btnPanel3.add(createButtonOpenFolder(f.getString("gui.addon.folder"), JavaAgent.javaAgentFolder));
        panelAgents.add(btnPanel3);

        tab.addTab(f.getString("gui.addons.agents"), panelAgents);
        tab.addTab(f.getString("gui.addons.mods.cn"), panelLunarCN);
        tab.addTab(f.getString("gui.addons.mods.weave"), panelWeave);

        this.add(tab);
    }

    private JButton createButtonOpenFolder(String text, File folder) {
        JButton btn = new JButton(text);
        btn.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(folder);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return btn;
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
