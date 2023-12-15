package org.cubewhy.celestial.gui;

import com.google.gson.JsonObject;
import org.cubewhy.celestial.utils.lunar.LauncherData;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.files.DownloadManager.cachesDir;

public class LauncherNews extends JPanel {

    public final JsonObject json;
    public final File image;

    public LauncherNews(JsonObject json) {
        this.json = json;
        this.image = new File(cachesDir, json.get("title").getAsString() + ".png");

        this.initGui();
    }

    private void initGui() {
        this.add(new JLabel(json.get("excerpt").getAsString()));

        final JLabel imageLabel = new JLabel(new ImageIcon(this.image.getPath()), SwingConstants.CENTER);
        this.add(imageLabel);
    }
}
