/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.Celestial.metadata;

@Slf4j
public class GuiVersionSelect extends JPanel {
    public GuiVersionSelect() throws IOException {
        this.setBorder(new TitledBorder(null, f.getString("gui.version-select.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new GridLayout(3, 2, 5, 5));

        this.initGui();
    }

    private void initGui() throws IOException {
        JComboBox<String> versionSelect = new JComboBox<>();
        JComboBox<String> moduleSelect = new JComboBox<>();
        JTextField branchInput = new JTextField();
        this.add(new Label("Version"));
        this.add(versionSelect);
        this.add(new Label("Module"));
        this.add(moduleSelect);
        this.add(new Label("Branch"));
        this.add(branchInput);

        // add items
        List<String> supportVersions = LauncherData.getSupportVersions(Celestial.metadata);
        for (String version : supportVersions) {
            versionSelect.addItem(version);
        }
        versionSelect.addActionListener((e) -> {
            try {
                refreshModuleSelect(versionSelect, moduleSelect);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void refreshModuleSelect(@NotNull JComboBox<String> versionSelect, JComboBox<String> moduleSelect) throws IOException {
        moduleSelect.removeAllItems();
        List<String> modules = LauncherData.getSupportModules(metadata, (String) versionSelect.getSelectedItem());
        for (String module : modules) {
            moduleSelect.addItem(module);
        }
    }
}
