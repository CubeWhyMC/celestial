/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.pages

import com.google.gson.JsonObject
import org.apache.commons.io.FileUtils
import org.cubewhy.celestial.Celestial.config
import org.cubewhy.celestial.Celestial.f
import org.cubewhy.celestial.Celestial.launcherLogFile
import org.cubewhy.celestial.Celestial.proxy
import org.cubewhy.celestial.Celestial.themesDir
import org.cubewhy.celestial.game.addon.LunarCNMod
import org.cubewhy.celestial.game.addon.WeaveMod
import org.cubewhy.celestial.gui.GuiLauncher
import org.cubewhy.celestial.gui.Language
import org.cubewhy.celestial.gui.dialogs.ArgsConfigDialog
import org.cubewhy.celestial.gui.dialogs.MirrorDialog
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.toJLabel
import org.cubewhy.celestial.utils.GuiUtils.chooseFile
import org.cubewhy.celestial.utils.GuiUtils.chooseFolder
import org.cubewhy.celestial.utils.GuiUtils.createButtonOpenFolder
import org.cubewhy.celestial.utils.GuiUtils.saveFile
import org.cubewhy.celestial.utils.OSEnum
import org.cubewhy.celestial.utils.OSEnum.Companion.current
import org.cubewhy.celestial.utils.SystemUtils.currentJavaExec
import org.cubewhy.celestial.utils.SystemUtils.totalMem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import javax.swing.*
import javax.swing.JSpinner.DefaultEditor
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.filechooser.FileNameExtensionFilter

class GuiSettings : JScrollPane(panel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED) {
    private val claimed: MutableSet<String> = HashSet()

