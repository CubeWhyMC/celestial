package org.cubewhy.celestial.gui.pages

import cn.hutool.crypto.SecureUtil
import com.google.gson.JsonArray
import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.Celestial.metadata
import org.cubewhy.celestial.files.DownloadManager.cache
import org.cubewhy.celestial.gui.LauncherNews
import org.cubewhy.celestial.utils.TextUtils.dumpTrace
import org.cubewhy.celestial.utils.lunar.LauncherData.Companion.getBlogPosts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.IOException
import java.net.URL
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.TitledBorder


class GuiNews : JScrollPane(panel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED) {
    private val blogPosts: JsonArray

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
        blogPosts = getBlogPosts(metadata)
        this.initGui()
    }

    private fun initGui() {
        // render blogPosts
        getVerticalScrollBar().unitIncrement = 30
        log.info("Loading blogPosts (gui)")
        if (blogPosts.isJsonNull) {
            log.error("Failed to load blog posts")
            this.add(JLabel("Failed to load news (blogPosts is null)"))
        } else {
            for (blogPost in blogPosts) {
                // cache the image if the image of the news doesn't exist
                val json = blogPost.asJsonObject
                val imageURL = json["image"].asString
                val title = json["title"].asString
                try {
                    if (cache(URL(imageURL), "news/${SecureUtil.sha1(title)}", false)) {
                        // load news
                        panel.add(LauncherNews(json))
                    }
                } catch (e: IOException) {
                    log.warn("Failed to cache $imageURL")
                    val trace = dumpTrace(e)
                    log.error(trace)
                } catch (e: NullPointerException) {
                    // new API
                    panel.add(JLabel(f.getString("gui.news.official")))
                    log.warn("Failed to load news $imageURL")
                    log.error(dumpTrace(e))
                }
            }
        }
    }

    companion object {
        private val panel = JPanel()
        private val log: Logger = LoggerFactory.getLogger(GuiNews::class.java)
    }
}
