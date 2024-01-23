/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import static cn.hutool.core.util.NumberUtil.isNumber;
import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.Celestial.proxy;
import static org.cubewhy.celestial.gui.GuiLauncher.statusBar;

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
            log.info("Check syntax");
            boolean status = this.checkSyntax();
            if (status) {
                JOptionPane.showMessageDialog(this, f.getString("gui.mirror.syntax.pass"), "Syntax check", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, f.getString("gui.mirror.syntax.incorrect"), "Syntax check", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(2, 2));
        buttons.add(btnCheckSyntax);

        this.add(buttons, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // save config
                if (!checkSyntax()) {
                    dispose();
                    return;
                }
                JsonObject json = asJson();
                proxy.applyMirrors(json);
                statusBar.setText(f.getString("giu.mirror.success"));
                dispose(); // close window
            }
        });

        loadFromJson(); // add texts to this.input
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

    private void loadFromJson() {
        JsonObject mirrors = proxy.getValue("mirror").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : mirrors.entrySet()) {
            String source = entry.getKey();
            String mirror = entry.getValue().getAsString();
            this.input.append(String.format("%s %s\n", source, mirror));
        }
    }

    private @NotNull JsonObject asJson() {
        JsonObject json = new JsonObject();
        for (String s : this.input.getText().split("\n")) {
            if (s.startsWith("#")) {
                continue;
            }
            String[] split = s.split(" ");
            String source = split[0];
            String mirror = split[1];
            json.addProperty(source, mirror);
        }
        return json;
    }
}
