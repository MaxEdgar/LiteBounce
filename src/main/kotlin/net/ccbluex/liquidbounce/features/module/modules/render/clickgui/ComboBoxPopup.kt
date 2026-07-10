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

import net.ccbluex.liquidbounce.config.types.list.ChoiceListValue
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.lwjgl.glfw.GLFW

/**
 * Dropdown popup for enum/choice settings.
 * Adapted from Wurst7's ComboBoxPopup.
 */
class ComboBoxPopup(
    ownerComponent: Component,
    private val setting: ChoiceListValue<*>,
) : Popup(ownerComponent) {

    private val gui: ClickGui get() = NativeClickGui.gui
    private val font: Font get() = Minecraft.getInstance().font

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
        setX(ownerComponent.getWidth() - getWidth())
        setY(ownerComponent.getHeight())
    }

    override fun handleMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) return

        var yi1 = getY() - 11
        for (choice in setting.choices) {
            if (choice == setting.get()) continue
            yi1 += 11
            val yi2 = yi1 + 11
            if (mouseY < yi1 || mouseY >= yi2) continue

            setting.setByString(choice.tag)
            close()
            break
        }
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val y1 = getY(); val y2 = y1 + getHeight()

        ClickGui.drawBorder2D(context, x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(),
            ClickGui.toIntColor(gui.getAcColor(), 0.5f))

        var yi1 = y1 - 11
        for (choice in setting.choices) {
            if (choice == setting.get()) continue
            yi1 += 11
            val yi2 = yi1 + 11

            val hValue = mouseX >= x1 && mouseY >= yi1 && mouseX < x2 && mouseY < yi2
            context.fill(x1, yi1, x2, yi2,
                ClickGui.toIntColor(gui.getBgColor(), gui.getOpacity() * (if (hValue) 1.5f else 1f)))

            context.guiRenderState.up()
            context.text(font, choice.tag, x1 + 2, yi1 + 2, gui.getTxtColor(), false)
        }
    }

    override fun getDefaultWidth(): Int {
        var maxWidth = 0
        for (choice in setting.choices) {
            val w = font.width(choice.tag)
            if (w > maxWidth) maxWidth = w
        }
        return maxWidth + 15
    }

    override fun getDefaultHeight() = (setting.choices.size - 1) * 11
}
