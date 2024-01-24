/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs

import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.files.DownloadManager
import org.cubewhy.celestial.files.Downloadable
import org.cubewhy.celestial.game.RemoteAddon
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.*
import javax.swing.border.TitledBorder

/**
 * @param addon remote addon
 * @param file path to save the addon
 * */
class AddonInfoDialog(val addon: RemoteAddon, val file: File) : JDialog() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AddonInfoDialog::class.java)
    }

    init {
        this.title = f.getString("gui.plugins.info.title")
        this.setSize(600, 600)
        this.layout = VerticalFlowLayout()
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.isLocationByPlatform = true
        this.initGui()
    }

    private fun initGui() {
        this.add(JLabel(f.getString("gui.plugins.info.name").format(addon.name)))
        this.add(JLabel(f.getString("gui.plugins.info.category").format(addon.category)))
        val exist = JLabel(f.getString("gui.plugins.exist"))

        val btnDownload = JButton(f.getString("gui.plugins.download"))
        btnDownload.addActionListener {
            DownloadManager.download(Downloadable(addon.downloadURL, file, null))
            Thread {
                try {
                    DownloadManager.waitForAll()
                    exist.isVisible = file.exists()
                } catch (err: InterruptedException) {
                    throw RuntimeException(err)
                }
            }.start()
        }

        this.add(exist)
        exist.isVisible = file.exists()

        this.add(btnDownload)
        this.add(JSeparator())

        val metaInfo = JPanel()
        metaInfo.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
        metaInfo.border = TitledBorder(
            null,
            f.getString("gui.plugins.info.meta"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        if (addon.meta == null) {
            metaInfo.add(JLabel(f.getString("gui.plugins.info.meta.notfound")))
        } else {
            val meta = addon.meta
            metaInfo.add(JLabel(f.getString("gui.plugins.info.meta.name").format(meta.name)))
            metaInfo.add(JLabel(f.getString("gui.plugins.info.meta.version").format(meta.version)))
            metaInfo.add(JLabel(f.getString("gui.plugins.info.meta.description").format(meta.description)))
            if (meta.website != null) metaInfo.add(createOpenWebsiteButton(f.getString("gui.plugins.info.meta.website"), meta.website.toURI()))
            if (meta.repository != null) metaInfo.add(createOpenWebsiteButton(f.getString("gui.plugins.info.meta.repo"), meta.repository.toURI()))
        }
        this.add(metaInfo)
    }

    private fun createOpenWebsiteButton(text: String, uri: URI) : JButton {
        val button = JButton(text)
        button.addActionListener {
            Desktop.getDesktop().browse(uri)
        }
        return button
    }
}

fun String.toURI(): URI {
    return URI.create(this)
}
