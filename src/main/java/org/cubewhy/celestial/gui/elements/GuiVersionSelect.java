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
import org.cubewhy.celestial.files.DownloadManager;
import org.cubewhy.celestial.utils.CrashReportType;
import org.cubewhy.celestial.utils.SystemUtils;
import org.cubewhy.celestial.utils.TextUtils;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.NotActiveException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.cubewhy.celestial.Celestial.*;
import static org.cubewhy.celestial.gui.GuiLauncher.statusBar;

@Slf4j
public class GuiVersionSelect extends JPanel {
    private final JComboBox<String> versionSelect = new JComboBox<>();
    private final JComboBox<String> moduleSelect = new JComboBox<>();
    private final JTextField branchInput = new JTextField();
    private boolean isFinishOk = false;
    private final JButton btnOnline = new JButton(f.getString("gui.version.online"));
    private final JButton btnOffline = new JButton(f.getString("gui.version.offline"));

    private interface CreateProcess {
        Process create() throws IOException;
    }

    public GuiVersionSelect() throws IOException {
        this.setBorder(new TitledBorder(null, f.getString("gui.version-select.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new GridLayout(4, 2, 5, 5));

        this.initGui();
    }

    private void initGui() throws IOException {
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
                refreshModuleSelect(this.isFinishOk);
                if (this.isFinishOk) {
                    saveVersion();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        moduleSelect.addActionListener((e) -> {
            if (this.isFinishOk) {
                saveModule();
            }
        });
        refreshModuleSelect(false);
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
        btnOnline.addActionListener(e -> {
            try {
                this.online();
            } catch (Exception ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
            }
        });
        this.add(btnOnline);

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

    private void beforeLaunch() throws IOException, AttachNotSupportedException {
        Celestial.completeSession();
        if (gamePid != 0) {
            if (SystemUtils.findJava(LauncherData.getMainClass(null)) != null) {
                JOptionPane.showMessageDialog(this, f.getString("gui.version.launched.message"), f.getString("gui.version.launched.title"), JOptionPane.WARNING_MESSAGE);
            } else {
                gamePid = 0;
            }
        }
    }

    private void runGame(CreateProcess cp, Runnable run) throws IOException {
        final Process[] p = new Process[1]; // create process

        Thread threadGetId = new Thread(() -> {
            // find the game process
            try {
                Thread.sleep(3000); // sleep 3s
            } catch (InterruptedException ignored) {

            }
            if (p[0].isAlive()) {
                try {
                    VirtualMachine java = SystemUtils.findJava(LauncherData.getMainClass(null));
                    assert java != null;
                    String id = java.id();
                    gamePid = Long.parseLong(id);
                    java.detach();
                } catch (Exception ex) {
                    log.error("Failed to get the real pid of the game, is Celestial launched non java program?");
                    log.warn("process.pid() will be used to get the process id, which may not be the real PID");
                    gamePid = p[0].pid();
                }
                log.info("Pid: " + gamePid);
                statusBar.setText(String.format(f.getString("status.launch.started"), gamePid));
                new GameStartEvent(gamePid).call();
            }
        });
        new Thread(() -> {
            try {
                if (run != null) {
                    run.run();
                }
                p[0] = cp.create();
                threadGetId.start();
                int code = p[0].waitFor();
                log.info("Game terminated");
                statusBar.setText(f.getString("status.launch.terminated"));
                Celestial.gamePid = 0;
                new GameTerminateEvent().call();
                if (code != 0) {
                    // upload crash report
                    statusBar.setText(f.getString("status.launch.crashed"));
                    log.info("Client looks crashed");
                    try {
                        if (config.getConfig().has("data-sharing") && config.getValue("data-sharing").getAsBoolean()) {
                            String trace = FileUtils.readFileToString(gameLogFile, StandardCharsets.UTF_8);
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
                                                                                
                                        *%s*""", id, url, gameLogFile.getPath(), f.getString("gui.version.crash.tip")), "Game crashed!", JOptionPane.ERROR_MESSAGE);
                            } else {
                                throw new RuntimeException("Failed to upload crash report");
                            }
                        } else {
                            throw new NotActiveException();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, String.format("""
                                Your client was crashed:
                                View the log of the latest launch: %s
                                *%s*
                                """, gameLogFile.getPath(), f.getString("gui.version.crash.tip")), "Game crashed!", JOptionPane.ERROR_MESSAGE);
                        if (!(e instanceof NotActiveException)) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } catch (InterruptedException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void online() throws IOException, AttachNotSupportedException {
        beforeLaunch();
        File natives = launch((String) versionSelect.getSelectedItem(), branchInput.getText(), (String) moduleSelect.getSelectedItem());
        if (natives == null) {
            JOptionPane.showMessageDialog(this, f.getString("gui.launch.server.failure.message"), f.getString("gui.launch.server.failure.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        runGame(() -> {
            try {
                statusBar.setText(f.getString("status.launch.call-process"));
                return SystemUtils.callExternalProcess(launch());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, () -> {
            try {
                statusBar.setText(f.getString("status.launch.begin"));
                Celestial.checkUpdate((String) versionSelect.getSelectedItem(), (String) moduleSelect.getSelectedItem(), branchInput.getText());
                DownloadManager.waitForAll();
                try {
                    statusBar.setText(f.getString("status.launch.natives"));
                    org.cubewhy.celestial.utils.FileUtils.unzipNatives(natives, new File(config.getValue("installation-dir").getAsString()));
                } catch (Exception e) {
                    String trace = TextUtils.dumpTrace(e);
                    log.error("Is game launched? Failed to unzip natives.");
                    log.error(trace);
                }
                // exec, run
                log.info("Everything is OK, starting game...");
            } catch (Exception e) {
                log.error("Failed to check update");
                String trace = TextUtils.dumpTrace(e);
                log.error(trace);
                JOptionPane.showMessageDialog(null, f.getString("gui.check-update.error.message"), f.getString("gui.check-update.error.title"), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void offline() throws IOException, InterruptedException, AttachNotSupportedException {
        beforeLaunch();
        ProcessBuilder process = Celestial.launch();
        runGame(() -> {
            try {
                statusBar.setText(f.getString("status.launch.call-process"));
                return SystemUtils.callExternalProcess(process);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, null);
    }

    private void initInput(@NotNull JComboBox<String> versionSelect, @NotNull JComboBox<String> moduleSelect, @NotNull JTextField branchInput) {
        JsonObject game = config.getValue("game").getAsJsonObject();
        versionSelect.setSelectedItem(game.get("version").getAsString());
        moduleSelect.setSelectedItem(game.get("module").getAsString());
        branchInput.setText(game.get("branch").getAsString());
    }

    private void saveVersion() {
        String version = (String) versionSelect.getSelectedItem();
        log.info("Select version -> " + version);
        JsonObject game = config.getValue("game").getAsJsonObject();
        game.addProperty("version", version);
        config.setValue("game", game);
    }

    private void saveModule() {
        String module = (String) moduleSelect.getSelectedItem();
        log.info("Select module -> " + module);
        JsonObject game = config.getValue("game").getAsJsonObject();
        game.addProperty("module", module);
        config.setValue("game", game);
    }


    private void refreshModuleSelect(boolean reset) throws IOException {
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
