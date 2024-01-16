/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

@AllArgsConstructor
@Getter
public class RemoteAddon {
    String name;
    URL downloadURL;
    Category category;

    public enum Category {
        AGENT,
        CN,
        WEAVE;


        /**
         * Parse plugin type from a string
         * */
        @Contract(pure = true)
        public static Category parse(@NotNull String category) {
            return switch (category) {
                case "cn" -> CN;
                case "weave", "Mod" -> WEAVE;
                case "Agent" -> AGENT;
                default -> null;
            };
        }
    }
}
