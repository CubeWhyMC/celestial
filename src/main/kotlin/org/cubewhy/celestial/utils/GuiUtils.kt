/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import org.cubewhy.celestial.Celestial
import java.awt.Desktop
import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter


fun chooseFile(filter: FileFilter?): File? {
    val fileDialog = JFileChooser()
    if (filter != null) {
        fileDialog.fileFilter = filter
        fileDialog.addChoosableFileFilter(filter)
    }
    fileDialog.fileSelectionMode = JFileChooser.FILES_ONLY
    return if ((fileDialog.showOpenDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION)) null else fileDialog.selectedFile
}

fun chooseFolder(): File? {
    val fileDialog = JFileChooser()
    fileDialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    return if ((fileDialog.showOpenDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION)) null else fileDialog.selectedFile
}

fun createButtonOpenFolder(text: String?, folder: File): JButton {
    val btn = JButton(text)
    btn.addActionListener {
        folder.mkdirs()
        Desktop.getDesktop().open(folder)
    }
    return btn
}


fun saveFile(filter: FileNameExtensionFilter?): File? {
    val fileDialog = JFileChooser()
    fileDialog.fileFilter = filter
    fileDialog.addChoosableFileFilter(filter)
    fileDialog.fileSelectionMode = JFileChooser.FILES_ONLY
    return if ((fileDialog.showSaveDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION)) null else fileDialog.selectedFile
}

/**
 * Get a empty label
 * */
fun emptyJLabel() = JLabel()
