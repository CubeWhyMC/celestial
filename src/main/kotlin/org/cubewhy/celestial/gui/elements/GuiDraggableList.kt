/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.elements

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.dnd.*
import javax.swing.DefaultListModel
import javax.swing.DropMode
import javax.swing.JList
import javax.swing.TransferHandler

// http://www.java2s.com/Tutorial/Java/0240__Swing/Usedraganddroptoreorderalist.htm
class GuiDraggableList<T> : JList<T>(DefaultListModel()) {

    init {
        dragEnabled = true
        dropMode = DropMode.INSERT
        transferHandler = ListDropHandler(this)
        GuiDragListener(this)
    }
}

internal class GuiDragListener<T>(var list: GuiDraggableList<T>) : DragSourceListener, DragGestureListener {
    private var ds: DragSource = DragSource()

    init {
        val dgr = ds.createDefaultDragGestureRecognizer(
            list,
            DnDConstants.ACTION_MOVE, this
        )
    }

    override fun dragGestureRecognized(dge: DragGestureEvent) {
        val transferable = StringSelection(list.selectedIndex.toString())
        ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this)
    }

    override fun dragEnter(dsde: DragSourceDragEvent) {
    }

    override fun dragExit(dse: DragSourceEvent) {
    }

    override fun dragOver(dsde: DragSourceDragEvent) {
    }

    override fun dragDropEnd(dsde: DragSourceDropEvent) {
        // do nothing
    }

    override fun dropActionChanged(dsde: DragSourceDragEvent) {
    }
}

internal class ListDropHandler<T>(var list: GuiDraggableList<T>) : TransferHandler() {
    override fun canImport(support: TransferSupport): Boolean {
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false
        }
        val dl = support.dropLocation as JList.DropLocation
        return dl.index != -1
    }

    override fun importData(support: TransferSupport): Boolean {
        if (!canImport(support)) {
            return false
        }

        val transferable = support.transferable
        val indexString: String
        try {
            indexString = transferable.getTransferData(DataFlavor.stringFlavor) as String
        } catch (e: Exception) {
            return false
        }

        val index = indexString.toInt()
        val dl = support.dropLocation as JList.DropLocation
        val dropTargetIndex = dl.index
        val jList = support.component as JList<T>
        val model = jList.model as DefaultListModel<T>

        val element = model[index]
        model.remove(index)
        model.insertElementAt(element, dropTargetIndex)
        return true
    }
}