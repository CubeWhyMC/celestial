package org.cubewhy.celestial.gui.pages;

import org.cubewhy.celestial.utils.GitUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static org.cubewhy.celestial.Celestial.config;
import static org.cubewhy.celestial.Celestial.f;

public class GuiAbout extends JPanel {
    public GuiAbout() {
        this.setBorder(new TitledBorder(null, f.getString("gui.about.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        String env = String.format("""
                
                Celestial v%s (Running on Java %s)
                Data sharing state: %s
                -----
                Git build info:
                    Build user: %s
                    Email: %s
                    Remote (%s): %s
                    Commit time: %s
                    Commit: %s
                    Commit Message: %s
                """, GitUtils.getBuildVersion(), System.getProperty("java.version"), config.getValue("data-sharing").getAsBoolean() ? "turn on" : "turn off", GitUtils.getBuildUser(), GitUtils.getBuildUserEmail(), GitUtils.getBranch(), GitUtils.getRemote(), GitUtils.getCommitTime(), GitUtils.getCommitId(true), GitUtils.getCommitMessage());

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

//        final JLabel l = new JLabel("<html><body><p>" + f.getString("gui.about").replace("\n", "<br>") + "</p></body></html>");
//        l.setFont(font);
//        this.add(l);
        JTextArea textArea = new JTextArea(f.getString("gui.about") + "\n" + env);
        Font font = new Font(null, Font.PLAIN, 14);
        textArea.setFont(font);
        textArea.setEditable(false);
        this.add(textArea);
    }
}
