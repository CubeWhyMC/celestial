/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial

import okhttp3.Response
import org.cubewhy.celestial.game.AddonType
import org.cubewhy.celestial.gui.GuiLauncher
import org.cubewhy.celestial.utils.OSEnum
import java.awt.Component
import java.awt.Desktop
import java.awt.event.ActionListener
import java.io.*
import java.net.URI
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipFile
import javax.swing.*


fun String.toURI(): URI = URI.create(this)

fun URI.open() {
    try {
        Desktop.getDesktop().browse(this)
    } catch (e: UnsupportedOperationException) {
        // open with native methods
        when (OSEnum.current) {
            OSEnum.Linux -> Runtime.getRuntime().exec("xdg-open $this")
            OSEnum.MacOS, OSEnum.MacOSX, OSEnum.Darwin -> Runtime.getRuntime().exec("open $this")
            OSEnum.Windows -> Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler $this")
            else -> throw RuntimeException(e) // really unsupported
        }
    }
}

fun ResourceBundle.format(key: String, vararg args: Any?): String =
    this.getString(key).format(*args)

fun Component.withScroller(
    vsbPolicy: Int = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
    hsbPolicy: Int = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
) =
    JScrollPane(this, vsbPolicy, hsbPolicy).let {
        it.verticalScrollBar.unitIncrement = 30
        it
    }

val Response.string: String?
    get() = this.body?.string()

fun <T : SwingConstants> EventObject.source(): T {
    return this.source as T
}

fun JTextArea.readOnly(): JTextArea {
    this.isEditable = false
    return this
}

fun <T> Array<T>.forEachIsEnd(action: (T, Boolean) -> Unit) {
    this.forEachIndexed { index, t -> action(t, index == this.size - 1) }
}

fun String.toJLabel(): JLabel =
    // todo multi line support
    JLabel(this)

fun String.toJButton(func: ActionListener) =
    JButton(this).apply {
        addActionListener(func)
    }

fun String.toFile(): File = File(this)

fun String.toJTextArea(): JTextArea = JTextArea(this)

fun String.getInputStream(): InputStream? = GuiLauncher::class.java.getResourceAsStream(this)

fun File.toJar(): JarFile = JarFile(this)
fun File.toZip(): ZipFile = ZipFile(this)

fun ZipFile.unzip(targetDir: File) {
    for (entry in this.entries()) {
        val out = File(targetDir, entry!!.name)
        if (entry.isDirectory) {
            out.mkdirs()
        } else {
            out.parentFile.mkdirs()
            out.createNewFile()
            val entryInputStream = this.getInputStream(entry)
            FileOutputStream(out).use { fileOutPutStream ->
                fileOutPutStream.write(entryInputStream.readAllBytes())
            }
        }
    }
}

fun getKotlinName(name: String): String {
    val case = name[0].uppercase()
    val exceptCase = name.substring(1)
    return case + exceptCase
}

fun <T> Any.getKotlinField(name: String): T =
    this::class.java.getDeclaredMethod("get${getKotlinName(name)}").let {
        it.isAccessible = true
        it.invoke(this) as T
    }

inline fun <reified T> Any.setKotlinField(name: String, value: T?) {
    // Fuck Kotlin
    val clazz = when (value) {
        is Boolean -> Boolean::class.java
        is Int -> Int::class.java
        is Short -> Short::class.java
        is Double -> Double::class.java
        is Long -> Long::class.java
        is Char -> Char::class.java
        is Float -> Float::class.java
        else -> T::class.java // not built-in types
    }
    this::class.java.getDeclaredMethod("set${getKotlinName(name)}", clazz).apply {
        isAccessible = true
        invoke(this@setKotlinField, value)
    }
}

/**
 * Is mod
 *
 * @param type type of the addon (WEAVE, LUNARCN only)
 * @return yes or no
 * */
fun JarFile.isMod(type: AddonType): Boolean =
    when (type) {
        AddonType.LUNARCN -> this.getJarEntry("lunarcn.mod.json") != null
        AddonType.WEAVE -> this.getJarEntry("weave.mod.json") != null
        else -> throw IllegalStateException(type.name + " is not a type of Lunar mods!")
    }

fun File.isZipFile(): Boolean {
    try {
        FileInputStream(this).use { fis ->
            val header = ByteArray(4)
            if (fis.read(header) != 4) {
                return false
            }
            return isZipSignature(header)
        }
    } catch (e: IOException) {
        return false
    }
}

private fun isZipSignature(header: ByteArray): Boolean {
    return header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
}
