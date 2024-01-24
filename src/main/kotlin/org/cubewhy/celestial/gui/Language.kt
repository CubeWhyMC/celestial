/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui

enum class Language(val view: String, val code: String) {
    ENGLISH("English", "en"),
    CHINESE("简体中文", "zh"),
    JAPANESE("日本語", "ja"),
    KOREAN("한국인", "ko");

    override fun toString(): String {
        return this.view + "/" + this.code
    }

    companion object {
        fun findByCode(code: String): Language? {
            for (value in entries) {
                if (value.code == code) {
                    return value
                }
            }
            return null
        }

        fun findByView(view: String): Language? {
            for (value in entries) {
                if (value.view == view) {
                    return value
                }
            }
            return null
        }
    }
}
