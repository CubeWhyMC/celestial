/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.utils

import org.cubewhy.celestial.Celestial
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.File
import java.io.IOException
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter

object GuiUtils {
    private val log: Logger = LoggerFactory.getLogger(GuiUtils::class.java)

    @JvmStatic
    fun chooseFile(filter: FileFilter?): File? {
        val fileDialog = JFileChooser()
        if (filter != null) {
            fileDialog.fileFilter = filter
            fileDialog.addChoosableFileFilter(filter)
        }
        fileDialog.fileSelectionMode = JFileChooser.FILES_ONLY
        return if ((fileDialog.showOpenDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION)) null else fileDialog.selectedFile
    }

    @JvmStatic
    fun chooseFolder(): File? {
        val fileDialog = JFileChooser()
        fileDialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        return if ((fileDialog.showOpenDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION)) null else fileDialog.selectedFile
    }

    @JvmStatic
    fun saveFile(filter: FileNameExtensionFilter?): File? {
        val fileDialog = JFileChooser()
        fileDialog.fileFilter = filter
        fileDialog.addChoosableFileFilter(filter)
        fileDialog.fileSelectionMode = JFileChooser.FILES_ONLY
        return if ((fileDialog.showSaveDialog(Celestial.launcherFrame) == JFileChooser.CANCEL_OPTION)) null else fileDialog.selectedFile
    }

    @JvmStatic
    fun createButtonOpenFolder(text: String?, folder: File): JButton {
        val btn = JButton(text)
        btn.addActionListener {
            try {
                if (folder.mkdirs()) {
                    log.info("Creating $folder because the folder not exist")
                }
                Desktop.getDesktop().open(folder)
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
        }
        return btn
    }
}
