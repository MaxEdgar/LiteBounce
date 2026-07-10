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

import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGui
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGuiIcons
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.Component
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.SettingsWindow
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.Window
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent

/**
 * Clickable button for toggling a module on/off and opening its settings.
 * Adapted from Wurst7's FeatureButton.
 */
class ModuleButton(private val module: ClientModule) : Component() {

    private val gui: ClickGui get() = clickGui
    private val font: Font get() = mc.font
    private val hasSettings: Boolean = module.inner.isNotEmpty()

    private var settingsWindow: Window? = null

    init {
        setWidth(getDefaultWidth())
        setHeight(getDefaultHeight())
    }

    override fun handleMouseClick(mouseX: Double, mouseY: Double, mouseButton: Int, context: MouseButtonEvent) {
        if (mouseButton != 0) return

        if (hasSettings && (mouseX > getX() + getWidth() - 12 || module.disableActivation)) {
            toggleSettingsWindow()
            return
        }

        module.enabled = !module.enabled
    }

    private fun toggleSettingsWindow() {
        if (settingsWindow == null || settingsWindow!!.isClosing()) {
            settingsWindow = SettingsWindow(module, getParent()!!, getY())
            gui.addWindow(settingsWindow!!)
        } else {
            settingsWindow!!.close()
            settingsWindow = null
        }
    }

    override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val x1 = getX(); val x2 = x1 + getWidth()
        val x3 = if (hasSettings || module.disableActivation) x2 - 11 else x2
        val y1 = getY(); val y2 = y1 + getHeight()

        val hovering = isHovering(mouseX, mouseY)
        val hFeature = hovering && mouseX < x3
        val hSettings = hovering && mouseX >= x3

        if (hFeature) {
            gui.tooltip = module.description.get() ?: ""
        }

        // buttons
        context.fill(x1, y1, x3, y2, getButtonColor(module.enabled, hFeature))
        if (hasSettings || module.disableActivation) {
            context.fill(x3, y1, x2, y2, getButtonColor(false, hSettings))
        }

        context.guiRenderState.up()

        // outlines
        val outlineColor = ClickGui.toIntColor(gui.getAcColor(), 0.5f)
        ClickGui.drawBorder2D(context, x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), outlineColor)
        if (hasSettings || module.disableActivation) {
            ClickGui.drawLine2D(context, x3.toFloat(), y1.toFloat(), x3.toFloat(), y2.toFloat(), outlineColor)
        }

        // arrow
        if (hasSettings || module.disableActivation) {
            ClickGuiIcons.drawMinimizeArrow(context, x3.toFloat(), y1 + 0.5f, x2.toFloat(), y2 - 0.5f,
                hSettings, settingsWindow?.isClosing() != false)
        }

        // text
        val name = module.name
        val tx = x1 + (x3 - x1 - font.width(name)) / 2
        val ty = y1 + 2
        context.text(font, name, tx, ty, gui.getTxtColor(), false)
    }

    private fun getButtonColor(enabled: Boolean, hovering: Boolean): Int {
        val rgb = if (enabled) floatArrayOf(0f, 1f, 0f) else gui.getBgColor()
        val opacity = gui.getOpacity() * (if (hovering) 1.5f else 1f)
        return ClickGui.toIntColor(rgb, opacity)
    }

    override fun getDefaultWidth(): Int {
        var width = font.width(module.name)
        width += if (hasSettings || module.disableActivation) 15 else 4
        return width
    }

    override fun getDefaultHeight() = 11

    companion object {
        private val clickGui: ClickGui
            get() = net.ccbluex.liquidbounce.features.module.modules.render.clickgui.NativeClickGui.gui
    }
}
