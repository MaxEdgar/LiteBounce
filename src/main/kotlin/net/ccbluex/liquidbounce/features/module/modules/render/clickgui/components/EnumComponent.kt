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

import net.ccbluex.liquidbounce.config.types.list.ChoiceListValue
import net.ccbluex.liquidbounce.config.types.list.Tagged
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGui
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGuiIcons
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.Component
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ComboBoxPopup
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import org.lwjgl.glfw.GLFW

/**
 * Dropdown component for enum/choice settings.
 * Adapted from Wurst7's ComboBoxComponent.
 */
class EnumComponent(private val setting: ChoiceListValue<*>) : Component() {

    private val gui: ClickGui get() = NativeClickGui.gui
    private val font = mc.font
    private val arrowSize = 11

    private var popup: ComboBoxPopup? = null

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
    }

    override fun handleMouseClick(mouseX: Double, mouseY: Double, mouseButton: Int, context: MouseButtonEvent) {
        when (mouseButton) {
            GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
                // Toggle popup
                if (popup == null || popup!!.isClosing()) {
                    popup = ComboBoxPopup(this, setting)
                    gui.addPopup(popup!!)
                } else {
                    popup!!.close()
                    popup = null
                }
            }
            GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                if (popup?.isClosing() != false) {
                    setting.restore()
                }
            }
        }
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val x3 = x2 - arrowSize; val x4 = x3 - font.width(setting.get().toString()) - 4
        val y1 = getY(); val y2 = y1 + getHeight()

        val hovering = isHovering(mouseX, mouseY)
        val hText = hovering && mouseX < x4
        val hBox = hovering && mouseX >= x4

        if (hText) gui.tooltip = setting.description.get() ?: ""

        // background
        context.fill(x1, y1, x4, y2, getFillColor(false))
        context.fill(x4, y1, x2, y2, getFillColor(hBox))

        context.guiRenderState.up()

        // outlines
        val outlineColor = ClickGui.toIntColor(gui.getAcColor(), 0.5f)
        ClickGui.drawBorder2D(context, x4.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), outlineColor)
        ClickGui.drawLine2D(context, x3.toFloat(), y1.toFloat(), x3.toFloat(), y2.toFloat(), outlineColor)

        // arrow
        ClickGuiIcons.drawMinimizeArrow(context, x3.toFloat(), y1 + 0.5f, x2.toFloat(), y2 - 0.5f,
            hBox, popup?.isClosing() != false)

        // text
        val name = setting.name
        val value = setting.get().toString()
        val txtColor = gui.getTxtColor()
        context.text(font, name, x1, y1 + 2, txtColor, false)
        context.text(font, value, x4 + 2, y1 + 2, txtColor, false)
    }

    private fun getFillColor(hovering: Boolean): Int {
        val opacity = gui.getOpacity() * (if (hovering) 1.5f else 1f)
        return ClickGui.toIntColor(gui.getBgColor(), opacity)
    }

    override fun getDefaultWidth(): Int {
        return font.width(setting.name) + font.width(setting.get().toString()) + arrowSize + 6
    }

    override fun getDefaultHeight() = arrowSize
}
