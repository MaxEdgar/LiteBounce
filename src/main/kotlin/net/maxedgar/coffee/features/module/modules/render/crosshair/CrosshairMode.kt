/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2025 MaxEdgar
 *
 * Coffee is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coffee is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coffee. If not, see <https://www.gnu.org/licenses/>.
 */

package net.maxedgar.coffee.features.module.modules.render.crosshair

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.OverlayRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.render.crosshair.ModuleCrosshair.modes
import net.maxedgar.coffee.render.withPush
import net.maxedgar.coffee.utils.inventory.isInContainerScreen
import net.maxedgar.coffee.utils.inventory.isInInventoryScreen

abstract class CrosshairMode(name: String) : Mode(name) {
    final override val parent: ModeValueGroup<*>
        get() = modes

    protected val showInThirdPerson by boolean("ShowInThirdPerson", true)

    protected abstract fun OverlayRenderEvent.drawCrosshair()

    @Suppress("unused")
    private val cursorHandler =
        handler<OverlayRenderEvent> {
            if (!mc.options.cameraType.isFirstPerson && !showInThirdPerson) return@handler
            if (isInInventoryScreen || isInContainerScreen) return@handler

            val centerX = (it.context.guiWidth() / 2.002f)
            val centerY = (it.context.guiHeight() / 2.0025f)

            it.context.pose().withPush {
                translate(centerX, centerY)
                it.drawCrosshair()
            }
        }
}
