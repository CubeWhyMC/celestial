/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.*;

import static org.cubewhy.celestial.Celestial.config;
import static org.cubewhy.celestial.Celestial.f;

@Slf4j
public class ArgsConfigDialog extends JDialog {

    private final String key;
    private final JsonObject json;
    private final JsonArray array;

    public ArgsConfigDialog(String key, @NotNull JsonObject json) {
        this.key = key;
        this.json = json;
        this.array = json.getAsJsonArray(key);
        this.setLayout(new GridLayout(2, 1));
        this.initGui();
        this.setSize(600, 600);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setLocationByPlatform(true);
    }

    private void initGui() {
        this.setTitle(f.getString("gui.settings.args.title"));

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> args = new JList<>(model);
        // load args from config
        if (array.isEmpty()) {
            // TODO remove this label
            this.add(new JLabel("Looks nothing here, try to add something?"));
        } else {
            for (JsonElement element : array) {
                model.addElement(element.getAsString());
            }
        }
        this.add(args);
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
        // btnAdd
        JButton btnAdd = new JButton(f.getString("gui.settings.args.add"));
        btnAdd.addActionListener((e) -> {
            String arg = JOptionPane.showInputDialog(this, f.getString("gui.settings.args.add.message"));
            if (arg != null) {
                this.addArg(arg, model);
            }
        });
        // btnRemove
        JButton btnRemove = new JButton(f.getString("gui.settings.args.remove"));
        btnRemove.addActionListener((e) -> {
            if (JOptionPane.showConfirmDialog(this, String.format(f.getString("gui.settings.args.remove.confirm"), args.getSelectedValue()), "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }
            ;
            int index = args.getSelectedIndex();
            this.removeArg(index, model);
        });
        panelButtons.add(btnAdd);
        panelButtons.add(btnRemove);
        this.add(panelButtons);
    }

    private void addArg(String arg, @NotNull DefaultListModel<String> model) {
        this.array.add(arg);
        model.addElement(arg);
        log.info("Add a arg to " + this.key + " -> " + arg);
        config.save();
    }

    private void removeArg(int index, @NotNull DefaultListModel<String> model) {
        String arg = model.get(index);
        model.remove(index);
        this.array.remove(index);
        log.info("Remove a arg to " + this.key + " -> " + arg);
        config.save();
    }
}
