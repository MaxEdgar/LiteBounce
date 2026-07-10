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
package net.ccbluex.liquidbounce.features.module.modules.render.clickgui.hud

import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.util.CommonColors

/**
 * Native HUD module list that displays enabled modules.
 * Rendered as part of IngameHUD's overlay render handling.
 * Adapted from Wurst7's HackListHUD.
 */
object ModuleListHUD {

    private var posY = 2

    fun render(context: GuiGraphicsExtractor, partialTicks: Float) {
        val font = Minecraft.getInstance().font
        posY = 2

        val activeModules = ModuleManager.filter { it.enabled }

        // Count mode
        if (posY + activeModules.size * 9 > context.guiHeight()) {
            drawCounter(context, activeModules.size)
        } else {
            drawModuleList(context, activeModules)
        }
    }

    private fun drawCounter(context: GuiGraphicsExtractor, count: Int) {
        val s = "$count hack${if (count != 1) "s" else ""} active"
        drawString(context, s)
    }

    private fun drawModuleList(context: GuiGraphicsExtractor, modules: List<*>) {
        // TODO: Add proper sorting and positioning based on settings
        for (module in ModuleManager.sortedBy { it.name }) {
            if (module.enabled) {
                drawString(context, module.name)
            }
        }
    }

    private fun drawString(context: GuiGraphicsExtractor, s: String) {
        val font = Minecraft.getInstance().font
        val posX = 2 // Left position

        val textColor = 0x04 shl 24 or 0xFF0000.toInt() or CommonColors.BLACK

        // Shadow
        context.text(font, s, posX + 1, posY + 1, CommonColors.BLACK, false)
        context.guiRenderState.up()
        // Main text
        context.text(font, s, posX, posY, textColor, false)

        posY += 9
    }
}
