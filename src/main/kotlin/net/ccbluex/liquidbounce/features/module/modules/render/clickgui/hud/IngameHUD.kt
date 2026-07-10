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

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.OverlayRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGuiScreen
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.NativeClickGui
import net.ccbluex.liquidbounce.utils.client.mc

/**
 * Manages the in-game HUD rendering.
 * Uses LiquidBounce's OverlayRenderEvent via the EventListener handler delegate.
 */
class IngameHUD : EventListener {

    @Suppress("unused")
    private val overlayRenderHandler = handler<OverlayRenderEvent> { event ->
        val context = event.context
        val partialTicks = event.tickDelta

        // Render module list
        ModuleListHUD.render(context, partialTicks)

        // Render pinned windows if ClickGUI is not open
        val clickGui = NativeClickGui.gui
        if (mc.gui.screen() !is ClickGuiScreen) {
            clickGui.renderPinnedWindows(context, partialTicks)
        }
    }
}
