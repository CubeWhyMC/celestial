package org.cubewhy.celestial.gui.pages;

import javax.swing.*;
import java.awt.*;

import static org.cubewhy.celestial.Celestial.f;

public class GuiAbout extends JPanel {
    public GuiAbout() {
        final JLabel l = new JLabel("<html><body><p>" + f.getString("gui.about").replace("\n", "<br>") + "</p></body></html>");
        Font font = new Font (null, Font.PLAIN, 13);
        l.setFont(font);
        this.add(l);
    }
}
