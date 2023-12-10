package org.cubewhy.celestial.gui.pages;

import javax.swing.*;
import java.awt.*;

public class GuiNews extends JPanel {
    public GuiNews() {
        this.setLayout(new GridLayout(3, 3, 5, 5));
        this.initGui();
    }

    private void initGui() {
        this.add(new JLabel("test"));
    }
}
