/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.gui.layouts

import java.awt.*
import java.io.Serial
import java.io.Serializable
import java.util.*
import kotlin.math.max

@Suppress("unused")
class VerticalFlowLayout(// 每一列中各组件水平向对齐方式(注意非每一列在容器中的水平向对齐方式, 因为每一列在容器中的水平对齐方式应当由 容器的 componentOrientation 属性 ltr/rtl 来指定)
    private var hAlign: Int = LEFT, // 每一列在容器中的垂直向对齐方式(注意无每一列在容器中的水平向对齐方式)
    private var vAlign: Int = TOP, // 水平向边框与组件之间的间隙
    private var hPadding: Int = 5, // 垂直向边框与组件之间的间隙, TOP:顶边距, BOTTOM:底边距
    private var vPadding: Int = 5, // 水平向组件之间的间隙
    private var hGap: Int = 5, // 垂直向组件之间的间隙
    var vGap: Int = 5, // 水平向组件是否填满逻辑列的宽度
    private val fill: Boolean = true, // 是否折列, true:折列, false:固定一列
    private val wrap: Boolean = false
) : LayoutManager, Serializable {
    constructor(padding: Int, gap: Int) : this(LEFT, TOP, padding, padding, gap, gap, true, false)

//    constructor(padding: Int) : this(LEFT, TOP, padding, padding, 5, 5, true, false)

    override fun addLayoutComponent(name: String, comp: Component) {
    }

    override fun removeLayoutComponent(comp: Component) {
    }

    /**
     * 最合适的尺寸, 一列放下全部组件
     */
    override fun preferredLayoutSize(container: Container): Dimension {
        synchronized(container.treeLock) {
            var width = 0
            var height = 0

            // 可见组件的最大宽和累计高
            val components = getVisibleComponents(container)
            for (component in components) {
                val dimension = component.preferredSize
                width = max(width.toDouble(), dimension.width.toDouble()).toInt()
                height += dimension.height
            }

            // 累计高添加组件间间隙
            if (components.isNotEmpty()) {
                height += vGap * (components.size - 1)
            }

            // 累计宽高添加边框宽高
            val insets = container.insets
            width += insets.left + insets.right
            height += insets.top + insets.bottom

            // 有组件的话, 累计宽高添加边框与组件的间隙和
            if (components.isNotEmpty()) {
                width += hPadding * 2
                height += vPadding * 2
            }
            return Dimension(width, height)
        }
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            var width = 0
            var height = 0

            // 可见组件的最大宽和累计高
            val components = getVisibleComponents(parent)
            for (component in components) {
                val dimension = component.minimumSize
                width = max(width.toDouble(), dimension.width.toDouble()).toInt()
                height += dimension.height
            }

            // 累计高添加组件间间隙
            if (components.isNotEmpty()) {
                height += vGap * (components.size - 1)
            }

            // 累计宽高添加边框宽高
            val insets = parent.insets
            width += insets.left + insets.right
            height += insets.top + insets.bottom

            // 有组件的话, 累计宽高添加边框与组件的间隙和
            if (components.isNotEmpty()) {
                width += hPadding * 2
                height += vPadding * 2
            }
            return Dimension(width, height)
        }
    }

    override fun layoutContainer(container: Container) {
        synchronized(container.treeLock) {
            // 容器实际宽高
            val size = container.size
            // 容器实际边框
            val insets = container.insets

            // 容器内可供组件使用的空间大小(排除边框和内边距)
            val availableWidth = size.width - insets.left - insets.right - hPadding * 2
            val availableHeight = size.height - insets.top - insets.bottom - vPadding * 2

            // 容器定义的组件方向, 这里先不管, 默认从左往右

            // 容器内所有可见组件
            val components = getVisibleComponents(container)

            // x基点
            var xBase = insets.left + hPadding

            // 缓存当前列中的所有组件
            val list: MutableList<Component> = LinkedList()

            for (component in components) {
                list.add(component)

                // 预算判断
                // 换列标准: 允许换列 且 该列组件数>1 且 该列累积高>容器可用高+vPadding
                // 累积高: 算上当前组件后, 当前列中的组件的累加高度(组件高度+组件间隙)
                if (wrap && list.size > 1 && availableHeight + vPadding < getPreferredHeight(list)) {
                    // 如果需要换行, 则当前列中得移除当前组件

                    list.remove(component)

                    batch(insets, availableWidth, availableHeight, xBase, list, components)

                    xBase += hGap + getPreferredWidth(list)

                    // 需要换列, 清空上一列中的所有组件
                    list.clear()

                    list.add(component)
                }
            }
            if (list.isNotEmpty()) {
                batch(insets, availableWidth, availableHeight, xBase, list, components)
            }
        }
    }

    private fun batch(
        insets: Insets,
        availableWidth: Int,
        availableHeight: Int,
        xBase: Int,
        list: List<Component>,
        components: List<Component>
    ) {
        val preferredWidth = getPreferredWidth(list)
        val preferredHeight = getPreferredHeight(list)

        // y
        var y = when (vAlign) {
            TOP -> {
                insets.top + vPadding
            }
            CENTER -> {
                (availableHeight - preferredHeight) / 2 + insets.top + vPadding
            }
            BOTTOM -> {
                availableHeight - preferredHeight + insets.top + vPadding
            }
            else -> {
                insets.top + vPadding
            }
        }

        for (i in list.indices) {
            val item = list[i]

            // x
            val x = if (fill) {
                xBase
            } else {
                when (hAlign) {
                    LEFT -> {
                        xBase
                    }
                    CENTER -> {
                        xBase + (preferredWidth - item.preferredSize.width) / 2
                    }
                    RIGHT -> {
                        xBase + preferredWidth - item.preferredSize.width
                    }
                    else -> {
                        xBase
                    }
                }
            }

            // width
            var width: Int
            if (fill) {
                width = if (wrap) preferredWidth else availableWidth
                // 下面这个判断的效果: 允许填充 且 允许折列 且 只有1列时, 填充全部可用区域
                // 或许可以来一个 开关 专门设置是否开启这个配置
                if (list.size == components.size) {
                    width = availableWidth
                }
            } else {
                width = item.preferredSize.width
            }

            // y
            if (i != 0) {
                y += vGap
            }

            // 组件调整
            item.setBounds(x, y, width, item.preferredSize.height)

            // y
            y += item.height
        }
    }

    private fun getVisibleComponents(container: Container): List<Component> {
        val list: MutableList<Component> = ArrayList()
        for (component in container.components) {
            if (component.isVisible) {
                list.add(component)
            }
        }
        return list
    }

    private fun getPreferredWidth(components: List<Component>): Int {
        var width = 0
        for (component in components) {
            width = max(width.toDouble(), component.preferredSize.width.toDouble()).toInt()
        }
        return width
    }

    private fun getPreferredHeight(components: List<Component>): Int {
        var height = 0
        // 可见组件的最大宽和累计高
        for (component in components) {
            height += component.preferredSize.height
        }
        // 累计高添加组件间间隙
        if (components.isNotEmpty()) {
            height += vGap * (components.size - 1)
        }
        return height
    }

    override fun toString(): String {
        return "VerticalFlowLayout{" +
                "hAlign=" + hAlign +
                ", vAlign=" + vAlign +
                ", hPadding=" + hPadding +
                ", vPadding=" + vPadding +
                ", hGap=" + hGap +
                ", vGap=" + vGap +
                ", fill=" + fill +
                ", wrap=" + wrap +
                '}'
    }

    companion object {
        @Serial
        private val serialVersionUID = 1L

        const val CENTER: Int = 0 // 垂直对齐/水平对齐
        const val TOP: Int = 1 // 垂直对齐
        const val BOTTOM: Int = 2 // 垂直对齐
        const val LEFT: Int = 3 // 水平对齐
        const val RIGHT: Int = 4 // 水平对齐
    }
}
