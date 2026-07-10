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

import net.ccbluex.liquidbounce.config.types.Value
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGui
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGuiIcons
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.Component
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW

/**
 * Checkbox component for boolean settings.
 * Adapted from Wurst7's CheckboxComponent.
 */
class BooleanComponent(private val setting: Value<Boolean>) : Component() {

    private val gui: ClickGui get() = NativeClickGui.gui
    private val font: Font get() = mc.font
    private val boxSize = 11

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
    }

    override fun handleMouseClick(mouseX: Double, mouseY: Double, mouseButton: Int, context: MouseButtonEvent) {
        when (mouseButton) {
            GLFW.GLFW_MOUSE_BUTTON_LEFT -> setting.set(!setting.get())
            GLFW.GLFW_MOUSE_BUTTON_RIGHT -> setting.restore()
        }
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val x3 = x1 + boxSize
        val y1 = getY(); val y2 = y1 + getHeight()

        val hovering = isHovering(mouseX, mouseY)
        val hText = hovering && mouseX >= x3

        if (hText) {
            gui.tooltip = setting.description.get() ?: ""
        }

        // background
        context.fill(x3, y1, x2, y2, getFillColor(false))

        // box
        context.fill(x1, y1, x3, y2, getFillColor(hovering))
        val outlineColor = ClickGui.toIntColor(gui.getAcColor(), 0.5f)
        ClickGui.drawBorder2D(context, x1.toFloat(), y1.toFloat(), x3.toFloat(), y2.toFloat(), outlineColor)

        context.guiRenderState.up()

        // check
        if (setting.get()) {
            ClickGuiIcons.drawCheck(context, x1.toFloat(), y1.toFloat(), x3.toFloat(), y2.toFloat(), hovering, false)
        }

        // text
        context.text(font, setting.name, x3 + 2, y1 + 2, gui.getTxtColor(), false)
    }

    private fun getFillColor(hovering: Boolean): Int {
        val opacity = gui.getOpacity() * (if (hovering) 1.5f else 1f)
        return ClickGui.toIntColor(gui.getBgColor(), opacity)
    }

    override fun getDefaultWidth() = boxSize + font.width(setting.name) + 2

    override fun getDefaultHeight() = boxSize
}
