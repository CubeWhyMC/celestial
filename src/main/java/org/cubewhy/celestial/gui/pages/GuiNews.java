package org.cubewhy.celestial.gui.pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.utils.lunar.LauncherData;

import javax.swing.*;

import static org.cubewhy.celestial.Celestial.metadata;

@Slf4j
public class GuiNews extends JScrollPane {
    private static final JPanel panel = new JPanel();
    private final JsonArray blogPosts;

    public GuiNews() {
        super(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        blogPosts = LauncherData.getBlogPosts(metadata);
        this.initGui();
    }

    private void initGui() {
        // render blogPosts
        log.info("Loading blogPosts (gui)");
        log.info(String.valueOf(blogPosts));
        for (JsonElement blogPost : blogPosts) {
            String excerpt = blogPost.getAsJsonObject().get("excerpt").getAsString();
            log.debug("Excerpt: " + excerpt);
            panel.add(new JLabel(excerpt));
        }
    }
}
