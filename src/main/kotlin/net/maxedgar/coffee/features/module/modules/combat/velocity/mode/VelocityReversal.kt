/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
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
package net.maxedgar.coffee.features.module.modules.combat.velocity.mode

import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.PlayerTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.utils.entity.moving
import net.maxedgar.coffee.utils.math.multiply
import net.maxedgar.coffee.utils.network.isLocalPlayerVelocity

/**
 * A velocity mode that reverses your velocity after a set amount of ticks.
 * Default values bypass Vulcan (3/9/25) ~ anticheat-test.com
 */
internal object VelocityReversal : VelocityMode("Reversal") {

    private val delay by int("Delay", 2, 1..5, "ticks")
    private val xModifier by float("XModifier", 0.5f, 0f..1.0f)
    private val zModifier by float("ZModifier", 0.5f, 0f..1.0f)
    private val onlyMoving by boolean("OnlyMoving", false)

    private var handlingVelocity = false
    private var velocityTicks = 0

    @Suppress("unused")
    private val packetEventHandler = handler<PacketEvent> { event ->
        if (!event.packet.isLocalPlayerVelocity()) {
            return@handler
        }

        if (onlyMoving && !player.moving) {
            return@handler
        }

        reset()
        handlingVelocity = true
    }

    @Suppress("unused")
    private val playerTickHandler = handler<PlayerTickEvent> {
        if (!handlingVelocity) {
            return@handler
        }

        when {
            player.deltaMovement.lengthSqr() == 0.0 -> reset()
            velocityTicks++ >= delay -> {
                player.deltaMovement = player.deltaMovement.multiply(
                    factorX = -xModifier,
                    factorZ = -zModifier,
                )
                reset()
            }
        }
    }

    private fun reset() {
        velocityTicks = 0
        handlingVelocity = false
    }
}
