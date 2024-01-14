package org.cubewhy.celestial.gui.pages;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.game.addon.LunarCNMod;
import org.cubewhy.celestial.game.addon.WeaveMod;
import org.cubewhy.celestial.gui.dialogs.ArgsConfigDialog;
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout;
import org.cubewhy.celestial.utils.GuiUtils;
import org.cubewhy.celestial.utils.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

import static org.cubewhy.celestial.Celestial.*;
import static org.cubewhy.celestial.gui.GuiLauncher.statusBar;

@Slf4j
public class GuiSettings extends JScrollPane {
    private static final JPanel panel = new JPanel();
    private final Set<String> claimed = new HashSet<>();

    public GuiSettings() {
        super(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(new TitledBorder(null, f.getString("gui.settings.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));
        this.getVerticalScrollBar().setUnitIncrement(30);
        this.initGui();
    }

    private void initGui() {
        // config
        // jre
        panel.add(new JLabel(f.getString("gui.settings.warn.restart")));
        JPanel panelVM = new JPanel();
        panelVM.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));
        panelVM.setBorder(new TitledBorder(null, f.getString("gui.settings.jvm"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));

        String customJre = config.getValue("jre").getAsString();
        JButton btnSelectPath = new JButton((customJre.isEmpty()) ? SystemUtils.getCurrentJavaExec().getPath() : customJre);
        JButton btnUnset = new JButton(f.getString("gui.settings.jvm.jre.unset"));
        btnSelectPath.addActionListener((e) -> {
            File file = GuiUtils.chooseFile(new FileNameExtensionFilter("Java Executable", "exe"));
            if (file != null) {
                JButton source = (JButton) e.getSource();
                statusBar.setText(String.format(f.getString("gui.settings.jvm.jre.success"), file));
                config.setValue("jre", file.getPath());
                source.setText(file.getPath());
            }
        });
        btnUnset.addActionListener((e) -> {
            if (JOptionPane.showConfirmDialog(this, f.getString("gui.settings.jvm.jre.unset.confirm"), "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            File java = SystemUtils.getCurrentJavaExec();
            btnSelectPath.setText(java.getPath());
            config.setValue("jre", "");
            statusBar.setText(f.getString("gui.settings.jvm.jre.unset.success"));
        });
        // jre settings
        JPanel p1 = new JPanel();
        p1.add(new JLabel(f.getString("gui.settings.jvm.jre")));
        p1.add(btnSelectPath);
        p1.add(btnUnset);
        panelVM.add(p1);
        // ram settings
        JPanel p2 = new JPanel();
        p2.add(new JLabel(f.getString("gui.settings.jvm.ram")));
        JSlider ramSlider = new JSlider(JSlider.HORIZONTAL, 0, SystemUtils.getTotalMem(), config.getValue("ram").getAsInt());
        ramSlider.setPaintTicks(true);
        ramSlider.setMajorTickSpacing(1024); // 1G
        p2.add(ramSlider);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        JLabel labelRam = new JLabel(decimalFormat.format((float) ramSlider.getValue() / 1024F) + "GB");
        ramSlider.addChangeListener((e) -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                // save value
                log.info("Set ram -> " + source.getValue());
                config.setValue("ram", source.getValue());
            }
            labelRam.setText(decimalFormat.format((float) source.getValue() / 1024F) + "GB");
        });
        p2.add(labelRam);
        panelVM.add(p2);

        JPanel p3 = new JPanel();
        p3.add(new JLabel(f.getString("gui.settings.jvm.wrapper")));
        JTextField wrapperInput = getAutoSaveTextField(config.getConfig(), "wrapper");
        p3.add(wrapperInput);
        JButton btnSetVMArgs = new JButton(f.getString("gui.settings.jvm.args"));
        btnSetVMArgs.addActionListener((e) -> {
            new ArgsConfigDialog("vm-args", config.getConfig()).setVisible(true);
        });
        panelVM.add(btnSetVMArgs);
        panelVM.add(p3);

        claim("jre", panelVM);
        claim("ram");
        claim("vm-args");
        claim("wrapper");

        claim("game"); // config in GuiVersionSelect
        claim("javaagents"); // config in GuiAddonManager

        // config of the launcher
        JPanel panelLauncher = new JPanel();
        panelLauncher.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));
        panelLauncher.setBorder(new TitledBorder(null, f.getString("gui.settings.launcher"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        // api
        JPanel p4 = new JPanel();
        p4.add(new JLabel(f.getString("gui.settings.launcher.api")));
        p4.add(getAutoSaveTextField(config.getConfig(), "api"));

        panelLauncher.add(p4);
        // data sharing
        panelLauncher.add(getAutoSaveCheckBox(config.getConfig(), "data-sharing", f.getString("gui.settings.launcher.data-sharing")));
        // theme
        JPanel p5 = new JPanel();
        p5.add(new JLabel(f.getString("gui.settings.launcher.theme")));
        List<String> themes = new ArrayList<>();
        themes.add("dark");
        themes.add("light"); // default themes
        // custom themes
        for (File file : Objects.requireNonNull(themesDir.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                themes.add(file.getName());
            }
        }
        p5.add(getAutoSaveComboBox(config.getConfig(), "theme", themes));
        JButton btnAddTheme = new JButton(f.getString("gui.settings.launcher.theme.add"));
        p5.add(btnAddTheme);
        panelLauncher.add(p5);
        // language
        JPanel p6 = new JPanel();
        p6.add(new JLabel(f.getString("gui.settings.launcher.language")));
        p6.add(getAutoSaveComboBox(config.getConfig(), "language", List.of(new String[]{"zh", "en"})));
        panelLauncher.add(p6);
        // max threads
        JPanel p7 = new JPanel();
        p7.add(new JLabel(f.getString("gui.settings.launcher.max-threads")));
        p7.add(getAutoSaveSpinner(config.getConfig(), "max-threads", 1, 256));
        panelLauncher.add(p7);
        // installation-dir
        JPanel p8 = new JPanel();
        p8.add(new JLabel(f.getString("gui.settings.launcher.installation")));
        JButton btnSelectInstallation = new JButton(config.getValue("installation-dir").getAsString());
        btnSelectInstallation.addActionListener((e) -> {
            File file = GuiUtils.chooseFolder();
            JButton source = (JButton) e.getSource();
            if (file == null) {
                return;
            }
            config.setValue("installation-dir", file.getPath());
            log.info("Set installation-dir to " + file);
            source.setText(file.getPath());
            statusBar.setText(String.format(f.getString("gui.settings.installation.success"), file));
        });
        p8.add(btnSelectInstallation);
        panelLauncher.add(p8);
        // game-dir
        JPanel p9 = new JPanel();
        p9.add(new JLabel(f.getString("gui.settings.launcher.game")));
        JButton btnSelectGameDir = new JButton(config.getValue("game-dir").getAsString());
        btnSelectGameDir.addActionListener((e) -> {
            File file = GuiUtils.chooseFolder();
            JButton source = (JButton) e.getSource();
            if (file == null) {
                return;
            }
            config.setValue("game-dir", file.getPath());
            log.info("Set game-dir to " + file);
            source.setText(file.getPath());
            statusBar.setText(String.format(f.getString("gui.settings.game-dir.success"), file));
        });
        p9.add(btnSelectGameDir);
        panelLauncher.add(p9);

        claim("data-sharing", panelLauncher);
        claim("theme");
        claim("language");
        claim("max-threads");
        claim("api");
        claim("installation-dir");
        claim("game-dir");

        // addon
        JPanel panelAddon = new JPanel();
        panelAddon.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));
        panelAddon.setBorder(new TitledBorder(null, f.getString("gui.settings.addon"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        JPanel p10 = new JPanel();
        ButtonGroup btnGroup = new ButtonGroup();
        JRadioButton btnLoaderUnset = new JRadioButton(f.getString("gui.settings.addon.loader.unset"), isLoaderSelected(null));
        btnGroup.add(btnLoaderUnset);
        JRadioButton btnWeave = new JRadioButton("Weave", isLoaderSelected("weave"));
        btnGroup.add(btnWeave);
        JRadioButton btnLunarCN = new JRadioButton("LunarCN", isLoaderSelected("cn"));
        btnGroup.add(btnLunarCN);
        btnLoaderUnset.addActionListener((e) -> {
            // make weave & cn = false
            toggleLoader(null);
        });
        btnWeave.addActionListener((e) -> {
            toggleLoader("weave");
        });
        btnLunarCN.addActionListener((e) -> {
            toggleLoader("cn");
        });
        p10.add(btnLoaderUnset);
        p10.add(btnWeave);
        p10.add(btnLunarCN);
        panelAddon.add(p10);
        // installation (loader)
        JPanel p11 = new JPanel();
        // lunarcn
        JButton btnSelectLunarCNInstallation = getSelectInstallationButton(LunarCNMod.getInstallation(), "LunarCN Loader", "lunarcn");
        p11.add(new JLabel(f.getString("gui.settings.addon.loader.cn.installation")));
        p11.add(btnSelectLunarCNInstallation);
        panelAddon.add(p11);
        JPanel p12 = new JPanel();
        JButton btnSelectWeaveInstallation = getSelectInstallationButton(WeaveMod.getInstallation(), "Weave Loader", "weave");
        p12.add(new JLabel(f.getString("gui.settings.addon.loader.weave.installation")));
        p12.add(btnSelectWeaveInstallation);
        panelAddon.add(p12);

        JPanel p13 = new JPanel();
        p13.add(getAutoSaveCheckBox(config.getConfig().getAsJsonObject("addon").getAsJsonObject("weave"), "check-update", f.getString("gui.settings.addon.loader.weave.check-update")));
        p13.add(getAutoSaveCheckBox(config.getConfig().getAsJsonObject("addon").getAsJsonObject("lunarcn"), "check-update", f.getString("gui.settings.addon.loader.cn.check-update")));
        panelAddon.add(p13);

        claim("addon", panelAddon);

        // game settings
        JPanel panelGame = new JPanel();
        panelGame.setBorder(new TitledBorder(null, f.getString("gui.settings.game"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        panelGame.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));

        // program args
        JPanel p14 = new JPanel();
        JButton btnProgramArgs = new JButton(f.getString("gui.settings.game.args"));
        btnProgramArgs.addActionListener((e) -> new ArgsConfigDialog("program-args", config.getConfig()).setVisible(true));
        p14.add(btnProgramArgs);
        panelGame.add(p14);
        // resize
        JPanel p15 = new JPanel();
        p15.setBorder(new TitledBorder(null, f.getString("gui.settings.game.resize"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        p15.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));
        JPanel p16 = new JPanel();
        p16.add(new JLabel(f.getString("gui.settings.game.resize.width")));
        p16.add(getAutoSaveTextField(config.getValue("resize").getAsJsonObject(), "width"));
        p15.add(p16);
        JPanel p17 = new JPanel();
        p17.add(new JLabel(f.getString("gui.settings.game.resize.height")));
        p17.add(getAutoSaveTextField(config.getValue("resize").getAsJsonObject(), "height"));
        p15.add(p17);
        panelGame.add(p15);

        claim("program-args", panelGame);
        claim("resize");

        if (config.getConfig().keySet().size() != claimed.size()) {
            JPanel panelUnclaimed = new JPanel();
            panelUnclaimed.setBorder(new TitledBorder(null, f.getString("gui.settings.unclaimed"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
            panelUnclaimed.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));
            addUnclaimed(panelUnclaimed, config.getConfig());
            panel.add(panelUnclaimed);
        }
    }

    @NotNull
    private JButton getSelectInstallationButton(File Installation, String name, String type) {
        JButton btnSelectLunarCNInstallation = new JButton(Installation.getPath());
        btnSelectLunarCNInstallation.addActionListener((e) -> {
            File file = GuiUtils.saveFile(new FileNameExtensionFilter(name, "jar"));
            if (file == null) {
                return;
            }
            JButton source = (JButton) e.getSource();
            source.setText(file.getPath());
            setModLoaderInstallation(type, file);
        });
        return btnSelectLunarCNInstallation;
    }

    private void setModLoaderInstallation(String key, @NotNull File file) {
        config.getValue("addon").getAsJsonObject().getAsJsonObject(key).addProperty("installation", file.getPath());
        config.save();
    }

    /**
     * Toggle loader
     *
     * @param type null, cn, weave
     */
    private void toggleLoader(@Nullable String type) {
        boolean b1 = false; // weave
        boolean b2 = false; // lccn
        if (type != null) {
            if (type.equals("cn")) {
                b2 = true;
            } else if (type.equals("weave")) {
                b1 = true;
            }
        }
        JsonObject addon = config.getValue("addon").getAsJsonObject();
        JsonObject weave = addon.get("weave").getAsJsonObject();
        JsonObject cn = addon.get("lunarcn").getAsJsonObject();
        weave.addProperty("enable", b1);
        cn.addProperty("enable", b2);
        config.save();
    }

    private boolean isLoaderSelected(String type) {
        JsonObject addon = config.getValue("addon").getAsJsonObject();
        JsonObject weave = addon.get("weave").getAsJsonObject();
        JsonObject cn = addon.get("lunarcn").getAsJsonObject();
        boolean stateWeave = weave.get("enable").getAsBoolean();
        boolean stateCN = cn.get("enable").getAsBoolean();
        if (stateWeave && stateCN && type != null) {
            // correct it

            log.warn("Weave cannot load with LunarCN, auto corrected");
            weave.addProperty("enable", false);
            cn.addProperty("enable", false);
            config.save();
            return isLoaderSelected(null);
        }
        if (type == null) {
            return !(stateWeave || stateCN);
        } else if (type.equals("weave")) {
            return stateWeave;
        } else if (type.equals("cn")) {
            return stateCN;
        }
        return false;
    }

    private void addUnclaimed(JPanel basePanel, @NotNull JsonObject json) {
        for (Map.Entry<String, JsonElement> s : json.entrySet()) {
            if (!claimed.contains(s.getKey())) {
                // unclaimed
                if (s.getValue().isJsonPrimitive()) {
                    JPanel p = getSimplePanel(json, s.getKey());
                    basePanel.add(p);
                }
                if (s.getValue().isJsonObject()) {
                    JPanel subPanel = new JPanel();
                    subPanel.setBorder(new TitledBorder(null, s.getKey(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
                    subPanel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT));
                    basePanel.add(subPanel);
                    addUnclaimed(subPanel, s.getValue().getAsJsonObject());
                }
                if (s.getValue().isJsonArray()) {
                    JButton btnShowList = new JButton(s.getKey());
                    btnShowList.addActionListener((e) -> {
                        new ArgsConfigDialog(s.getKey(), json).setVisible(true);
                    });
                    basePanel.add(btnShowList);
                }
            }
        }
    }

    private JComboBox<String> getAutoSaveComboBox(JsonObject json, String key, @NotNull List<String> items) {
        JComboBox<String> cb = new JComboBox<>();
        for (String item : items) {
            cb.addItem(item);
        }
        cb.setSelectedItem(json.get(key).getAsString());
        cb.addActionListener((e) -> {
            JComboBox<String> source = (JComboBox<String>) e.getSource();
            String v = (String) source.getSelectedItem();
            json.addProperty(key, v);
            config.save();
        });
        return cb;
    }

    private @NotNull JPanel getSimplePanel(@NotNull JsonObject json, String key) {
        JPanel panel = new JPanel();
        JsonPrimitive value = json.getAsJsonPrimitive(key);
        if (value.isBoolean()) {
            JCheckBox cb = getAutoSaveCheckBox(json, key, key);
            panel.add(cb);
        } else if (value.isString()) {
            panel.add(new JLabel(key));
            JTextField input = getAutoSaveTextField(json, key);
            panel.add(input);
        } else if (value.isNumber()) {
            panel.add(new JLabel(key));
            JSpinner spinner = getAutoSaveSpinner(json, key, Double.MIN_VALUE, Double.MAX_VALUE);
            panel.add(spinner);

        }
        return panel;
    }

    @NotNull
    private static JSpinner getAutoSaveSpinner(@NotNull JsonObject json, String key, double min, double max) {
        JsonPrimitive value = json.getAsJsonPrimitive(key);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value.getAsDouble(), min, max, 0.01));
        spinner.setAutoscrolls(true);
        JComponent editor = spinner.getEditor();
        JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
        spinner.addChangeListener((e) -> {
            JSpinner source = (JSpinner) e.getSource();
            Number v = (Number) source.getValue();
            json.addProperty(key, v);
            config.save();
        });
        textField.setColumns(20);
        return spinner;
    }

    @NotNull
    private static JCheckBox getAutoSaveCheckBox(@NotNull JsonObject json, String key, String text) {
        JCheckBox cb = new JCheckBox(text);
        JsonPrimitive value = json.getAsJsonPrimitive(key);
        cb.setSelected(value.getAsBoolean());
        cb.addActionListener((e) -> {
            JCheckBox source = (JCheckBox) e.getSource();
            json.addProperty(key, source.isSelected());
            config.save();
        });
        return cb;
    }

    @NotNull
    private static JTextField getAutoSaveTextField(@NotNull JsonObject json, String key) {
        JsonPrimitive value = json.getAsJsonPrimitive(key);
        JTextField input = new JTextField(value.getAsString());
        input.addActionListener((e) -> {
            JTextField source = (JTextField) e.getSource();
            // save value
            json.addProperty(key, source.getText());
            config.save();
        });
        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                JTextField source = (JTextField) e.getSource();
                // save value
                json.addProperty(key, source.getText());
                config.save();
            }
        });
        return input;
    }

    /**
     * Mark a key as claimed and add the panel
     *
     * @param key      key in celestial.json
     * @param cfgPanel a panel to config this value
     */
    private void claim(String key, JPanel cfgPanel) {
        claim(key);
        panel.add(cfgPanel); // add the panel
    }

    private void claim(String key) {
        if (claimed.add(key)) {
            log.debug("Claimed " + key);
        } else {
            log.warn("Failed to claim " + key + " : always claimed.");
        }
    }
}
