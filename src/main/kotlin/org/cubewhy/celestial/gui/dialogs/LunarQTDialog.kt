/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.dialogs

import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.game.thirdparty.LunarQT
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.source
import org.cubewhy.celestial.toJLabel
import org.cubewhy.celestial.withScroller
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.ChangeEvent

class LunarQTDialog : JDialog() {
    private val log: Logger = LoggerFactory.getLogger(LunarQTDialog::class.java)
    private val panel = JPanel()

    init {
        this.title = f.getString("gui.settings.addons.lcqt.manage")
        this.isLocationByPlatform = true
        this.setSize(600, 350)
        this.modalityType = ModalityType.APPLICATION_MODAL
        this.contentPane = panel.withScroller()

        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                log.info("Dumping LunarQT configs...")
                LunarQT.saveConfig()
                this@LunarQTDialog.dispose()
            }
        })

        panel.layout = VerticalFlowLayout()

        this.initGui()
    }

    private fun initGui() {
        panel.run {
            add(getCheckBoxToggleModule("cosmetics", f.getString("gui.settings.addons.lcqt.module.cosmetics")))
            add(getCheckBoxToggleModule("freelook", f.getString("gui.settings.addons.lcqt.module.freelook")))
            add(getCheckBoxToggleModule("cracked", f.getString("gui.settings.addons.lcqt.module.cracked")))
            add(getCheckBoxToggleModule("noHitDelay", f.getString("gui.settings.addons.lcqt.module.noHitDelay")))
            add(getCheckBoxToggleModule("debugMods", f.getString("gui.settings.addons.lcqt.module.debugMods")))

            add(getCheckBoxToggleModule("fpsSpoof", f.getString("gui.settings.addons.lcqt.module.fpsSpoof")))
            add(getFloatInput("fpsSpoofMultiplier", f.getString("gui.settings.addons.lcqt.module.fpsSpoofMultiplier")))

            add(getCheckBoxToggleModule("rawInput", f.getString("gui.settings.addons.lcqt.module.rawInput")))
            add(getCheckBoxToggleModule("packFix", f.getString("gui.settings.addons.lcqt.module.packfix")))
        }
    }

    private fun getFloatInput(name: String, text: String) : JPanel {
        val methodGet = LunarQT.configLunarQT::class.java.getDeclaredMethod("get${getKotlinName(name)}")
        val methodSet = LunarQT.configLunarQT::class.java.getDeclaredMethod("set${getKotlinName(name)}", Float::class.java)
        val spinner = JSpinner(SpinnerNumberModel(methodGet.invoke(LunarQT.configLunarQT) as Float, 0f, Float.MAX_VALUE, 0.01f))
        spinner.autoscrolls = true
        val editor = spinner.editor
        val textField = (editor as JSpinner.DefaultEditor).textField
        spinner.addChangeListener { e: ChangeEvent ->
            val source = e.source as JSpinner
            val v = source.value as Float
            methodSet.invoke(LunarQT.configLunarQT, v)
        }
        textField.columns = 20

        val panelSp = JPanel().apply {
            add(text.toJLabel())
            add(spinner)
        }

        return panelSp
    }

    private fun getCheckBoxToggleModule(moduleName: String, text: String): JCheckBox {
        val cb = JCheckBox(text)
        val methodGet = LunarQT.configLunarQT::class.java.getDeclaredMethod("get${getKotlinName(moduleName)}Enabled")
        cb.isSelected = methodGet.invoke(LunarQT.configLunarQT) as Boolean
        cb.addActionListener {
            val source = it.source<JCheckBox>()
            source.isSelected = toggle(moduleName)
        }
        return cb
    }

    private fun toggle(moduleName: String): Boolean {
        // kotlin set/get
        val methodSet = LunarQT.configLunarQT::class.java.getDeclaredMethod("set${getKotlinName(moduleName)}Enabled", Boolean::class.java)
        val methodGet = LunarQT.configLunarQT::class.java.getDeclaredMethod("get${getKotlinName(moduleName)}Enabled")
        val willSetTo = !(methodGet.invoke(LunarQT.configLunarQT) as Boolean)
        methodSet.isAccessible = true
        methodSet.invoke(LunarQT.configLunarQT, willSetTo)
        return willSetTo
    }
}

fun getKotlinName(name: String): String {
    val case = name[0].uppercase()
    val exceptCase = name.substring(1)
    return case + exceptCase
}
