/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.pages

import org.cubewhy.celestial.Celestial.config
import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.readOnly
import org.cubewhy.celestial.toJTextArea
import org.cubewhy.celestial.utils.GitUtils.branch
import org.cubewhy.celestial.utils.GitUtils.buildUser
import org.cubewhy.celestial.utils.GitUtils.buildUserEmail
import org.cubewhy.celestial.utils.GitUtils.buildVersion
import org.cubewhy.celestial.utils.GitUtils.commitMessage
import org.cubewhy.celestial.utils.GitUtils.commitTime
import org.cubewhy.celestial.utils.GitUtils.getCommitId
import org.cubewhy.celestial.utils.GitUtils.remote
import java.awt.Color
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.border.TitledBorder


class GuiAbout : JPanel() {
    init {
        this.name = "about"
        this.border = TitledBorder(
            null,
            f.getString("gui.about.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        val env = String.format(
            """
                
                Celestial v%s (Running on Java %s)
                Data sharing state: %s
                -----
                Git build info:
                    Build user: %s
                    Email: %s
                    Remote (%s): %s
                    Commit time: %s
                    Commit: %s
                    Commit Message: %s
                
                """.trimIndent(),
            buildVersion,
            System.getProperty("java.version"),
            if (config.getValue("data-sharing").asBoolean) "turn on" else "turn off",
            buildUser,
            buildUserEmail,
            branch,
            remote,
            commitTime,
            getCommitId(true),
            commitMessage
        )

        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val textArea = (f.getString("gui.about") + "\n" + env).toJTextArea().readOnly()
        this.add(textArea)
    }
}
