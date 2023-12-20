/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cubewhy.celestial.Celestial.*;

@Slf4j
public class GuiVersionSelect extends JPanel {
    private boolean isFinishOk = false;

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
        Map<String, Object> map = LauncherData.getSupportVersions(Celestial.metadata);
        List<String> supportVersions = (ArrayList<String>) map.get("versions");
        for (String version : supportVersions) {
            versionSelect.addItem(version);
        }
        versionSelect.addActionListener((e) -> {
            try {
                refreshModuleSelect(versionSelect, moduleSelect, this.isFinishOk);
                if (this.isFinishOk) {
                    saveVersion(versionSelect);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        moduleSelect.addActionListener((e) -> {
            if (this.isFinishOk) {
                saveModule(moduleSelect);
            }
        });
        refreshModuleSelect(versionSelect, moduleSelect, false);
        // get is first launch
        if (config.getValue("game").isJsonNull()) {
            log.info("Init ");
            JsonObject game = new JsonObject();
            game.addProperty("version", (String) versionSelect.getSelectedItem());
            game.addProperty("module", (String) moduleSelect.getSelectedItem());
            game.addProperty("branch", "master");
            config.setValue("game", game);
            versionSelect.setSelectedItem(map.get("default"));
        }
        initInput(versionSelect, moduleSelect, branchInput);
        isFinishOk = true;
    }

    private void initInput(@NotNull JComboBox<String> versionSelect, @NotNull JComboBox<String> moduleSelect, @NotNull JTextField branchInput) {
        JsonObject game = config.getValue("game").getAsJsonObject();
        versionSelect.setSelectedItem(game.get("version").getAsString());
        moduleSelect.setSelectedItem(game.get("module").getAsString());
        branchInput.setText(game.get("branch").getAsString());
    }

    private void saveVersion(@NotNull JComboBox<String> versionSelect) {
        String version = (String) versionSelect.getSelectedItem();
        log.info("Select version -> " + version);
        JsonObject game = config.getValue("game").getAsJsonObject();
        game.addProperty("version", version);
        config.setValue("game", game);
    }

    private void saveModule(@NotNull JComboBox<String> moduleSelect) {
        String module = (String) moduleSelect.getSelectedItem();
        log.info("Select module -> " + module);
        JsonObject game = config.getValue("game").getAsJsonObject();
        game.addProperty("module", module);
        config.setValue("game", game);
    }


    private void refreshModuleSelect(@NotNull JComboBox<String> versionSelect, JComboBox<String> moduleSelect, boolean reset) throws IOException {
        moduleSelect.removeAllItems();
        Map<String, Object> map = LauncherData.getSupportModules(metadata, (String) versionSelect.getSelectedItem());
        List<String> modules = (ArrayList<String>) map.get("modules");
        String defaultValue = (String) map.get("default");
        for (String module : modules) {
            moduleSelect.addItem(module);
        }
        if (reset) {
            moduleSelect.setSelectedItem(defaultValue);
        }
    }
}
