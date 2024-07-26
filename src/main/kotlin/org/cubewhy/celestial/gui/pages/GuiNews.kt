package org.cubewhy.celestial.gui.pages

import cn.hutool.crypto.SecureUtil
import org.cubewhy.celestial.f
import org.cubewhy.celestial.files.DownloadManager.cache
import org.cubewhy.celestial.gui.LauncherBirthday
import org.cubewhy.celestial.gui.LauncherNews
import org.cubewhy.celestial.metadata
import org.cubewhy.celestial.toJLabel
import org.cubewhy.celestial.utils.lunar.Blogpost
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.TitledBorder
import kotlin.math.abs


class GuiNews : JScrollPane(panel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED) {
    private val blogPosts: List<Blogpost>

    init {
        this.border = TitledBorder(
            null,
            f.getString("gui.news.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        blogPosts = metadata.blogposts
        this.initGui()
    }


    private fun calcBirthday(): Int {
        val birthday = LocalDate.of(LocalDate.now().year, 7, 29)
        val today = LocalDate.now()

        return ChronoUnit.DAYS.between(today, birthday).toInt()
    }

    private fun initGui() {
        // render blogPosts
        getVerticalScrollBar().unitIncrement = 30
        log.info("Loading blogPosts (gui)")
        val birthday = calcBirthday()
        if (abs(birthday) <= 10) {
            this.add(LauncherBirthday(birthday))
        }
        if (blogPosts.isEmpty()) {
            log.error("Failed to load blog posts")
            this.add("Failed to load news (blogPosts is empty)".toJLabel())
        } else {
            for (blogPost in blogPosts) {
                // cache the image if the image of the news doesn't exist
                val imageURL = blogPost.image
                val title = blogPost.title
                try {
                    if (cache(URL(imageURL), "news/${SecureUtil.sha1(title)}", false)) {
                        // load news
                        panel.add(LauncherNews(blogPost))
                    }
                } catch (e: IOException) {
                    log.warn("Failed to cache $imageURL")
                    log.error(e.stackTraceToString())
                } catch (e: NullPointerException) {
                    // new API
                    panel.add(JLabel(f.getString("gui.news.official")))
                    log.warn("Failed to load news $imageURL")
                    log.error(e.stackTraceToString())
                }
            }
        }
    }

    companion object {
        private val panel = JPanel()
        private val log: Logger = LoggerFactory.getLogger(GuiNews::class.java)
    }
}
