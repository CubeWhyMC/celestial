/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.utils;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.cubewhy.celestial.Celestial;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public final class GuiUtils {
    private GuiUtils() {
    }

    public static @Nullable File chooseFile(FileFilter filter) {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.addChoosableFileFilter(filter);
        return (fileDialog.showOpenDialog(Celestial.launcherFrame) == JFileChooser.APPROVE_OPTION) ? null : fileDialog.getSelectedFile();
    }
}
