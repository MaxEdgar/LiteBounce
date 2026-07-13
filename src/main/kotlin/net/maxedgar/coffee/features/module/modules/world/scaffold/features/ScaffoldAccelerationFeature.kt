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
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.utils.math.multiply

object ScaffoldAccelerationFeature : ToggleableValueGroup(ModuleScaffold, "Acceleration", false) {
    private val speedMultiplier by float("SpeedMultiplier", 0.6f, 0.1f..3f)
    private val onlyOnGround by boolean("OnlyOnGround", false)

    @Suppress("unused")
    val stateUpdateHandler = handler<GameTickEvent> {
        if (onlyOnGround && !player.onGround()) {
            return@handler
        }

        player.deltaMovement = player.deltaMovement.multiply(factorX = speedMultiplier, factorZ = speedMultiplier)
    }
}
