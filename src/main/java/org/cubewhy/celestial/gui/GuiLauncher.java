package org.cubewhy.celestial.gui;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.event.EventManager;
import org.cubewhy.celestial.event.EventTarget;
import org.cubewhy.celestial.event.impl.AuthEvent;
import org.cubewhy.celestial.event.impl.GameStartEvent;
import org.cubewhy.celestial.event.impl.GameTerminateEvent;
import org.cubewhy.celestial.gui.elements.StatusBar;
import org.cubewhy.celestial.gui.pages.*;
import org.cubewhy.celestial.utils.FileUtils;
import org.cubewhy.celestial.utils.SystemUtils;
import org.cubewhy.celestial.utils.TextUtils;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.cubewhy.celestial.Celestial.*;

@Slf4j
public class GuiLauncher extends JFrame {

    public static final JLabel statusBar = new StatusBar();

    public GuiLauncher() throws IOException {
        // register with EventManager
        EventManager.register(this);

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
    private void initGui() throws IOException {
        this.add(statusBar, BorderLayout.SOUTH);
        // menu
        Panel menu = new Panel();
        JButton btnPrevious = new JButton(f.getString("gui.previous"));
        JButton btnNext = new JButton(f.getString("gui.next"));
        // For developers: It is not recommended to remove the Donate button in Celestial Launcher's derivative versions
        // 不建议在衍生版本中删除赞助按钮
        // Celestial 是免费开源的启动器, 请赞助来帮助我们走得更远 (收入会全部用于开发)
        // Celestial is an opensource launcher, please donate to let us go further (All money will be used for development)
        JButton btnDonate = new JButton(f.getString("gui.donate"));
        JButton btnHelp = new JButton(f.getString("gui.help"));
        btnDonate.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://www.lunarclient.top/donate"));
            } catch (IOException ignored) {
            }
        });
        btnHelp.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://www.lunarclient.top/help"));
            } catch (IOException ignored) {
            }
        });

        menu.add(btnPrevious);
        menu.add(btnNext);
        menu.add(btnDonate);
        menu.add(btnHelp);
//        menu.add(btnLanguage);
        menu.setSize(100, 20);

        this.add(menu, BorderLayout.NORTH);
        // main panel
        final Panel mainPanel = new Panel();
        final CardLayout layout = new CardLayout();
        mainPanel.setLayout(layout);
        // TODO: add enabled pages (from metadata)
        // add pages
        mainPanel.add("news", new GuiNews());
        mainPanel.add("version", new GuiVersion());
        mainPanel.add("plugins", new GuiPlugins());
        mainPanel.add("settings", new GuiSettings());
        mainPanel.add("about", new GuiAbout());

        // bind buttons
        btnPrevious.addActionListener(e -> layout.previous(mainPanel));
        btnNext.addActionListener(e -> layout.next(mainPanel));
        this.add(mainPanel); // add MainPanel

        // try to find the exist game process
        new Thread(() ->{
            try {
                this.findExistGame();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void findExistGame() throws IOException {
        try {
            VirtualMachine java = SystemUtils.findJava(LauncherData.getMainClass(null));
            if (java != null) {
                String pid = java.id();
                log.info("Exist game process found! Pid: " + pid);
                gamePid.set(Long.parseLong(pid));
                JOptionPane.showMessageDialog(this, String.format(f.getString("gui.launcher.game.exist.message"), pid), f.getString("gui.launcher.game.exist.title"), JOptionPane.INFORMATION_MESSAGE);
                java.detach();
            }
        } catch (AttachNotSupportedException e) {
            log.error("Failed to find the game process, is launched with the official launcher? (attach not support)");
            log.error(TextUtils.dumpTrace(e));
        }
    }


    /**
     * Load icon image from /images/icons
     *
     * @param name file name
     */
    public void setIconImage(String name) throws IOException {
        this.setIconImage(new ImageIcon(FileUtils.readBytes(FileUtils.inputStreamFromClassPath("/images/icons/" + name + ".png"))).getImage());
    }

    /**
     * Reset the icon
     */
    public void resetIcon() throws IOException {
        String themeType = config.getValue("theme").getAsString();
        switch (themeType) {
            case "light":
                this.setIconImage("icon-dark");
            case "dark":
            default:
                this.setIconImage("icon-light");
                break;
        }
    }

    @EventTarget
    public void onGameStart(GameStartEvent e) throws IOException {
        this.setIconImage("running");
    }

    @EventTarget
    public void onGameTerminate(GameTerminateEvent e) throws IOException {
        this.resetIcon();
    }

    @EventTarget
    public void onAuth(@NotNull AuthEvent e) {
        log.info("Request for login");
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(e.authURL.toString()), null);
        String link = JOptionPane.showInputDialog(this, f.getString("gui.launcher.auth.message"), f.getString("gui.launcher.auth.title"), JOptionPane.QUESTION_MESSAGE);
        e.put(link);
    }
}
