/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.pages

import org.apache.commons.io.FileUtils
import org.cubewhy.celestial.*
import org.cubewhy.celestial.event.EventManager
import org.cubewhy.celestial.game.addon.LunarCNMod
import org.cubewhy.celestial.game.addon.WeaveMod
import org.cubewhy.celestial.gui.GuiLauncher
import org.cubewhy.celestial.gui.Language
import org.cubewhy.celestial.gui.dialogs.ArgsConfigDialog
import org.cubewhy.celestial.gui.dialogs.LunarQTDialog
import org.cubewhy.celestial.gui.dialogs.MirrorDialog
import org.cubewhy.celestial.gui.layouts.VerticalFlowLayout
import org.cubewhy.celestial.utils.*
import org.cubewhy.celestial.utils.OSEnum.Companion.current
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
        EventManager.register(this)
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
        panelFolders.add(createButtonOpenFolder(f.getString("gui.settings.folder.main"), configDir))
        panelFolders.add(createButtonOpenFolder(f.getString("gui.settings.folder.theme"), themesDir))
        panelFolders.add(createButtonOpenFolder(f.getString("gui.settings.folder.log"), launcherLogFile.parentFile))
        panelFolders.add(
            createButtonOpenFolder(
                f.getString("gui.settings.folder.game"),
                config.installationDir.toFile()
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

        val customJre: String = config.jre
        val btnSelectPath = JButton(if ((customJre.isEmpty())) currentJavaExec.path else customJre)
        val btnUnset = JButton(f.getString("gui.settings.jvm.jre.unset"))
        btnSelectPath.addActionListener { e: ActionEvent ->
            val file =
                chooseFile(if ((current == OSEnum.Windows)) FileNameExtensionFilter("Java Executable", "exe") else null)
            if (file != null) {
                val source = e.source as JButton
                GuiLauncher.statusBar.text = f.format("gui.settings.jvm.jre.success", file)
                config.jre = file.path
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
            ) return@addActionListener

            val java = currentJavaExec
            btnSelectPath.text = java.path
            config.jre = ""
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
        val ramSlider = JSlider(JSlider.HORIZONTAL, 0, totalMem, config.game.ram)
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
                config.game.ram = source.value
            }
            labelRam.text = decimalFormat.format((source.value.toFloat() / 1024f).toDouble()) + "GB"
        }
        p2.add(labelRam)
        panelVM.add(p2)

        val p3 = JPanel()
        p3.add(JLabel(f.getString("gui.settings.jvm.wrapper")))
        val wrapperInput = getAutoSaveTextField(config.game, "wrapper")
        p3.add(wrapperInput)
        val btnSetVMArgs = JButton(f.getString("gui.settings.jvm.args"))
        btnSetVMArgs.addActionListener {
            ArgsConfigDialog("vmArgs", config.game).isVisible = true
        }
        panelVM.add(btnSetVMArgs)
        panelVM.add(p3)

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
        p4.add(getAutoSaveTextField(config, "api"))

        panelLauncher.add(p4)
        // data sharing
        panelLauncher.add(
            getAutoSaveCheckBox(
                config,
                "dataSharing",
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
        p5.add(getAutoSaveComboBox(config, "theme", themes))
        val btnAddTheme = JButton(f.getString("gui.settings.launcher.theme.add"))
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
                config,
                "language",
                listOf(*Language.entries.toTypedArray())
            )
        )
        panelLauncher.add(p6)
        // cele wrap
        val panelCeleWrap = JPanel()
        panelCeleWrap.border = TitledBorder(
            null,
            f.getString("gui.settings.launcher.celewrap"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.ORANGE
        )
        panelCeleWrap.add(getAutoSaveCheckBox(config.celeWrap, "state", f.getString("gui.settings.launcher.celewrap.state")))
        panelCeleWrap.add(getAutoSaveCheckBox(config.celeWrap, "checkUpdate", f.getString("gui.settings.launcher.celewrap.update")))
        panelLauncher.add(panelCeleWrap)
        // max-threads
        val p7 = JPanel()
        p7.add(JLabel(f.getString("gui.settings.launcher.max-threads")))
        p7.add(getAutoSaveSpinner(config, "maxThreads", 1.0, 256.0, 1.0, true))
        panelLauncher.add(p7)
        // installation-dir
        val p8 = JPanel()
        p8.add(JLabel(f.getString("gui.settings.launcher.installation")))
        val btnSelectInstallation = JButton(config.installationDir)
        btnSelectInstallation.addActionListener { e: ActionEvent ->
            val file = chooseFolder()
            val source = e.source as JButton
            if (file == null) {
                return@addActionListener
            }
            config.installationDir = file.path
            log.info("Set installation-dir to $file")
            source.text = file.path
            GuiLauncher.statusBar.text = String.format(f.getString("gui.settings.installation.success"), file)
        }
        p8.add(btnSelectInstallation)
        panelLauncher.add(p8)
        // game-dir
        val p9 = JPanel()
        p9.add(JLabel(f.getString("gui.settings.launcher.game")))
        val btnSelectGameDir = JButton(config.game.gameDir)
        btnSelectGameDir.addActionListener { e: ActionEvent ->
            val file = chooseFolder()
            val source = e.source as JButton
            if (file == null) {
                return@addActionListener
            }
            config.game.gameDir = file.path
            log.info("Set game-dir to $file")
            source.text = file.path
            GuiLauncher.statusBar.text = String.format(f.getString("gui.settings.game-dir.success"), file)
        }
        p9.add(btnSelectGameDir)
        panelLauncher.add(p9)

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
        val btnLoaderUnset = JRadioButton(f.getString("gui.settings.addon.loader.unset"), isLoaderSelected(null))
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

        val panelLunarQT = JPanel()
        panelLunarQT.layout = VerticalFlowLayout()
        panelLunarQT.border = TitledBorder(
            null,
            f.getString("gui.settings.addons.lcqt"),
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            null,
            Color.ORANGE
        )
        panelLunarQT.add(
            f.getString("gui.settings.addons.lcqt.compatibility").toJLabel()
        )
        panelLunarQT.add(
            getAutoSaveCheckBox(
                config.addon.lcqt,
                "state",
                f.getString("gui.settings.addons.lcqt.toggle")
            )
        )
        panelLunarQT.add(
            getAutoSaveCheckBox(
                config.addon.lcqt,
                "checkUpdate",
                f.getString("gui.settings.addons.lcqt.check-update")
            )
        )
        val panelManageLunarQTInstallation = JPanel()
        panelManageLunarQTInstallation.add(f.getString("gui.settings.addons.lcqt.installation").toJLabel())
        val btnSelectLunarQTInstallation = createJButton(config.addon.lcqt.installationDir) { e: ActionEvent ->
            val file = chooseFile(FileNameExtensionFilter("LunarQT Agent (*.jar)", "jar"))
            val source = e.source as JButton
            if (file == null) return@createJButton
            config.addon.lcqt.installationDir = file.path
            log.info("Set lcqt-installation to $file")
            source.text = file.path
            GuiLauncher.statusBar.text = f.format("gui.settings.addons.lcqt.success", file)
        }
        panelManageLunarQTInstallation.add(btnSelectLunarQTInstallation)
        panelLunarQT.add(panelManageLunarQTInstallation)

        val btnManageLunarQT = JButton(f.getString("gui.settings.addons.lcqt.manage"))
        btnManageLunarQT.addActionListener {
            LunarQTDialog().isVisible = true
        }

        panelLunarQT.add(btnManageLunarQT)

        panelAddon.add(panelLunarQT)

        val p13 = JPanel()
        p13.add(
            getAutoSaveCheckBox(
                config.addon.weave,
                "checkUpdate",
                f.getString("gui.settings.addon.loader.weave.check-update")
            )
        )
        p13.add(
            getAutoSaveCheckBox(
                config.addon.lunarcn,
                "checkUpdate",
                f.getString("gui.settings.addon.loader.cn.check-update")
            )
        )
        panelAddon.add(p13)
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
        val btnProgramArgs = JButton(f.getString("gui.settings.game.args"))
        btnProgramArgs.addActionListener {
            ArgsConfigDialog(
                "args",
                config.game
            ).isVisible = true
        }
        p14.add(btnProgramArgs)
        panelGame.add(p14)
        // listener
//        val panelListener = JPanel()
//        panelListener.border = TitledBorder(
//            null,
//            f.getString("gui.settings.game.listener"),
//            TitledBorder.DEFAULT_JUSTIFICATION,
//            TitledBorder.DEFAULT_POSITION,
//            null,
//            Color.orange
//        )
//        val btnGroupListener = ButtonGroup()
//        val btnAttach = JRadioButton(f.getString("gui.settings.game.listener.attach"), config.connectMethod == BasicConfig.ConnectMethod.ATTACH)
//        val btnCmdLine = JRadioButton(f.getString("gui.settings.game.listener.cmdline"), config.connectMethod == BasicConfig.ConnectMethod.CMDLINE)
//        val btnDisable = JRadioButton("Disable", config.connectMethod == BasicConfig.ConnectMethod.DISABLE)
//        btnAttach.addActionListener {
//            config.connectMethod = BasicConfig.ConnectMethod.ATTACH
//        }
//        btnCmdLine.addActionListener {
//            config.connectMethod = BasicConfig.ConnectMethod.CMDLINE
//        }
//        btnDisable.addActionListener {
//            config.connectMethod = BasicConfig.ConnectMethod.DISABLE
//        }
//        btnGroupListener.add(btnAttach)
//        btnGroupListener.add(btnCmdLine)
//        btnGroupListener.add(btnDisable)
//        panelListener.add(btnAttach)
//        panelListener.add(btnCmdLine)
//        panelListener.add(btnDisable)
//        panelGame.add(panelListener)
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
        p16.add(getAutoSaveSpinner(config.game.resize, "width", 10.0, 5000.0, 1.0))
        p15.add(p16)
        val p17 = JPanel()
        p17.add(JLabel(f.getString("gui.settings.game.resize.height")))
        p17.add(getAutoSaveSpinner(config.game.resize, "height", 10.0, 5000.0, 1.0))
        p15.add(p17)
        panelGame.add(p15)

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

        p18.add(getAutoSaveTextField(config.proxy, "proxyAddress"))
        p18.add(getAutoSaveCheckBox(config.proxy, "state", f.getString("gui.settings.proxy.state")))

        val btnMirror = JButton(f.getString("gui.settings.proxy.mirror"))
        btnMirror.addActionListener {
            MirrorDialog().isVisible = true
        }

        panelProxy.add(btnMirror)
        panelProxy.add(p18)

        panel.add(panelVM)
        panel.add(panelLauncher)
        panel.add(panelGame)
        panel.add(panelAddon)

        panel.add(panelProxy)
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
        val addon = config.addon::class.java.getDeclaredMethod("get${getKotlinName(key)}")
            .invoke(config.addon) as AddonLoaderConfiguration
        addon.installationDir = file.path
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
        val addon = config.addon
        val weave = addon.weave
        val cn = addon.lunarcn
        weave.state = b1
        cn.state = b2
    }

    private fun isLoaderSelected(type: String?): Boolean {
        val addon = config.addon
        val weave = addon.weave
        val cn = addon.lunarcn
        val stateWeave = weave.state
        val stateCN = cn.state
        if (stateWeave && stateCN && type != null) {
            // correct it

            log.warn("Weave cannot load with LunarCN, auto corrected")
            weave.state = false
            cn.state = false

            return isLoaderSelected(null)
        }
        return when (type) {
            null -> !(stateWeave || stateCN)
            "weave" -> stateWeave
            "cn" -> stateCN
            else -> false
        }
    }

    private fun getAutoSaveComboBox(obj3ct: Any, key: String, items: List<*>): JComboBox<*> {
        val cb = JComboBox<Any>()
        var isString = false
        var isLanguage = false
        for (item in items) {
            if (item is Language) isLanguage = true
            if (item is String) isString = true
            cb.addItem(item)

        }
        val value = if (isLanguage) obj3ct.getKotlinField<Language>(key) else obj3ct.getKotlinField<String>(key)
        cb.selectedItem = value

        val finalIsLanguage = isLanguage
        cb.addActionListener { e: ActionEvent ->
            val source = e.source as JComboBox<*>
            if (source.selectedItem == null) {
                return@addActionListener
            }
            if (finalIsLanguage) obj3ct.setKotlinField(key, source.selectedItem as Language)
            else obj3ct.setKotlinField(key, source.selectedItem?.toString())


        }
        return cb
    }


    companion object {
        private val panel = JPanel()
        private val log: Logger = LoggerFactory.getLogger(GuiSettings::class.java)
        private fun getAutoSaveSpinner(
            obj3ct: Any,
            key: String,
            min: Double,
            max: Double,
            step: Double,
            forceInt: Boolean = false
        ): JSpinner {
            val value = obj3ct.getKotlinField<Double>(key)
            val spinner = JSpinner(SpinnerNumberModel(value, min, max, step))
            spinner.autoscrolls = true
            val editor = spinner.editor
            val textField = (editor as DefaultEditor).textField
            spinner.addChangeListener { e: ChangeEvent ->
                val source = e.source as JSpinner
                val v = if (forceInt) (source.value as Number).toInt() else source.value as Number
                obj3ct.setKotlinField(key, v)
            }
            textField.columns = 20
            return spinner
        }

        private fun getAutoSaveCheckBox(obj3ct: Any, key: String, text: String): JCheckBox {
            val cb = JCheckBox(text)
            cb.isSelected = obj3ct.getKotlinField(key)
            cb.addActionListener { e: ActionEvent ->
                val source = e.source as JCheckBox
                obj3ct.setKotlinField(key, source.isSelected)
            }
            return cb
        }

        private fun getAutoSaveTextField(obj3ct: Any, key: String): JTextField {
            val value = obj3ct.getKotlinField<String>(key)
            val input = JTextField(value)
            input.addActionListener { e: ActionEvent ->
                val source = e.source as JTextField
                // save value
                obj3ct.setKotlinField(key, source.text)
            }
            input.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent) {
                    val source = e.source as JTextField
                    // save value
                    obj3ct.setKotlinField(key, source.text)
                }
            })
            return input
        }
    }
}

