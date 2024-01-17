/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.Celestial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

@Slf4j
public final class GuiUtils {
    private GuiUtils() {
    }

    public static @Nullable File chooseFile(FileFilter filter) {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileFilter(filter);
        fileDialog.addChoosableFileFilter(filter);
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        return (fileDialog.showOpenDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION) ? null : fileDialog.getSelectedFile();
    }

    public static @Nullable File chooseFolder() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return (fileDialog.showOpenDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION) ? null : fileDialog.getSelectedFile();
    }

    public static File saveFile(FileNameExtensionFilter filter) {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileFilter(filter);
        fileDialog.addChoosableFileFilter(filter);
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        return (fileDialog.showSaveDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION) ? null : fileDialog.getSelectedFile();
    }

    public static @NotNull JButton createButtonOpenFolder(String text, File folder) {
        JButton btn = new JButton(text);
        btn.addActionListener(e -> {
            try {
                if (folder.mkdirs()) {
                    log.info("Creating " + folder + " because the folder not exist");
                }
                Desktop.getDesktop().open(folder);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return btn;
    }
}
