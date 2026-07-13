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
package net.maxedgar.coffee.features.module.modules.combat.velocity.mode

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.PlayerMoveEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.sequenceHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.combat.findEnemy
import net.maxedgar.coffee.utils.entity.horizontalSpeed
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.entity.withStrafe
import net.maxedgar.coffee.utils.network.isLocalPlayerVelocity
import net.maxedgar.coffee.utils.raytracing.isLookingAtEntity

/**
 * Strafe velocity
 */
internal object VelocityStrafe : VelocityMode("Strafe") {

    private val delay by int("Delay", 2, 0..10, "ticks")
    private val strength by float("Strength", 1f, 0.1f..2f)

    object OnlyFacing: ToggleableValueGroup(this, "OnlyFacing", false) {
        val range by float("Range", 3.5f, 0.1f..6f)
    }

    init {
        tree(OnlyFacing)
    }

    private val untilGround by boolean("UntilGround", false)

    private var applyStrafe = false
    private var shouldStrafe = false

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (!OnlyFacing.enabled) return@handler
        val target = world.findEnemy(0f, OnlyFacing.range) ?: return@handler

        shouldStrafe = isLookingAtEntity(
            target,
            OnlyFacing.range.toDouble(),
            RotationManager.currentRotation ?: player.rotation
        ) != null
    }

    @Suppress("unused")
    private val packetHandler = sequenceHandler<PacketEvent> { event ->
        val packet = event.packet

        // Check if this is a regular velocity update
        if (packet.isLocalPlayerVelocity()) {
            if (OnlyFacing.enabled && !shouldStrafe) {
                return@sequenceHandler
            }

            // A few anti-cheats can be easily tricked by applying the velocity a few ticks after being damaged
            waitTicks(delay)

            // Apply strafe
            player.deltaMovement = player.deltaMovement.withStrafe(speed = player.horizontalSpeed * strength)

            if (untilGround) {
                applyStrafe = true
            }
        }
    }

    @Suppress("unused")
    private val moveHandler = handler<PlayerMoveEvent> { event ->
        if (player.onGround()) {
            applyStrafe = false
        } else if (applyStrafe) {
            event.movement = event.movement.withStrafe(speed = player.horizontalSpeed * strength)
        }
    }

}
