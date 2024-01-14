/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import org.cubewhy.celestial.Celestial;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

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
}
