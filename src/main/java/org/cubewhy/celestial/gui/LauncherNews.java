package org.cubewhy.celestial.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.cubewhy.celestial.files.DownloadManager.cacheDir;

public class LauncherNews extends JPanel {

    public final JsonObject json;
    public final File image;

    public LauncherNews(JsonObject json) {
        this.json = json;
        this.image = new File(cacheDir, "news/" + json.get("title").getAsString() + ".png");
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(new TitledBorder(null, json.get("title").getAsString(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        this.initGui();
    }

    private void initGui() {
        final JLabel textLabel = new JLabel(json.get("excerpt").getAsString() + " - " + this.json.get("author").getAsString());
        this.add(textLabel);

        ImageIcon image = new ImageIcon(this.image.getPath());
        final JLabel imageLabel = new JLabel(new ImageIcon(image.getImage().getScaledInstance(400, 200, Image.SCALE_DEFAULT)), SwingConstants.CENTER);
        this.add(imageLabel);

        JsonElement jsonBtnText = this.json.get("button_text");
        String text = "View";
        if (!jsonBtnText.isJsonNull()) {
            text = jsonBtnText.getAsString();
        }
        JButton button = new JButton(jsonBtnText.getAsString());
        button.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(this.json.get("link").getAsString()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        this.add(button);

        textLabel.setLabelFor(imageLabel);
    }
}
