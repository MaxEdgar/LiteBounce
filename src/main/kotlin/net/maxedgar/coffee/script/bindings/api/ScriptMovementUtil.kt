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
package net.maxedgar.coffee.script.bindings.api

import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.entity.horizontalSpeed
import net.maxedgar.coffee.utils.entity.moving
import net.maxedgar.coffee.utils.entity.withStrafe

@Suppress("unused")
object ScriptMovementUtil {

    @JvmName("getSpeed")
    fun getSpeed(): Double = player.horizontalSpeed

    @JvmName("isMoving")
    fun isMoving(): Boolean = player.moving

    @JvmName("strafe")
    fun strafe() {
        player.deltaMovement = player.deltaMovement.withStrafe()
    }

    @JvmName("strafeWithSpeed")
    fun strafeWithSpeed(speed: Double) {
        player.deltaMovement = player.deltaMovement.withStrafe(speed = speed)
    }

    @JvmName("strafeWithStrength")
    fun strafeWithStrength(strength: Double) {
        player.deltaMovement = player.deltaMovement.withStrafe(strength = strength)
    }

    @JvmName("strafeWithSpeedAndStrength")
    fun strafeWithSpeedAndStrength(speed: Double, strength: Double) {
        player.deltaMovement = player.deltaMovement.withStrafe(speed = speed, strength = strength)
    }

}
