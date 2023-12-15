package org.cubewhy.celestial.gui.dialogs;

import javax.swing.*;

public class LanguageSelectDialog extends JDialog {
    public LanguageSelectDialog(JFrame owner) {
        super(owner);
        this.setTitle("Select language");
        this.setModalityType(ModalityType.APPLICATION_MODAL);
    }
}
