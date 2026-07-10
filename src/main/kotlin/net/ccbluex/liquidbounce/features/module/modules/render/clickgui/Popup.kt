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

import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * Abstract popup for dropdown menus in the ClickGUI.
 * Adapted from Wurst7's Popup class.
 */
abstract class Popup(protected val owner: Component) {

    private var x: Int = 0
    private var y: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private var closing: Boolean = false

    abstract fun handleMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int)
    abstract fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int)
    abstract fun getDefaultWidth(): Int
    abstract fun getDefaultHeight(): Int

    fun getOwner() = owner
    fun getX() = x; fun setX(x: Int) { this.x = x }
    fun getY() = y; fun setY(y: Int) { this.y = y }
    fun getWidth() = width; fun setWidth(width: Int) { this.width = width }
    fun getHeight() = height; fun setHeight(height: Int) { this.height = height }
    fun isClosing() = closing; fun close() { closing = true }
}
