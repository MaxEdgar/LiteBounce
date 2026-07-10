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
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent

/**
 * Abstract base class for all ClickGUI components.
 * Adapted from Wurst7's Component architecture.
 */
abstract class Component {

    protected val mc: Minecraft get() = Minecraft.getInstance()

    private var x: Int = 0
    private var y: Int = 0
    private var width: Int = 0
    private var height: Int = 0

    private var parent: Window? = null

    open fun handleMouseClick(mouseX: Double, mouseY: Double, mouseButton: Int, context: MouseButtonEvent) {}

    abstract fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float)

    abstract fun getDefaultWidth(): Int

    abstract fun getDefaultHeight(): Int

    fun getX() = x
    fun setX(x: Int) {
        if (this.x != x) invalidateParent()
        this.x = x
    }

    fun getY() = y
    fun setY(y: Int) {
        if (this.y != y) invalidateParent()
        this.y = y
    }

    fun getWidth() = width
    fun setWidth(width: Int) {
        if (this.width != width) invalidateParent()
        this.width = width
    }

    fun getHeight() = height
    fun setHeight(height: Int) {
        if (this.height != height) invalidateParent()
        this.height = height
    }

    fun getParent() = parent
    fun setParent(parent: Window?) {
        this.parent = parent
    }

    private fun invalidateParent() {
        parent?.invalidate()
    }

    protected fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        val x1 = getX()
        val x2 = x1 + getWidth()
        val y1 = getY()
        val y2 = y1 + getHeight()

        val parent = getParent() ?: return false
        val scrollEnabled = parent.isScrollingEnabled()
        val scroll = if (scrollEnabled) parent.getScrollOffset() else 0

        return mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2
            && mouseY >= -scroll && mouseY < parent.getHeight() - 13 - scroll
    }
}
