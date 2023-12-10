package org.cubewhy.celestial.gui;

import org.cubewhy.celestial.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

import static org.cubewhy.celestial.Celestial.f;

public class GuiLauncher extends JFrame {
    public GuiLauncher() throws IOException {
        this.setBounds(100, 100, 1200, 700);
//        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setTitle(f.getString("gui.launcher.title"));
        this.setIconImage(new ImageIcon(FileUtils.readBytes(FileUtils.inputStreamFromClassPath("/images/icon.png"))).getImage());

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
        mainPanel.add("version", new GuiVersionSelect());

        // bind buttons
        btnPrevious.addActionListener(e -> {
            layout.previous(mainPanel);
        });
        btnNext.addActionListener(e -> {
            layout.next(mainPanel);
        });
        this.add(mainPanel); // add MainPanel
    }
}
