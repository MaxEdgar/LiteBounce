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
package net.maxedgar.coffee.features.module.modules.world.scaffold.features

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.utils.entity.horizontalSpeed
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.maxedgar.coffee.utils.movement.DirectionalInput

object ScaffoldSpeedLimiterFeature : ToggleableValueGroup(ModuleScaffold, "SpeedLimiter", false) {

    private val speedLimit by float("SpeedLimit", 0.11f, 0.01f..0.4f)

    @Suppress("unused")
    private val moveEvent = handler<MovementInputEvent>(priority = EventPriorityConvention.SAFETY_FEATURE) {
        if (player.horizontalSpeed > speedLimit) {
            it.directionalInput = DirectionalInput.NONE
        }
    }

}