    init {
        this.border = TitledBorder(
            null,
            f.getString("gui.settings.title"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        panel.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
        getVerticalScrollBar().unitIncrement = 30
        this.initGui()
    }

    private fun initGui() {
        // config
        panel.add(JLabel(f.getString("gui.settings.warn.restart")))
        val panelFolders = JPanel()
        panelFolders.add(createButtonOpenFolder(f.getString("gui.settings.folder.main"), config.file.parentFile))
        panelFolders.add(createButtonOpenFolder(f.getString("gui.settings.folder.theme"), themesDir))
        panelFolders.add(
            createButtonOpenFolder(
                f.getString("gui.settings.folder.log"),
                launcherLogFile.parentFile
            )
        )
        panel.add(panelFolders)
        // jre
        val panelVM = JPanel()
        panelVM.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
        panelVM.border = TitledBorder(
            null,
            f.getString("gui.settings.jvm"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )

        val customJre: String = config.getValue("jre").asString
        val btnSelectPath = JButton(if ((customJre.isEmpty())) currentJavaExec.path else customJre)
        val btnUnset: JButton = JButton(f.getString("gui.settings.jvm.jre.unset"))
        btnSelectPath.addActionListener { e: ActionEvent ->
            val file =
                chooseFile(if ((current == OSEnum.Windows)) FileNameExtensionFilter("Java Executable", "exe") else null)
            if (file != null) {
                val source = e.source as JButton
                GuiLauncher.statusBar.text = String.format(f.getString("gui.settings.jvm.jre.success"), file)
                config.setValue("jre", file.path)
                source.text = file.path
            }
        }
        btnUnset.addActionListener {
            if (JOptionPane.showConfirmDialog(
                    this,
                    f.getString("gui.settings.jvm.jre.unset.confirm"),
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.NO_OPTION
            ) {
                return@addActionListener
            }
            val java = currentJavaExec
            btnSelectPath.text = java.path
            config.setValue("jre", "")
            GuiLauncher.statusBar.text = f.getString("gui.settings.jvm.jre.unset.success")
        }
        // jre settings
        val p1 = JPanel()
        p1.add(JLabel(f.getString("gui.settings.jvm.jre")))
        p1.add(btnSelectPath)
        p1.add(btnUnset)
        panelVM.add(p1)
        // ram settings
        val p2 = JPanel()
        p2.add(JLabel(f.getString("gui.settings.jvm.ram")))
        val ramSlider = JSlider(JSlider.HORIZONTAL, 0, totalMem, config.getValue("ram").asInt)
        ramSlider.paintTicks = true
        ramSlider.majorTickSpacing = 1024 // 1G
        p2.add(ramSlider)
        val decimalFormat = DecimalFormat("#.##")

        val labelRam = JLabel(decimalFormat.format((ramSlider.value.toFloat() / 1024f).toDouble()) + "GB")
        ramSlider.addChangeListener { e: ChangeEvent ->
            val source = e.source as JSlider
            if (!source.valueIsAdjusting) {
                // save value
                log.info("Set ram -> " + source.value)
                config.setValue("ram", source.value)
            }
            labelRam.text = decimalFormat.format((source.value.toFloat() / 1024f).toDouble()) + "GB"
        }
        p2.add(labelRam)
        panelVM.add(p2)

        val p3 = JPanel()
        p3.add(JLabel(f.getString("gui.settings.jvm.wrapper")))
        val wrapperInput = getAutoSaveTextField(config.config, "wrapper")
        p3.add(wrapperInput)
        val btnSetVMArgs: JButton = JButton(f.getString("gui.settings.jvm.args"))
        btnSetVMArgs.addActionListener {
            ArgsConfigDialog("vm-args", config.config).isVisible = true
        }
        panelVM.add(btnSetVMArgs)
        panelVM.add(p3)

        claim("jre", panelVM)
        claim("ram")
        claim("vm-args")
        claim("wrapper")

        claim("game") // config in GuiVersionSelect
        claim("javaagents") // config in GuiAddonManager

        // config of the launcher
        val panelLauncher = JPanel()
        panelLauncher.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
        panelLauncher.border = TitledBorder(
            null,
            f.getString("gui.settings.launcher"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        // api
        val p4 = JPanel()
        p4.add(JLabel(f.getString("gui.settings.launcher.api")))
        p4.add(getAutoSaveTextField(config.config, "api"))

        panelLauncher.add(p4)
        // data sharing
        panelLauncher.add(
            getAutoSaveCheckBox(
                config.config,
                "data-sharing",
                f.getString("gui.settings.launcher.data-sharing")
            )
        )
        // theme
        val p5 = JPanel()
        p5.add(JLabel(f.getString("gui.settings.launcher.theme")))
        val themes: MutableList<String> = ArrayList()
        themes.add("dark")
        themes.add("light") // default themes
        themes.add("unset") // unset theme
        // custom themes
        for (file in Objects.requireNonNull<Array<File>>(themesDir.listFiles())) {
            if (file.isFile && file.name.endsWith(".json")) {
                themes.add(file.name)
            }
        }
        p5.add(getAutoSaveComboBox(config.config, "theme", themes))
        val btnAddTheme: JButton = JButton(f.getString("gui.settings.launcher.theme.add"))
        btnAddTheme.addActionListener {
            val file = chooseFile(FileNameExtensionFilter("Intellij IDEA theme (.json)", "json"))
                ?: return@addActionListener
            // copy
            val f1: File = File(themesDir, file.name)
            if (f1.exists()) {
                JOptionPane.showMessageDialog(
                    this,
                    f.getString("gui.settings.launcher.theme.exist"),
                    "File always exist",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }
            try {
                FileUtils.copyFile(file, f1)
                GuiLauncher.statusBar.text = "gui.settings.launcher.theme.success"
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
        }
        p5.add(btnAddTheme)
        panelLauncher.add(p5)
        // language
        val p6 = JPanel()
        p6.add(JLabel(f.getString("gui.settings.launcher.language")))
        p6.add(
            getAutoSaveComboBox(
                config.config,
                "language",
                listOf(*Language.entries.toTypedArray())
            )
        )
        panelLauncher.add(p6)
        // max-threads
        val p7 = JPanel()
        p7.add(JLabel(f.getString("gui.settings.launcher.max-threads")))
        p7.add(getAutoSaveSpinner(config.config, "max-threads", 1.0, 256.0, 1.0))
        panelLauncher.add(p7)
        // installation-dir
        val p8 = JPanel()
        p8.add(JLabel(f.getString("gui.settings.launcher.installation")))
        val btnSelectInstallation: JButton = JButton(config.getValue("installation-dir").asString)
        btnSelectInstallation.addActionListener { e: ActionEvent ->
            val file = chooseFolder()
            val source = e.source as JButton
            if (file == null) {
                return@addActionListener
            }
            config.setValue("installation-dir", file.path)
            log.info("Set installation-dir to $file")
            source.text = file.path
            GuiLauncher.statusBar.text = String.format(f.getString("gui.settings.installation.success"), file)
        }
        p8.add(btnSelectInstallation)
        panelLauncher.add(p8)
        // game-dir
        val p9 = JPanel()
        p9.add(JLabel(f.getString("gui.settings.launcher.game")))
        val btnSelectGameDir: JButton = JButton(config.getValue("game-dir").asString)
        btnSelectGameDir.addActionListener { e: ActionEvent ->
            val file = chooseFolder()
            val source = e.source as JButton
            if (file == null) {
                return@addActionListener
            }
            config.setValue("game-dir", file.path)
            log.info("Set game-dir to $file")
            source.text = file.path
            GuiLauncher.statusBar.text = String.format(f.getString("gui.settings.game-dir.success"), file)
        }
        p9.add(btnSelectGameDir)
        panelLauncher.add(p9)

        claim("data-sharing", panelLauncher)
        claim("theme")
        claim("language")
        claim("max-threads")
        claim("api")
        claim("installation-dir")
        claim("game-dir")

        // addon
        val panelAddon = JPanel()
        panelAddon.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
        panelAddon.border = TitledBorder(
            null,
            f.getString("gui.settings.addon"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        val p10 = JPanel()
        val btnGroup = ButtonGroup()
        val btnLoaderUnset: JRadioButton =
            JRadioButton(f.getString("gui.settings.addon.loader.unset"), isLoaderSelected(null))
        btnGroup.add(btnLoaderUnset)
        val btnWeave = JRadioButton("Weave", isLoaderSelected("weave"))
        btnGroup.add(btnWeave)
        val btnLunarCN = JRadioButton("LunarCN", isLoaderSelected("cn"))
        btnGroup.add(btnLunarCN)
        btnLoaderUnset.addActionListener { toggleLoader(null) }
        btnWeave.addActionListener { toggleLoader("weave") }
        btnLunarCN.addActionListener { toggleLoader("cn") }
        p10.add(btnLoaderUnset)
        p10.add(btnWeave)
        p10.add(btnLunarCN)
        panelAddon.add(p10)
        // installation (loader)
        val p11 = JPanel()
        // lunarcn
        val btnSelectLunarCNInstallation =
            getSelectInstallationButton(LunarCNMod.installation, "LunarCN Loader", "lunarcn")
        p11.add(JLabel(f.getString("gui.settings.addon.loader.cn.installation")))
        p11.add(btnSelectLunarCNInstallation)
        panelAddon.add(p11)
        val p12 = JPanel()
        val btnSelectWeaveInstallation = getSelectInstallationButton(WeaveMod.installation, "Weave Loader", "weave")
        p12.add(JLabel(f.getString("gui.settings.addon.loader.weave.installation")))
        p12.add(btnSelectWeaveInstallation)
        panelAddon.add(p12)

        val p13 = JPanel()
        p13.add(
            getAutoSaveCheckBox(
                config.config.getAsJsonObject("addon").getAsJsonObject("weave"),
                "check-update",
                f.getString("gui.settings.addon.loader.weave.check-update")
            )
        )
        p13.add(
            getAutoSaveCheckBox(
                config.config.getAsJsonObject("addon").getAsJsonObject("lunarcn"),
                "check-update",
                f.getString("gui.settings.addon.loader.cn.check-update")
            )
        )
        panelAddon.add(p13)

        claim("addon", panelAddon)

        // game settings
        val panelGame = JPanel()
        panelGame.border = TitledBorder(
            null,
            f.getString("gui.settings.game"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        panelGame.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)

        // program args
        val p14 = JPanel()
        val btnProgramArgs: JButton = JButton(f.getString("gui.settings.game.args"))
        btnProgramArgs.addActionListener {
            ArgsConfigDialog(
                "program-args",
                config.config
            ).isVisible = true
        }
        p14.add(btnProgramArgs)
        panelGame.add(p14)
        // resize
        val p15 = JPanel()
        p15.border = TitledBorder(
            null,
            f.getString("gui.settings.game.resize"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        p15.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
        val p16 = JPanel()
        p16.add(JLabel(f.getString("gui.settings.game.resize.width")))
        p16.add(getAutoSaveTextField(config.getValue("resize").asJsonObject, "width"))
        p15.add(p16)
        val p17 = JPanel()
        p17.add(JLabel(f.getString("gui.settings.game.resize.height")))
        p17.add(getAutoSaveTextField(config.getValue("resize").asJsonObject, "height"))
        p15.add(p17)
        panelGame.add(p15)

        claim("program-args", panelGame)
        claim("resize")

        val panelProxy = JPanel()
        panelProxy.border = TitledBorder(
            null,
            f.getString("gui.settings.proxy"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.orange
        )
        panelProxy.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)

        val p18 = JPanel()
        p18.add(JLabel(f.getString("gui.settings.proxy.address")))

        p18.add(getAutoSaveTextField(proxy.config, "proxy"))
        p18.add(getAutoSaveCheckBox(proxy.config, "state", f.getString("gui.settings.proxy.state")))

        val btnMirror: JButton = JButton(f.getString("gui.settings.proxy.mirror"))
        btnMirror.addActionListener {
            MirrorDialog().isVisible = true
        }

        panelProxy.add(btnMirror)
        panelProxy.add(p18)

        panel.add(panelProxy)

        if (config.config.keySet().size != claimed.size) {
            val panelUnclaimed = JPanel()
            panelUnclaimed.border = TitledBorder(
                null,
                f.getString("gui.settings.unclaimed"),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                null,
                Color.orange
            )
            panelUnclaimed.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
            addUnclaimed(panelUnclaimed, config.config)
            panel.add(panelUnclaimed)
        }
    }

    private fun getSelectInstallationButton(installation: File, name: String, type: String): JButton {
        val btnSelectLunarCNInstallation = JButton(installation.path)
        btnSelectLunarCNInstallation.addActionListener { e: ActionEvent ->
            val file = saveFile(FileNameExtensionFilter(name, "jar")) ?: return@addActionListener
            val source = e.source as JButton
            source.text = file.path
            setModLoaderInstallation(type, file)
        }
        return btnSelectLunarCNInstallation
    }

    private fun setModLoaderInstallation(key: String, file: File) {
        config.getValue("addon").asJsonObject.getAsJsonObject(key).addProperty("installation", file.path)
    }

    /**
     * Toggle loader
     *
     * @param type null, cn, weave
     */
    private fun toggleLoader(type: String?) {
        var b1 = false // weave
        var b2 = false // lccn
        if (type != null) {
            if (type == "cn") {
                b2 = true
            } else if (type == "weave") {
                b1 = true
            }
        }
        val addon: JsonObject = config.getValue("addon").asJsonObject
        val weave = addon["weave"].asJsonObject
        val cn = addon["lunarcn"].asJsonObject
        weave.addProperty("enable", b1)
        cn.addProperty("enable", b2)
    }

    private fun isLoaderSelected(type: String?): Boolean {
        val addon: JsonObject = config.getValue("addon").asJsonObject
        val weave = addon["weave"].asJsonObject
        val cn = addon["lunarcn"].asJsonObject
        val stateWeave = weave["enable"].asBoolean
        val stateCN = cn["enable"].asBoolean
        if (stateWeave && stateCN && type != null) {
            // correct it

            log.warn("Weave cannot load with LunarCN, auto corrected")
            weave.addProperty("enable", false)
            cn.addProperty("enable", false)

            return isLoaderSelected(null)
        }
        if (type == null) {
            return !(stateWeave || stateCN)
        } else if (type == "weave") {
            return stateWeave
        } else if (type == "cn") {
            return stateCN
        }
        return false
    }

    private fun addUnclaimed(basePanel: JPanel, json: JsonObject) {
        for ((key, value) in json.entrySet()) {
            if (!claimed.contains(key)) {
                // unclaimed
                if (value.isJsonPrimitive) {
                    val p = getSimplePanel(json, key)
                    basePanel.add(p)
                } else if (value.isJsonObject) {
                    val subPanel = JPanel()
                    subPanel.border = TitledBorder(
                        null,
                        key,
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        null,
                        Color.orange
                    )
                    subPanel.layout = VerticalFlowLayout(VerticalFlowLayout.LEFT)
                    basePanel.add(subPanel)
                    addUnclaimed(subPanel, value.asJsonObject)
                } else if (value.isJsonArray) {
                    val btnShowList = JButton(key)
                    btnShowList.addActionListener { ArgsConfigDialog(key, json).isVisible = true }
                    basePanel.add(btnShowList)
                } else if (value.isJsonNull) {
                    basePanel.add("$key: null".toJLabel())
                }
            }
        }
    }

    private fun getAutoSaveComboBox(json: JsonObject, key: String, items: List<*>): JComboBox<*> {
        val cb = JComboBox<Any>()
        var isString = false
        var isLanguage = false
        for (item in items) {
            if (item is Language) {
                isLanguage = true
            }
            cb.addItem(item)
            if (item is String) {
                isString = true
            }
        }
        if (isString) {
            cb.setSelectedItem(json[key].asString)
        } else if (isLanguage) {
            cb.selectedItem = Language.findByCode(json[key].asString)
        }
        val finalIsLanguage = isLanguage
        cb.addActionListener { e: ActionEvent ->
            val source = e.source as JComboBox<*>
            if (source.selectedItem == null) {
                return@addActionListener
            }
            val v = if (finalIsLanguage) {
                (source.selectedItem as Language).code
            } else {
                source.selectedItem?.toString()
            }
            json.addProperty(key, v)
        }
        return cb
    }

    private fun getSimplePanel(json: JsonObject, key: String): JPanel {
        val panel = JPanel()
        val value = json.getAsJsonPrimitive(key)
        if (value.isBoolean) {
            val cb = getAutoSaveCheckBox(json, key, key)
            panel.add(cb)
        } else if (value.isString) {
            panel.add(JLabel(key))
            val input = getAutoSaveTextField(json, key)
            panel.add(input)
        } else if (value.isNumber) {
            panel.add(JLabel(key))
            val spinner = getAutoSaveSpinner(json, key, Double.MIN_VALUE, Double.MAX_VALUE, 0.01)
            panel.add(spinner)
        }
        return panel
    }

    /**
     * Mark a key as claimed and add the panel
     *
     * @param key      key in celestial.json
     * @param cfgPanel a panel to config this value
     */
    private fun claim(key: String, cfgPanel: JPanel) {
        claim(key)
        panel.add(cfgPanel) // add the panel
    }

    private fun claim(key: String) {
        if (!claimed.add(key)) {
            log.warn("Failed to claim $key : always claimed.")
        }
    }

    companion object {
        private val panel = JPanel()
        private val log: Logger = LoggerFactory.getLogger(GuiSettings::class.java)
        private fun getAutoSaveSpinner(
            json: JsonObject,
            key: String,
            min: Double,
            max: Double,
            step: Double
        ): JSpinner {
            val value = json.getAsJsonPrimitive(key)
            val spinner = JSpinner(SpinnerNumberModel(value.asDouble, min, max, step))
            spinner.autoscrolls = true
            val editor = spinner.editor
            val textField = (editor as DefaultEditor).textField
            spinner.addChangeListener { e: ChangeEvent ->
                val source = e.source as JSpinner
                val v = source.value as Number
                json.addProperty(key, v)
            }
            textField.columns = 20
            return spinner
        }

        private fun getAutoSaveCheckBox(json: JsonObject, key: String, text: String): JCheckBox {
            val cb = JCheckBox(text)
            val value = json.getAsJsonPrimitive(key)
            cb.isSelected = value.asBoolean
            cb.addActionListener { e: ActionEvent ->
                val source = e.source as JCheckBox
                json.addProperty(key, source.isSelected)
            }
            return cb
        }

        private fun getAutoSaveTextField(json: JsonObject, key: String): JTextField {
            val value = json.getAsJsonPrimitive(key)
            val input = JTextField(value.asString)
            input.addActionListener { e: ActionEvent ->
                val source = e.source as JTextField
                // save value
                json.addProperty(key, source.text)
            }
            input.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent) {
                    val source = e.source as JTextField
                    // save value
                    json.addProperty(key, source.text)
                }
            })
            return input
        }
    }
}
