package org.cubewhy.celestial.gui

import com.google.gson.JsonObject
import org.cubewhy.celestial.files.DownloadManager.cacheDir
import java.awt.Color
import java.awt.Desktop
import java.awt.Image
import java.awt.event.ActionEvent
import java.io.File
import java.io.IOException
import java.net.URI
import javax.swing.*
import javax.swing.border.TitledBorder

class LauncherNews(val json: JsonObject) : JPanel() {
    private val image = File(cacheDir, "news/" + json["title"].asString + ".png")

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
        val textLabel = JLabel(json["excerpt"].asString + " - " + json["author"].asString)
        this.add(textLabel)

        val image = ImageIcon(image.path)
        val imageLabel =
            JLabel(ImageIcon(image.image.getScaledInstance(400, 200, Image.SCALE_DEFAULT)), SwingConstants.CENTER)
        this.add(imageLabel)

        val jsonBtnText = json["button_text"]
        var text: String? = "View"
        if (!jsonBtnText.isJsonNull) {
            text = jsonBtnText.asString
        }
        val button = JButton(text)
        button.addActionListener {
            try {
                Desktop.getDesktop().browse(URI.create(json["link"].asString))
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
        }
        this.add(button)

        textLabel.labelFor = imageLabel
    }
}
