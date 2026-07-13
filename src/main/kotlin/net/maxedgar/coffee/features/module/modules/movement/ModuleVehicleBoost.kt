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
package net.maxedgar.coffee.features.module.modules.movement

import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vehicle Boost module
 *
 * Boosts you when leaving a vehicle.
 */
object ModuleVehicleBoost : ClientModule("VehicleBoost", ModuleCategories.MOVEMENT) {

    private var horizontalSpeed by float("HorizontalSpeed", 2f, 0.1f..10f)
    private var verticalSpeed by float("VerticalSpeed", 1f, 0.1f..5f)
    private var wasInVehicle = false

    val repeatable = tickHandler {
        val isInVehicle = player.isPassenger

        if (wasInVehicle && !isInVehicle) {
            val angle = Math.toRadians(player.yRot.toDouble())

            // Boost player
            player.deltaMovement = Vec3(
                -sin(angle) * horizontalSpeed.toDouble(),
                verticalSpeed.toDouble(),
                cos(angle) * horizontalSpeed.toDouble()
            )
        }

        wasInVehicle = isInVehicle
    }

}
