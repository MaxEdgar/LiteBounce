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

import net.ccbluex.liquidbounce.config.types.group.ModeValueGroup
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGui
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.Component
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.NativeClickGui
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent

/**
 * Component for mode value groups (like Speed mode, Fly mode).
 * Shows the current mode name as a clickable item.
 */
class ModeGroupComponent(private val group: ModeValueGroup<*>) : Component() {

    private val gui: ClickGui get() = NativeClickGui.gui
    private val font = mc.font
    private val textHeight = 11

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
    }

    override fun handleMouseClick(mouseX: Double, mouseY: Double, mouseButton: Int, context: MouseButtonEvent) {
        // Cycle through modes on click
        if (mouseButton == 0) {
            val modes = group.modes
            val currentIndex = modes.indexOf(group.activeMode)
            val nextIndex = (currentIndex + 1) % modes.size
            group.setByString(modes[nextIndex].tag)
        }
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val y1 = getY(); val y2 = y1 + getHeight()

        val hovering = isHovering(mouseX, mouseY)

        if (hovering) gui.tooltip = group.description.get() ?: ""

        val bgColor = ClickGui.toIntColor(gui.getBgColor(),
            gui.getOpacity() * (if (hovering) 1.5f else 1f))
        context.fill(x1, y1, x2, y2, bgColor)

        val outlineColor = ClickGui.toIntColor(gui.getAcColor(), 0.5f)
        ClickGui.drawBorder2D(context, x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), outlineColor)

        context.guiRenderState.up()

        val name = "${group.name}: ${group.activeMode.name}"
        context.text(font, name, x1 + 2, y1 + 2, gui.getTxtColor(), false)
    }

    override fun getDefaultWidth() = font.width("${group.name}: ${group.activeMode.name}") + 4

    override fun getDefaultHeight() = textHeight
}
