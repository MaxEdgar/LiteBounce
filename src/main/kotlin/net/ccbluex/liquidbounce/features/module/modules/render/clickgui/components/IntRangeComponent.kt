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
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * Component for integer range pair settings (min..max).
 * Shows the values as text.
 */
class IntRangeComponent(private val setting: RangedValue<IntRange>) : Component() {

    private val gui: ClickGui get() = NativeClickGui.gui
    private val font = mc.font
    private val textHeight = 11

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val y1 = getY(); val y2 = y1 + getHeight()

        val hovering = isHovering(mouseX, mouseY)
        if (hovering) gui.tooltip = setting.description.get() ?: ""

        val bgColor = ClickGui.toIntColor(gui.getBgColor(),
            gui.getOpacity() * (if (hovering) 1.5f else 1f))
        context.fill(x1, y1, x2, y2, bgColor)

        val outlineColor = ClickGui.toIntColor(gui.getAcColor(), 0.5f)
        ClickGui.drawBorder2D(context, x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), outlineColor)

        context.guiRenderState.up()

        val range = setting.get()
        val valueStr = "${range.first}..${range.last}${setting.suffix}"
        val txtColor = gui.getTxtColor()
        context.text(font, setting.name, x1 + 2, y1 + 2, txtColor, false)
        context.text(font, valueStr, x2 - font.width(valueStr) - 2, y1 + 2, txtColor, false)
    }

    override fun getDefaultWidth(): Int {
        val range = setting.get()
        val valueStr = "${range.first}..${range.last}${setting.suffix}"
        return font.width(setting.name) + font.width(valueStr) + 10
    }

    override fun getDefaultHeight() = textHeight
}
