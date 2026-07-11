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

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.OverlayRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * Renders enabled modules in the top-left corner of the screen.
 * Similar to Wurst7's HackList HUD.
 * Registered via EventListener's auto-registration.
 */
class EnabledModulesHUD : EventListener {

    @Suppress("unused")
    private val overlayRenderHandler = handler<OverlayRenderEvent> { event ->
        render(event.context)
    }

    private fun render(context: GuiGraphicsExtractor) {
        val font = Minecraft.getInstance().font
        val activeModules = ModuleManager.filter { it.enabled && !it.hide }

        if (activeModules.isEmpty()) {
            return
        }

        var y = 2

        for (module in activeModules) {
            // Shadow
            context.text(font, module.name, 3, y + 1, 0x80000000.toInt(), false)
            // Main text - bright green for enabled modules
            context.text(font, module.name, 2, y, 0xFF66FF66.toInt(), false)

            y += font.lineHeight + 1
        }
    }
}
