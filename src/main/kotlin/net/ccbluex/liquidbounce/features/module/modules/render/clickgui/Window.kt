/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.render.clickgui

import net.minecraft.client.Minecraft
import net.minecraft.util.Mth

/**
 * Represents a window in the ClickGUI, containing components.
 * Adapted from Wurst7's Window class.
 */
open class Window(title: String) {

    companion object {
        private val MC = Minecraft.getInstance()
    }

    private val windowTitle: String = title

    private var x: Int = 0
    private var y: Int = 0
    private var width: Int = 0
    private var height: Int = 0

    private var valid: Boolean = false
    private val children = mutableListOf<Component>()

    private var dragging: Boolean = false
    private var dragOffsetX: Int = 0
    private var dragOffsetY: Int = 0

    private var minimized: Boolean = false
    private var minimizable: Boolean = true

    private var pinned: Boolean = false
    private var pinnable: Boolean = true

    private var closable: Boolean = false
    private var closing: Boolean = false

    private var invisible: Boolean = false
    private var positionClampingEnabled: Boolean = true

    private var fixedWidth: Boolean = false
    private var innerHeight: Int = 0
    private var maxInnerHeight: Int = 0
    private var scrollOffset: Int = 0
    private var scrollingEnabled: Boolean = false

    private var draggingScrollbar: Boolean = false
    private var scrollbarDragOffsetY: Int = 0

    fun getTitle() = windowTitle

    fun getX(): Int {
        if (!positionClampingEnabled) return x
        val scaledWidth = MC.window.guiScaledWidth
        return Mth.clamp(x, -width + 1, scaledWidth - 1)
    }

    fun getActualX() = x
    fun setX(x: Int) { this.x = x }

    fun getY(): Int {
        if (!positionClampingEnabled) return y
        val scaledHeight = MC.window.guiScaledHeight
        return Mth.clamp(y, -12, scaledHeight - 1)
    }

    fun getActualY() = y
    fun setY(y: Int) { this.y = y }

    fun getWidth() = width
    fun setWidth(width: Int) {
        if (fixedWidth) return
        if (this.width != width) invalidate()
        this.width = width
    }

    fun getHeight() = height
    fun setHeight(height: Int) {
        if (this.height != height) invalidate()
        this.height = height
    }

    fun pack() {
        var maxChildWidth = 0
        for (c in children) {
            if (c.getDefaultWidth() > maxChildWidth) {
                maxChildWidth = c.getDefaultWidth()
            }
        }
        maxChildWidth += 4

        val font = MC.font
        var titleBarWidth = font.width(windowTitle) + 4
        if (minimizable) titleBarWidth += 11
        if (pinnable) titleBarWidth += 11
        if (closable) titleBarWidth += 11

        var childrenHeight = 13
        for (c in children) {
            childrenHeight += c.getHeight() + 2
        }
        childrenHeight += 2

        if (maxInnerHeight > 0 && childrenHeight > maxInnerHeight + 13) {
            setWidth(maxOf(maxChildWidth + 3, titleBarWidth))
            setHeight(maxInnerHeight + 13)
        } else {
            setWidth(maxOf(maxChildWidth, titleBarWidth))
            setHeight(childrenHeight)
        }

        validate()
    }

    fun validate() {
        if (valid) return

        var offsetY = 2
        var cWidth = width - 4
        for (c in children) {
            c.setX(2)
            c.setY(offsetY)
            c.setWidth(cWidth)
            offsetY += c.getHeight() + 2
        }

        innerHeight = offsetY

        if (maxInnerHeight == 0 || innerHeight < maxInnerHeight) {
            setHeight(innerHeight + 13)
        } else {
            setHeight(maxInnerHeight + 13)
        }

        scrollingEnabled = innerHeight + 13 > height
        if (scrollingEnabled) {
            cWidth -= 3
        }

        scrollOffset = minOf(scrollOffset, 0)
        scrollOffset = maxOf(scrollOffset, -innerHeight + height - 13)

        for (c in children) {
            c.setWidth(cWidth)
        }

        valid = true
    }

    fun invalidate() { valid = false }

    fun countChildren() = children.size
    fun getChild(index: Int) = children[index]

    fun add(component: Component) {
        children.add(component)
        component.setParent(this)
        invalidate()
    }

    fun remove(index: Int) {
        children[index].setParent(null)
        children.removeAt(index)
        invalidate()
    }

    fun remove(component: Component) {
        children.remove(component)
        component.setParent(null)
        invalidate()
    }

    fun isDragging() = dragging
    fun startDragging(mouseX: Int, mouseY: Int) {
        dragging = true
        dragOffsetX = getX() - mouseX
        dragOffsetY = getY() - mouseY
    }

    fun dragTo(mouseX: Int, mouseY: Int) {
        x = mouseX + dragOffsetX
        y = mouseY + dragOffsetY
    }

    fun stopDragging() {
        dragging = false
        dragOffsetX = 0
        dragOffsetY = 0
    }

    fun isMinimized() = minimized
    fun setMinimized(minimized: Boolean) { this.minimized = minimized }

    fun isMinimizable() = minimizable
    fun setMinimizable(minimizable: Boolean) { this.minimizable = minimizable }

    fun isPinned() = pinned
    fun setPinned(pinned: Boolean) { this.pinned = pinned }

    fun isPinnable() = pinnable
    fun setPinnable(pinnable: Boolean) { this.pinnable = pinnable }

    fun isClosable() = closable
    fun setClosable(closable: Boolean) { this.closable = closable }

    fun isClosing() = closing
    fun close() { closing = true }

    fun isInvisible() = invisible
    fun setInvisible(invisible: Boolean) { this.invisible = invisible }

    fun isPositionClampingEnabled() = positionClampingEnabled
    fun setPositionClampingEnabled(enabled: Boolean) { positionClampingEnabled = enabled }

    fun isFixedWidth() = fixedWidth
    fun setFixedWidth(fixed: Boolean) { fixedWidth = fixed }

    fun getInnerHeight() = innerHeight
    fun setMaxInnerHeight(maxInnerHeight: Int) {
        val clamped = maxOf(0, maxInnerHeight)
        if (this.maxInnerHeight != clamped) invalidate()
        this.maxInnerHeight = clamped
    }

    fun setMaxHeight(maxHeight: Int) {
        setMaxInnerHeight(maxHeight - 13)
    }

    fun getScrollOffset() = scrollOffset
    fun setScrollOffset(scrollOffset: Int) { this.scrollOffset = scrollOffset }

    fun isScrollingEnabled() = scrollingEnabled
    fun isDraggingScrollbar() = draggingScrollbar

    fun startDraggingScrollbar(mouseY: Int) {
        draggingScrollbar = true
        val outerHeight = height - 13
        val scrollbarY = outerHeight * (-scrollOffset / innerHeight.toDouble()) + 1
        scrollbarDragOffsetY = scrollbarY.toInt() - mouseY
    }

    fun dragScrollbarTo(mouseY: Int) {
        val scrollbarY = mouseY + scrollbarDragOffsetY
        val outerHeight = height - 13
        scrollOffset = ((scrollbarY - 1) / outerHeight * innerHeight * -1).toInt()
        scrollOffset = minOf(scrollOffset, 0)
        scrollOffset = maxOf(scrollOffset, -innerHeight + height - 13)
    }

    fun stopDraggingScrollbar() {
        draggingScrollbar = false
        scrollbarDragOffsetY = 0
    }
}
