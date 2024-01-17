/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.management.loading.MLetContent;

@Getter
public enum Language {
    ENGLISH("English", "en"),
    CHINESE("简体中文", "zh"),
    JAPANESE("日本語", "ja"),
    KOREAN("한국인", "ko");

    private final String view;
    private final String code;

    Language(String view, String code) {
        this.view = view;
        this.code = code;
    }

    public static @Nullable Language findByCode(String code) {
        for (Language value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static @Nullable Language findByView(String view) {
        for (Language value : values()) {
            if (value.view.equals(view)) {
                return value;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return this.view + "/" + this.code;
    }
}
