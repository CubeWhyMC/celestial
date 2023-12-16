package org.cubewhy.celestial.gui.pages;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.utils.SystemUtils;
import org.cubewhy.celestial.utils.TextUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;

import static org.cubewhy.celestial.Celestial.f;

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
                new Thread(() -> {
                    try {
                        SystemUtils.callExternalProcess(process);
                    } catch (IOException | InterruptedException ex) {
                        String trace = TextUtils.dumpTrace(ex);
                        log.error(trace);
                    }
                }).start();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });
    }
}
