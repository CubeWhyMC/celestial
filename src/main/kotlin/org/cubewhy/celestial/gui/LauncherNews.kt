/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui

import cn.hutool.crypto.SecureUtil
import org.cubewhy.celestial.files.DownloadManager.cacheDir
import org.cubewhy.celestial.toJLabel
import org.cubewhy.celestial.toURI
import org.cubewhy.celestial.utils.lunar.Blogpost
import java.awt.Color
import java.awt.Desktop
import java.awt.Image
import java.io.File
import java.net.URI
import javax.swing.*
import javax.swing.border.TitledBorder

class LauncherNews(private val blogPost: Blogpost) : JPanel() {
    private val image = File(cacheDir, "news/" + SecureUtil.sha1(blogPost.title))

    init {
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.border = TitledBorder(
            null,
            blogPost.title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        this.initGui()
    }

    private fun initGui() {
        val isMoonsworth = blogPost.excerpt == null

        val textLabel: JLabel = if (isMoonsworth) {
            blogPost.title.toJLabel()
        } else {
            (blogPost.excerpt + " - " + blogPost.author).toJLabel()
        }

        this.add(textLabel)

        val image = ImageIcon(image.path)
        val imageLabel =
            JLabel(ImageIcon(image.image.getScaledInstance(400, 200, Image.SCALE_DEFAULT)), SwingConstants.CENTER)
        this.add(imageLabel)

        val text = if (isMoonsworth) {
            "View"
        } else {
            blogPost.buttonText ?: "View"
        }

        val button = JButton(text)
        button.addActionListener {
            Desktop.getDesktop().browse(blogPost.link.toURI())
        }
        this.add(button)

        textLabel.labelFor = imageLabel
    }
}
