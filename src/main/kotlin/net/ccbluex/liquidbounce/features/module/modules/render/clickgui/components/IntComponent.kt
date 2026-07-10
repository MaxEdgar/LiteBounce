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
package net.ccbluex.liquidbounce.features.module.modules.render.clickgui.components

import net.ccbluex.liquidbounce.config.types.RangedValue
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGui
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.Component
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.NativeClickGui
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW

/**
 * Slider component for integer settings.
 * Adapted from Wurst7's SliderComponent.
 */
class IntComponent(private val setting: RangedValue<Int>) : Component() {

    private val gui: ClickGui get() = NativeClickGui.gui
    private val font = mc.font
    private val textHeight = 11

    private var dragging = false

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
    }

    override fun handleMouseClick(mouseX: Double, mouseY: Double, mouseButton: Int, context: MouseButtonEvent) {
        if (mouseY < getY() + 11) return

        when (mouseButton) {
            GLFW.GLFW_MOUSE_BUTTON_LEFT -> dragging = true
            GLFW.GLFW_MOUSE_BUTTON_RIGHT -> setting.restore()
        }
    }

    private fun handleDragging(mouseX: Int, x3: Int, x4: Int) {
        if (!dragging) return
        if (!gui.isLeftMouseButtonPressed()) {
            dragging = false
            return
        }

        val percentage = (mouseX - x3).toDouble() / (x4 - x3).toDouble()
        val min = (setting.range as IntRange).first
        val max = (setting.range as IntRange).last
        val value = (min + ((max - min) * percentage).toInt()).coerceIn(min, max)
        setting.set(value)
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val x3 = x1 + 2; val x4 = x2 - 2
        val y1 = getY(); val y2 = y1 + getHeight()
        val y3 = y1 + textHeight; val y4 = y3 + 4; val y5 = y2 - 4

        handleDragging(mouseX, x3, x4)

        val hovering = isHovering(mouseX, mouseY)
        val hSlider = (hovering && mouseY >= y3) || dragging

        if (hovering && mouseY < y3) gui.tooltip = setting.description.get() ?: ""

        val opacity = gui.getOpacity()
        val railOpacity = opacity * (if (hSlider) 1.5f else 1f)

        ClickGui.fill2D(context, x1.toFloat(), y1.toFloat(), x2.toFloat(), y3.toFloat(),
            ClickGui.toIntColor(gui.getBgColor(), opacity))
        ClickGui.fill2D(context, x1.toFloat(), y5.toFloat(), x2.toFloat(), y2.toFloat(),
            ClickGui.toIntColor(gui.getBgColor(), opacity))
        ClickGui.fill2D(context, x1.toFloat(), y3.toFloat(), x3.toFloat(), y5.toFloat(),
            ClickGui.toIntColor(gui.getBgColor(), opacity))
        ClickGui.fill2D(context, x4.toFloat(), y3.toFloat(), x2.toFloat(), y5.toFloat(),
            ClickGui.toIntColor(gui.getBgColor(), opacity))

        ClickGui.fill2D(context, x3.toFloat(), y4.toFloat(), x4.toFloat(), y5.toFloat(),
            ClickGui.toIntColor(gui.getBgColor(), railOpacity))
        ClickGui.drawBorder2D(context, x3.toFloat(), y4.toFloat(), x4.toFloat(), y5.toFloat(),
            ClickGui.toIntColor(gui.getAcColor(), 0.5f))

        context.guiRenderState.up()

        val min = (setting.range as IntRange).first
        val max = (setting.range as IntRange).last
        val range = max - min
        val percentage = if (range == 0) 0f else (setting.get() - min).toFloat() / range.toFloat()

        val xk1 = x1 + (x2 - x1 - 8) * percentage
        val xk2 = xk1 + 8
        val yk1 = y3 + 1.5f; val yk2 = y2 - 1.5f
        val knobColor = ClickGui.toIntColor(gui.getAcColor(), if (hSlider) 1f else 0.75f)
        ClickGui.fill2D(context, xk1, yk1, xk2, yk2, knobColor)
        ClickGui.drawBorder2D(context, xk1, yk1, xk2, yk2, 0x80101010)

        val name = setting.name
        val valueStr = "${setting.get()}${setting.suffix}"
        val valueWidth = font.width(valueStr)
        val txtColor = gui.getTxtColor()
        context.text(font, name, x1, y1 + 2, txtColor, false)
        context.text(font, valueStr, x2 - valueWidth, y1 + 2, txtColor, false)
    }

    override fun getDefaultWidth(): Int {
        val valueStr = "${setting.get()}${setting.suffix}"
        return font.width(setting.name) + font.width(valueStr) + 6
    }

    override fun getDefaultHeight() = textHeight * 2
}
