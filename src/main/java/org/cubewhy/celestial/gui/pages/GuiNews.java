package org.cubewhy.celestial.gui.pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.files.DownloadManager;
import org.cubewhy.celestial.gui.LauncherNews;
import org.cubewhy.celestial.utils.lunar.LauncherData;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

import static org.cubewhy.celestial.Celestial.f;
import static org.cubewhy.celestial.Celestial.metadata;

@Slf4j
public class GuiNews extends JScrollPane {
    private static final JPanel panel = new JPanel();
    private final JsonArray blogPosts;

    public GuiNews() throws MalformedURLException {
        super(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.setBorder(new TitledBorder(null, f.getString("gui.news.title"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.orange));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        blogPosts = LauncherData.getBlogPosts(metadata);
        this.initGui();
    }

    private void initGui() throws MalformedURLException {
        // render blogPosts
        log.info("Loading blogPosts (gui)");
        log.info(String.valueOf(blogPosts));
        for (JsonElement blogPost : blogPosts) {
            // cache the image if the image of the news isn't exist
            JsonObject json = blogPost.getAsJsonObject();
            String imageURL = json.get("image").getAsString();
            String title = json.get("title").getAsString();
            if (DownloadManager.cache(new URL(imageURL), title, false)) {
                // load
                panel.add(new LauncherNews(json));
            }
        }
    }
}
