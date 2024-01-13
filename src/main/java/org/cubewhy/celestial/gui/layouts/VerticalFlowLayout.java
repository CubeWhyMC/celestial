/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.gui.layouts;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class VerticalFlowLayout implements LayoutManager, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final int CENTER = 0; // 垂直对齐/水平对齐
    public static final int TOP = 1; // 垂直对齐
    public static final int BOTTOM = 2; // 垂直对齐
    public static final int LEFT = 3; // 水平对齐
    public static final int RIGHT = 4; // 水平对齐

    private int hAlign; // 每一列中各组件水平向对齐方式(注意非每一列在容器中的水平向对齐方式, 因为每一列在容器中的水平对齐方式应当由 容器的 componentOrientation 属性 ltr/rtl 来指定)
    private int vAlign; // 每一列在容器中的垂直向对齐方式(注意无每一列在容器中的水平向对齐方式)
    private int hPadding; // 水平向边框与组件之间的间隙
    private int vPadding; // 垂直向边框与组件之间的间隙, TOP:顶边距, BOTTOM:底边距
    private int hGap; // 水平向组件之间的间隙
    private int vGap; // 垂直向组件之间的间隙
    @Getter
    @Setter
    private boolean fill; // 水平向组件是否填满逻辑列的宽度
    @Setter
    @Getter
    private boolean wrap; // 是否折列, true:折列, false:固定一列
    // 折列时, 每列列宽是否相同
    // 折列时, 列是否填充满容器

    public VerticalFlowLayout(int hAlign, int vAlign, int hPadding, int vPadding, int hGap, int vGap, boolean fill, boolean wrap) {
        this.hAlign = hAlign;
        this.vAlign = vAlign;
        this.hPadding = hPadding;
        this.vPadding = vPadding;
        this.hGap = hGap;
        this.vGap = vGap;
        this.fill = fill;
        this.wrap = wrap;
    }

    public VerticalFlowLayout() {
        this(LEFT, TOP, 5, 5, 5, 5, true, false);
    }

    public VerticalFlowLayout(int padding, int gap) {
        this(LEFT, TOP, padding, padding, gap, gap, true, false);
    }

    public VerticalFlowLayout(int padding) {
        this(LEFT, TOP, padding, padding, 5, 5, true, false);
    }

    public int getHAlign() {
        return hAlign;
    }

    public void setHAlign(int hAlign) {
        this.hAlign = hAlign;
    }

    public int getVAlign() {
        return vAlign;
    }

    public void setVAlign(int vAlign) {
        this.vAlign = vAlign;
    }

    public int getHPadding() {
        return hPadding;
    }

    public void setHPadding(int hPadding) {
        this.hPadding = hPadding;
    }

    public int getVPadding() {
        return vPadding;
    }

    public void setVPadding(int vPadding) {
        this.vPadding = vPadding;
    }

    public int getHGap() {
        return hGap;
    }

    public void setHGap(int hGap) {
        this.hGap = hGap;
    }

    public int getVGap() {
        return vGap;
    }

    public void setVGap(int vGap) {
        this.vGap = vGap;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    /**
     * 最合适的尺寸, 一列放下全部组件
     */
    @Override
    public Dimension preferredLayoutSize(Container container) {
        synchronized (container.getTreeLock()) {

            int width = 0, height = 0;

            // 可见组件的最大宽和累计高
            List<Component> components = getVisibleComponents(container);
            for (Component component : components) {
                Dimension dimension = component.getPreferredSize();
                width = Math.max(width, dimension.width);
                height += dimension.height;
            }

            // 累计高添加组件间间隙
            if (!components.isEmpty()) {
                height += vGap * (components.size() - 1);
            }

            // 累计宽高添加边框宽高
            Insets insets = container.getInsets();
            width += insets.left + insets.right;
            height += insets.top + insets.bottom;

            // 有组件的话, 累计宽高添加边框与组件的间隙和
            if (!components.isEmpty()) {
                width += hPadding * 2;
                height += vPadding * 2;
            }

            return new Dimension(width, height);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {

            int width = 0, height = 0;

            // 可见组件的最大宽和累计高
            List<Component> components = getVisibleComponents(parent);
            for (Component component : components) {
                Dimension dimension = component.getMinimumSize();
                width = Math.max(width, dimension.width);
                height += dimension.height;
            }

            // 累计高添加组件间间隙
            if (!components.isEmpty()) {
                height += vGap * (components.size() - 1);
            }

            // 累计宽高添加边框宽高
            Insets insets = parent.getInsets();
            width += insets.left + insets.right;
            height += insets.top + insets.bottom;

            // 有组件的话, 累计宽高添加边框与组件的间隙和
            if (!components.isEmpty()) {
                width += hPadding * 2;
                height += vPadding * 2;
            }

            return new Dimension(width, height);
        }
    }

    @Override
    public void layoutContainer(Container container) {
        synchronized (container.getTreeLock()) {

            // 容器理想宽高, 即组件加间隙加容器边框后的累积理想宽高, 用于和容器实际宽高做比较
            Dimension idealSize = preferredLayoutSize(container);
            // 容器实际宽高
            Dimension size = container.getSize();
            // 容器实际边框
            Insets insets = container.getInsets();

            // 容器内可供组件使用的空间大小(排除边框和内边距)
            int availableWidth = size.width - insets.left - insets.right - hPadding * 2;
            int availableHeight = size.height - insets.top - insets.bottom - vPadding * 2;

            // 容器定义的组件方向, 这里先不管, 默认从左往右

            // 容器内所有可见组件
            List<Component> components = getVisibleComponents(container);

            // x基点
            int xBase = insets.left + hPadding;

            // 缓存当前列中的所有组件
            List<Component> list = new LinkedList<>();

            for (Component component : components) {

                list.add(component);

                // 预算判断
                // 换列标准: 允许换列 且 该列组件数>1 且 该列累积高>容器可用高+vPadding
                // 累积高: 算上当前组件后, 当前列中的组件的累加高度(组件高度+组件间隙)
                if (wrap && list.size() > 1 && availableHeight + vPadding < getPreferredHeight(list)) {

                    // 如果需要换行, 则当前列中得移除当前组件
                    list.remove(component);

                    batch(insets, availableWidth, availableHeight, xBase, list, components);

                    xBase += hGap + getPreferredWidth(list);

                    // 需要换列, 清空上一列中的所有组件
                    list.clear();

                    list.add(component);

                }

            }

            if (!list.isEmpty()) {
                batch(insets, availableWidth, availableHeight, xBase, list, components);
            }
        }
    }

    private void batch(Insets insets, int availableWidth, int availableHeight, int xBase, List<Component> list, List<Component> components) {

        int preferredWidth = getPreferredWidth(list);
        int preferredHeight = getPreferredHeight(list);

        // y
        int y;
        if (vAlign == TOP) {
            y = insets.top + vPadding;
        } else if (vAlign == CENTER) {
            y = (availableHeight - preferredHeight) / 2 + insets.top + vPadding;
        } else if (vAlign == BOTTOM) {
            y = availableHeight - preferredHeight + insets.top + vPadding;
        } else {
            y = insets.top + vPadding;
        }

        for (int i = 0; i < list.size(); i++) {

            Component item = list.get(i);

            // x
            int x;
            if (fill) {
                x = xBase;
            } else {
                if (hAlign == LEFT) {
                    x = xBase;
                } else if (hAlign == CENTER) {
                    x = xBase + (preferredWidth - item.getPreferredSize().width) / 2;
                } else if (hAlign == RIGHT) {
                    x = xBase + preferredWidth - item.getPreferredSize().width;
                } else {
                    x = xBase;
                }
            }

            // width
            int width;
            if (fill) {
                width = wrap ? preferredWidth : availableWidth;
                // 下面这个判断的效果: 允许填充 且 允许折列 且 只有1列时, 填充全部可用区域
                // 或许可以来一个 开关 专门设置是否开启这个配置
                if (list.size() == components.size()) {
                    width = availableWidth;
                }
            } else {
                width = item.getPreferredSize().width;
            }

            // y
            if (i != 0) {
                y += vGap;
            }

            // 组件调整
            item.setBounds(x, y, width, item.getPreferredSize().height);

            // y
            y += item.getHeight();

        }

    }

    private List<Component> getVisibleComponents(Container container) {
        List<Component> list = new ArrayList<>();
        for (Component component : container.getComponents()) {
            if (component.isVisible()) {
                list.add(component);
            }
        }
        return list;
    }

    private int getPreferredWidth(List<Component> components) {
        int width = 0;
        for (Component component : components) {
            width = Math.max(width, component.getPreferredSize().width);
        }
        return width;
    }

    private int getPreferredHeight(List<Component> components) {
        int height = 0;
        // 可见组件的最大宽和累计高
        for (Component component : components) {
            height += component.getPreferredSize().height;
        }
        // 累计高添加组件间间隙
        if (!components.isEmpty()) {
            height += vGap * (components.size() - 1);
        }
        return height;
    }

    @Override
    public String toString() {
        return "VerticalFlowLayout{" +
                "hAlign=" + hAlign +
                ", vAlign=" + vAlign +
                ", hPadding=" + hPadding +
                ", vPadding=" + vPadding +
                ", hGap=" + hGap +
                ", vGap=" + vGap +
                ", fill=" + fill +
                ", wrap=" + wrap +
                '}';
    }
}
