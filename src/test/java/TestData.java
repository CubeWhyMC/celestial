import com.google.gson.JsonObject;
import org.cubewhy.celestial.utils.lunar.SiteData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestData {
    @Test
    public void testSiteData() throws IOException {
        SiteData siteData = new SiteData();
        JsonObject metadata = siteData.metadata();
        System.out.println("siteData.getPlayersInGame(metadata) = " + siteData.getPlayersInGame(metadata));
        System.out.println("siteData.getPlayersInLauncher(metadata) = " + siteData.getPlayersInLauncher(metadata));
    }
}
