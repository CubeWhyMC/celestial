/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.Response
import org.cubewhy.celestial.game.AddonType
import org.cubewhy.celestial.gui.GuiLauncher
import java.awt.Component
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipFile
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingConstants

fun String.toURI(): URI = URI.create(this)

fun ResourceBundle.format(key: String, vararg args: Any?): String =
    this.getString(key).format(*args)

fun String.toURL(): URL =
    URL(this)


fun String.hasNonAscii(): Boolean {
    // todo make it high performance
    this.toCharArray().forEach {
        if (it.code !in 0..127) {
            return false
        }
    }
    return true
}

fun Component.withScroller(
    vsbPolicy: Int = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
    hsbPolicy: Int = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
) =
    JScrollPane(this, vsbPolicy, hsbPolicy).let {
        it.verticalScrollBar.unitIncrement = 30
        it
    }

val Response.json: JsonElement?
    get() {
        val json = this.body!!.string()
        return JsonParser.parseString(json)
    }

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
            out.createNewFile()
            val entryInputStream = this.getInputStream(entry)
            FileOutputStream(out).use { fileOutPutStream ->
                fileOutPutStream.write(entryInputStream.readAllBytes())
            }
        }
    }
}

/**
 * Is mod
 *
 * @param type type of the addon (WEAVE, LUNARCN only)
 * @return yes or no
 * */
fun JarFile.isMod(type: AddonType): Boolean {
    return when (type) {
        AddonType.LUNARCN -> this.getJarEntry("lunarcn.mod.json") != null
        AddonType.WEAVE -> this.getJarEntry("weave.mod.json") != null
        else -> throw IllegalStateException(type.name + " is not a type of Lunar mods!")
    }
}
