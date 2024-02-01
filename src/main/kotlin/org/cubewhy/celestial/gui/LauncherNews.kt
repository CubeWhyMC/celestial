/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui

import cn.hutool.crypto.SecureUtil
import com.google.gson.JsonObject
import org.cubewhy.celestial.files.DownloadManager.cacheDir
import java.awt.Color
import java.awt.Desktop
import java.awt.Image
import java.io.File
import java.net.URI
import javax.swing.*
import javax.swing.border.TitledBorder

class LauncherNews(val json: JsonObject) : JPanel() {
    private val image = File(cacheDir, "news/" + SecureUtil.sha1(json["title"].asString))

    init {
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.border = TitledBorder(
            null,
            json["title"].asString,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        this.initGui()
    }

    private fun initGui() {
        val isMoonsworth = !json.has("excerpt")

        val textLabel: JLabel = if (isMoonsworth) {
            JLabel(json["title"].asString)
        } else {
            JLabel(json["excerpt"].asString + " - " + json["author"].asString)
        }

        this.add(textLabel)

        val image = ImageIcon(image.path)
        val imageLabel =
            JLabel(ImageIcon(image.image.getScaledInstance(400, 200, Image.SCALE_DEFAULT)), SwingConstants.CENTER)
        this.add(imageLabel)

        val text = if (isMoonsworth) {
            "View"
        } else {
            val jsonBtnText = json["button_text"]
            if (!jsonBtnText.isJsonNull) {
                jsonBtnText.asString
            } else {
                "View"
            }
        }

        val button = JButton(text)
        button.addActionListener {
            Desktop.getDesktop().browse(URI.create(json["link"].asString))
        }
        this.add(button)

        textLabel.labelFor = imageLabel
    }
}
