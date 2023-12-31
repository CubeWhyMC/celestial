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

import java.awt.*;
import java.io.File;

public final class GuiUtils {
    private GuiUtils() {
    }

    public static @Nullable File chooseFile(AbstractFileFilter filter) {
        FileDialog fileDialog = new FileDialog(Celestial.launcherFrame, "Choosing a file", FileDialog.LOAD);
        fileDialog.setFilenameFilter(filter); // jar
        fileDialog.setVisible(true);
        return (fileDialog.getFile() == null) ? null : new File(fileDialog.getDirectory(), fileDialog.getFile());
    }
}
