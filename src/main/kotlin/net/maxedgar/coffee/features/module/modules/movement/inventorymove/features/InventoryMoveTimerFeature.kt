/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.features.module.modules.movement.inventorymove.features

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.modules.movement.inventorymove.ModuleInventoryMove
import net.maxedgar.coffee.utils.client.Timer
import net.maxedgar.coffee.utils.kotlin.Priority
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object InventoryMoveTimerFeature : ToggleableValueGroup(ModuleInventoryMove, "Timer", false) {

    private val speed by float("Speed", 1.0f, 0.1f..2.0f)

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (mc.gui.screen() is AbstractContainerScreen<*>) {
            Timer.requestTimerSpeed(speed, Priority.IMPORTANT_FOR_USAGE_2, ModuleInventoryMove)
        }
    }

}
