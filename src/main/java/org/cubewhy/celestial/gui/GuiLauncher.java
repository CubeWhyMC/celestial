package org.cubewhy.celestial.gui;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.gui.pages.GuiAbout;
import org.cubewhy.celestial.gui.pages.GuiNews;
import org.cubewhy.celestial.gui.pages.GuiSettings;
import org.cubewhy.celestial.utils.FileUtils;
import org.cubewhy.celestial.utils.lunar.LauncherData;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.cubewhy.celestial.Celestial.*;

@Slf4j
public class GuiLauncher extends JFrame {
    public GuiLauncher() throws IOException {
        this.setBounds(100, 100, 1200, 700);
        this.setTitle(f.getString("gui.launcher.title"));

        // init icon
        this.resetIcon();

        this.initGui();
        // show alert
        Map<String, String> alert = LauncherData.getAlert(metadata);
        if (alert != null) {
            String title = alert.get("title");
            String message = alert.get("message");
            log.info(title + ": " + message);
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Init Celestial Launcher (gui)
     */
    private void initGui() {
        // menu
        Panel menu = new Panel();
        Button btnPrevious = new Button("previous");
        Button btnNext = new Button("next");
        // For developers: It is not recommended to remove the Donate button in Celestial Launcher's derivative versions
        // 不建议在衍生版本中删除赞助按钮
        // Celestial 是免费开源的启动器, 请赞助来帮助我们走得更远 (收入会全部用于开发) , 若你不想再次看到这个按钮, 可以在config.json中关闭
        // Celestial is a opensource launcher, please donate to let us go further (All money will be used for development), If you don't want to see this button again, you can turn it off in config.json
        Button btnDonate = new Button(f.getString("gui.donate"));
        btnDonate.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://www.lunarclient.top/donate"));
            } catch (IOException ignored) {
            }
        });
        menu.add(btnPrevious);
        menu.add(btnNext);
        menu.add(btnDonate);
        menu.setSize(100, 20);

        this.add(menu, BorderLayout.NORTH);
        // main panel
        final Panel mainPanel = new Panel();
        final CardLayout layout = new CardLayout();
        mainPanel.setLayout(layout);
        // TODO: add enabled pages (from metadata)
        // add pages
        mainPanel.add("news", new GuiNews());
        mainPanel.add("version", new GuiVersionSelect());
        mainPanel.add("settings", new GuiSettings());
        mainPanel.add("about", new GuiAbout());

        // bind buttons
        btnPrevious.addActionListener(e -> {
            layout.previous(mainPanel);
        });
        btnNext.addActionListener(e -> {
            layout.next(mainPanel);
        });
        this.add(mainPanel); // add MainPanel
    }

    /**
     * Load icon image from /images/icons
     *
     * @param name file name
     * */
    public void setIconImage(String name) throws IOException {
        this.setIconImage(new ImageIcon(FileUtils.readBytes(FileUtils.inputStreamFromClassPath("/images/icons/" + name + ".png"))).getImage());
    }

    /**
     * Reset the icon
     * */
    public void resetIcon() throws IOException {
        String themeType = config.getValue("theme").getAsString();
        switch (themeType) {
            case "dark":
                this.setIconImage("icon-light");
                break;
            case "light":
            default:
                this.setIconImage("icon-dark");
        }
    }
}
