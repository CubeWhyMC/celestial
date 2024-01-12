package org.cubewhy.celestial.gui.pages;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static org.cubewhy.celestial.Celestial.f;

public class GuiSettings extends JScrollPane {
    private static final JPanel panel = new JPanel();

    public GuiSettings() {
        super(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(new TitledBorder(null, f.getString("gui.settings.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        this.initGui();
    }

    private void initGui() {
        panel.add(new JLabel("settings"));
    }
}
