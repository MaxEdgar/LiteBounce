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
package net.maxedgar.coffee.features.module.modules.player.nofall.modes

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * SpoofGround mode for the NoFall module.
 * This mode spoofs the 'onGround' flag in PlayerMoveC2SPacket to prevent fall damage.
 */
internal object NoFallSpoofGround : NoFallMode("SpoofGround") {
    private val fallDistance = modes("FallDistance", Smart, arrayOf(Smart, Constant))
    private val resetFallDistance by boolean("ResetFallDistance", true)

    // Packet handler to intercept and modify PlayerMoveC2SPacket
    val packetHandler = handler<PacketEvent> {
        // Retrieve the packet from the event
        val packet = it.packet

        // Check if the packet is a PlayerMoveC2SPacket
        if (packet is ServerboundMovePlayerPacket && player.fallDistance >= fallDistance.activeMode.value) {
            // Modify the 'onGround' flag to true, preventing fall damage
            packet.onGround = true
            if (resetFallDistance) {
                player.resetFallDistance()
            }
        }
    }

    private abstract class DistanceMode(name: String) : Mode(name) {
        override val parent: ModeValueGroup<*>
            get() = fallDistance

        abstract val value: Float
    }

    private object Smart : DistanceMode("Smart") {
        override val value: Float
            get() = playerSafeFallDistance.toFloat()
    }

    private object Constant : DistanceMode("Constant") {
        override val value by float("Value", 1.7f, 0f..5f)
    }
}
