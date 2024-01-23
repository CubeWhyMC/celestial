/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.files.ProxyConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;

import static cn.hutool.core.util.NumberUtil.isNumber;
import static org.cubewhy.celestial.Celestial.f;

@Slf4j
public class MirrorDialog extends JDialog {

    private JTextArea input;

    public MirrorDialog() {
        super();

        this.setTitle(f.getString("gui.mirror.title"));
        this.setSize(600, 600);
        this.setLayout(new BorderLayout());
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setLocationByPlatform(true);
        this.initGui();
    }

    private void initGui() {
        this.input = new JTextArea(getHeader());
        this.add(input);

        JButton btnCheckSyntax = new JButton(f.getString("gui.mirror.syntax"));

        btnCheckSyntax.addActionListener((e) -> {
            boolean status = this.checkSyntax();
            log.info("Check syntax");
        });

        this.add(btnCheckSyntax, BorderLayout.SOUTH);
    }

    private @NotNull String getHeader() {
        String[] lines = f.getString("gui.mirror.header").split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append("# ").append(line).append("\n");
        }
        return sb.toString();
    }

    private boolean checkSyntax() {
        for (String s : this.input.getText().split("\n")) {
            if (s.startsWith("#")) {
                continue;
            }
            String[] addresses = s.split(" ");
            if (addresses.length != 2) {
                return false;
            }
            // check port is number
            for (String address : addresses) {
                String[] split = address.split(":");
                if (split.length != 2) {
                    return false; // wtf this
                }
                String port = split[1];
                if (!isNumber(port)) {
                    return false;
                }
            }
        }
        return true;
    }
}
