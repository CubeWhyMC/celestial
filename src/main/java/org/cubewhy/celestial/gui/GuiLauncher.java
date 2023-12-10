package org.cubewhy.celestial.gui;

import javax.swing.*;
import java.awt.*;

import static org.cubewhy.celestial.Celestial.f;

public class GuiLauncher extends JFrame {
    public GuiLauncher() {
        this.setBounds(100, 100, 1200, 700);
//        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setTitle(f.getString("gui.launcher.title"));

        this.initGui();
    }

    /**
     * Init Celestial Launcher (gui)
     */
    private void initGui() {
        // menu
        Panel menu = new Panel();
        Button btnPrevious = new Button("previous");
        Button btnNext = new Button("next");
        menu.add(btnPrevious);
        menu.add(btnNext);
        menu.setSize(100, 20);

        this.add(menu, BorderLayout.NORTH);
        // main panel
        GuiVersionSelect versionSelect = new GuiVersionSelect();
        this.add(versionSelect);
        versionSelect.setLocation(0, 0);
        versionSelect.setSize(60, 40);
    }
}
