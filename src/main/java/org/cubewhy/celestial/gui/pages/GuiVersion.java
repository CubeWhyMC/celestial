package org.cubewhy.celestial.gui.pages;

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

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.cubewhy.celestial.Celestial.*;

@Slf4j
public class GuiVersion extends JPanel {
    public GuiVersion() {
        this.setBorder(new TitledBorder(null, f.getString("gui.version.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.initGui();
    }

    private void initGui() {
        JButton btnOffline = new JButton(f.getString("gui.version.offline"));
        this.add(btnOffline);

        btnOffline.addActionListener(e -> {
            try {
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
                            Map<String, String> map = launcherData.uploadCrashReport(trace, CrashReportType.GAME, script);
                            if (!map.isEmpty()) {
                                String url = map.get("url");
                                String id = map.get("id");
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
                            gamePid = Integer.parseInt(id);
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
            } catch (IOException | InterruptedException ex) {
                String trace = TextUtils.dumpTrace(ex);
                log.error(trace);
            }
        });
    }
}
