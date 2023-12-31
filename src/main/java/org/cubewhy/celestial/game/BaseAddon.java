/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.game;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface BaseAddon {
    @Contract(pure = true)
    static BaseAddon @Nullable [] findAll() {
        return null;
    }
}
