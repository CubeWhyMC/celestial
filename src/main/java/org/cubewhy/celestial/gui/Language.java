/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui;

import lombok.Getter;

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


    @Override
    public String toString() {
        return this.code;
    }
}
