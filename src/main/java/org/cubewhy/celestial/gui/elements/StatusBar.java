/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements;

import javax.swing.*;

public class StatusBar extends JLabel {
    private final Timer autoClearTimer = new Timer(10000, (e) -> {
        this.clear();
    });

    public void clear() {
        this.setText("");
    }

    @Override
    public void setText(String text) {
        if (!this.getText().isEmpty()) {
            autoClearTimer.stop();
        }
        super.setText(text);
        if (text != null && !text.isEmpty()) {
            autoClearTimer.start();
        }
    }
}
