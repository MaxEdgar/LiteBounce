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

package net.maxedgar.coffee.features.module.modules.movement.fly.modes.grim

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.EventState
import net.maxedgar.coffee.event.events.BlinkPacketEvent
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.blink.BlinkManager.Action
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly.modes
import net.maxedgar.coffee.utils.entity.airTicks
import net.minecraft.network.protocol.common.ClientboundPingPacket
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket

/**
 * @anticheat Grim
 * @anticheatVersion 2.3.73-b7a719d
 *   https://modrinth.com/plugin/grimac/version/Eq05CMZ9
 *   January 15, 2026
 * @testedOn test.ccbluex.net
 */
object FlyGrim2373Jan15 : Mode("Grim2373Jan15") {

    override val parent: ModeValueGroup<*>
        get() = modes

    private val autoLag by boolean("AutoLagInAir", true)
    private val airTick by int("AirTick", 3, 0..12, "ticks")

    private var isStarted = false
    private var shouldDelay = false

    override fun disable() {
        isStarted = false
        shouldDelay = false
        super.disable()
    }

    @Suppress("unused")
    private val queuePacketHandler = handler<BlinkPacketEvent> { event ->
        val packet = event.packet
        if (packet is ClientboundSetEntityMotionPacket && packet.id == player.id) {
            shouldDelay = true
        }

        if (shouldDelay) {
            event.action = when (packet) {
                is ClientboundPingPacket -> Action.QUEUE
                is ClientboundPlayerPositionPacket -> Action.FLUSH
                else -> Action.PASS
            }
        }
    }

    @Suppress("unused")
    private val motionHandler = handler<PlayerNetworkMovementTickEvent> { event ->
        if (event.state == EventState.POST) {
            if (isStarted) {
                sendFallFlying()
            }
            return@handler
        }

        if (isStarted) {
            return@handler
        }

        if (autoLag && player.airTicks >= airTick || shouldDelay) {
            isStarted = true
            sendFallFlying()
        }
    }

    private fun sendFallFlying() {
        val packet = ServerboundPlayerCommandPacket(
            player,
            ServerboundPlayerCommandPacket.Action.START_FALL_FLYING
        )

        network.send(packet)
    }

}

