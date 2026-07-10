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
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.Component
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.NativeClickGui
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW

/**
 * Component for color settings.
 * Shows the current color with a preview box.
 */
class ColorComponent(private val colorSetting: Value<Color4b>) : Component() {

    private val gui: ClickGui get() = NativeClickGui.gui
    private val font = mc.font
    private val boxSize = 11

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
    }

    override fun handleMouseClick(mouseX: Double, mouseY: Double, mouseButton: Int, context: MouseButtonEvent) {
        // Could open a color picker screen
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            colorSetting.restore()
        }
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val x3 = x1 + boxSize
        val y1 = getY(); val y2 = y1 + getHeight()

        val hovering = isHovering(mouseX, mouseY)
        val hText = hovering && mouseX >= x3

        if (hText) gui.tooltip = colorSetting.description.get() ?: ""

        // background
        context.fill(x3, y1, x2, y2,
            ClickGui.toIntColor(gui.getBgColor(), gui.getOpacity()))
        context.fill(x1, y1, x3, y2, colorSetting.get().argb)

        val outlineColor = ClickGui.toIntColor(gui.getAcColor(), 0.5f)
        ClickGui.drawBorder2D(context, x1.toFloat(), y1.toFloat(), x3.toFloat(), y2.toFloat(), outlineColor)

        context.guiRenderState.up()

        // text
        val hexStr = String.format("#%06X", 0xFFFFFF and colorSetting.get().argb)
        context.text(font, colorSetting.name, x3 + 2, y1 + 2, gui.getTxtColor(), false)
        context.text(font, hexStr, x2 - font.width(hexStr) - 2, y1 + 2, gui.getTxtColor(), false)
    }

    override fun getDefaultWidth(): Int {
        val hexStr = String.format("#%06X", 0xFFFFFF and colorSetting.get().argb)
        return boxSize + font.width(colorSetting.name) + font.width(hexStr) + 6
    }

    override fun getDefaultHeight() = boxSize
}
