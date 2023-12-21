package org.cubewhy.celestial.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameArgs {
    int width;
    int height;
    File gameDir;
    String server = null;

    public GameArgs(int width, int height, File gameDir) {
        this.width = width;
        this.height = height;
        this.gameDir = gameDir;
    }
}
