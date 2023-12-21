package org.cubewhy.celestial.gui.pages;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.gui.elements.GuiVersionSelect;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;

import static org.cubewhy.celestial.Celestial.f;

@Slf4j
public class GuiVersion extends JPanel {
    public GuiVersion() throws IOException {
        this.setBorder(new TitledBorder(null, f.getString("gui.version.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.initGui();
    }

    private void initGui() throws IOException {
        this.add(new GuiVersionSelect());
    }
}
