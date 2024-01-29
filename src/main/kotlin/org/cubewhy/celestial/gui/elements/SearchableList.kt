/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements

import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

class SearchableList<T>(private val model: DefaultListModel<T>, baseList: JList<T>) : JPanel() {

    private var list = ArrayList<T>()

    init {
        this.layout = BorderLayout(0, 0)
        model.elements().asIterator().forEach {
            list.add(it)
        }
        val searchBar = JTextField("Search")
        searchBar.foreground = Color.GRAY
        searchBar.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                val source = e.source<JTextField>()
                if (source.text == "Search") source.text = ""
                source.foreground = null
            }

            override fun focusLost(e: FocusEvent) {
                val source = e.source<JTextField>()
                if (source.text.isEmpty()) {
                    source.text = "Search"
                    source.foreground = Color.GRAY
                }
            }
        })

        searchBar.addActionListener {
            this.search(it.source<JTextField>().text)
        }
        this.add(searchBar, BorderLayout.NORTH)

        baseList.addPropertyChangeListener {
            // reload items
            list = ArrayList()
            model.elements().asIterator().forEach {
                list.add(it)
            }
        }

        this.add(
            JScrollPane(
                baseList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            )
        )
    }

    private fun search(text: String) {
        if (text.isEmpty()) {
            model.removeAllElements()
            model.addAll(list)
            return
        }
        model.removeAllElements()
        list.forEach {
            if (it.toString().contains(text, ignoreCase = true)) {
                model.addElement(it)
            }
        }
    }
}

fun <T : SwingConstants> AWTEvent.source(): T {
    return this.source as T
}
