/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import com.google.gson.JsonObject;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.event.impl.GameStartEvent;
import org.cubewhy.celestial.event.impl.GameTerminateEvent;
import org.cubewhy.celestial.utils.CrashReportType;
import org.cubewhy.celestial.utils.SystemUtils;
import org.cubewhy.celestial.utils.TextUtils;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cubewhy.celestial.Celestial.*;

@Slf4j
public class GuiVersionSelect extends JPanel {
    private boolean isFinishOk = false;

    public GuiVersionSelect() throws IOException {
        this.setBorder(new TitledBorder(null, f.getString("gui.version-select.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new GridLayout(4, 2, 5, 5));

        this.initGui();
    }

    private void initGui() throws IOException {
        JComboBox<String> versionSelect = new JComboBox<>();
        JComboBox<String> moduleSelect = new JComboBox<>();
        JTextField branchInput = new JTextField();
        this.add(new JLabel(f.getString("gui.version-select.label.version")));
        this.add(versionSelect);
        this.add(new JLabel(f.getString("gui.version-select.label.module")));
        this.add(moduleSelect);
        this.add(new JLabel(f.getString("gui.version-select.label.branch")));
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

        // add launch buttons
        JButton btnOffline = new JButton(f.getString("gui.version.offline"));
        this.add(btnOffline);
        btnOffline.addActionListener(e -> {
            try {
                this.offline();
            } catch (IOException | InterruptedException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
            } catch (AttachNotSupportedException ignored) {
                log.warn("Failed to attach to the game process");
            }
        });
    }

    private void offline() throws IOException, InterruptedException, AttachNotSupportedException {
        if (gamePid != 0) {
            if (SystemUtils.findJava(LauncherData.getMainClass(null)) != null) {
                JOptionPane.showMessageDialog(this, f.getString("gui.version.launched.message"), f.getString("gui.version.launched.title"), JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                gamePid = 0;
            }
        }
        ProcessBuilder process = Celestial.launch();
        Process p = SystemUtils.callExternalProcess(process);
        new Thread(() -> {
            try {
                int code = p.waitFor();
                log.info("Game terminated");
                Celestial.gamePid = 0;
                new GameTerminateEvent().call();
                if (code != 0) {
                    // upload crash report
                    log.info("Client looks crashed, starting upload the log");
                    String trace = FileUtils.readFileToString(logFile, StandardCharsets.UTF_8);
                    String script = FileUtils.readFileToString(launchScript, StandardCharsets.UTF_8);
                    Map<String, String> map1 = launcherData.uploadCrashReport(trace, CrashReportType.GAME, script);
                    if (!map1.isEmpty()) {
                        String url = map1.get("url");
                        String id = map1.get("id");
                        JOptionPane.showMessageDialog(this, String.format("""
                                        Your client was crashed:
                                        Crash id: %s
                                        View your crash report at %s
                                        View the log of the latest launch: %s
                                                                                
                                        *%s*""", id, url, logFile.getPath(), f.getString("gui.version.crash.tip")), "Game crashed!", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, String.format("""
                                        Your client was crashed:
                                        View the log of the latest launch: %s
                                        *%s*
                                        """, logFile.getPath(), f.getString("gui.version.crash.tip")));
                    }
                }
            } catch (IOException | InterruptedException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
            }
        }).start();

        new Thread(() -> {
            // find the game process
            try {
                Thread.sleep(3000); // sleep 10s
            } catch (InterruptedException ignored) {

            }
            if (p.isAlive()) {
                try {
                    VirtualMachine java = SystemUtils.findJava(LauncherData.getMainClass(null));
                    assert java != null;
                    String id = java.id();
                    gamePid = Long.parseLong(id);
                    java.detach();
                } catch (Exception ex) {
                    log.error("Failed to get the real pid of the game, is Celestial launched non java program?");
                    log.warn("process.pid() will be used to get the process id, which may not be the real PID");
                    gamePid = p.pid();
                }
                log.info("Pid: " + gamePid);
                new GameStartEvent(gamePid).call();
            }
        }).start();
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


    private void refreshModuleSelect(@NotNull JComboBox<String> versionSelect, @NotNull JComboBox<String> moduleSelect, boolean reset) throws IOException {
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
