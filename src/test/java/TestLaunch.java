import org.cubewhy.celestial.Celestial;
import org.cubewhy.celestial.game.GameArgs;
import org.cubewhy.celestial.game.GameArgsResult;
import org.cubewhy.celestial.utils.lunar.LauncherData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestLaunch {
    @Test
    public void testGetArgs() throws IOException {
        GameArgs gameArgs = new GameArgs(540, 320, new File(System.getProperty("user.home"), ".cubewhy/lunarcn/minecraft"), new File(System.getProperty("user.home"), ".cubewhy/lunarcn/textures"));
        Celestial.launcherData = new LauncherData("https://api.lunarclientprod.com");
        GameArgsResult argsResult = Celestial.getArgs("1.8.9", "master", "lunar", new File(System.getProperty("user.home"), ".cubewhy/lunarcn/game"), gameArgs);
        List<String> args = argsResult.args();
        File natives = argsResult.natives();
        System.out.println("args = " + String.join(" ", args));
        System.out.println("natives = " + natives);
    }
}
