package org.cubewhy.celestial.gui.pages;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static org.cubewhy.celestial.Celestial.f;

public class GuiSettings extends JPanel {
    public GuiSettings() {
        this.setBorder(new TitledBorder(null, f.getString("gui.version.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.setLayout(new GridLayout(3, 3, 5, 5));
        this.initGui();
    }

    private void initGui() {
        this.add(new JLabel("settings"));
    }
}
