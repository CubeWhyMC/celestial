package org.cubewhy.celestial.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static org.cubewhy.celestial.Celestial.f;

public class GuiVersionSelect extends JPanel {
    public GuiVersionSelect() {
        this.setBorder(new TitledBorder(null, f.getString("gui.version-select.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new GridLayout(3, 2, 5, 5));

        this.initGui();
    }

    private void initGui() {
        JComboBox<String> versionSelect = new JComboBox<>();
        JComboBox<String> moduleSelect = new JComboBox<>();
        JTextField branchInput = new JTextField();
        this.add(new Label("Version"));
        this.add(versionSelect);
        this.add(new Label("Module"));
        this.add(moduleSelect);
        this.add(new Label("Branch"));
        this.add(branchInput);
    }
}
