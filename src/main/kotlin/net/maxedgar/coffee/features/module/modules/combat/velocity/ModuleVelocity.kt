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
package net.maxedgar.coffee.features.module.modules.combat.velocity

import net.maxedgar.coffee.event.EventManager
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.TransferOrigin
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.sequenceHandler
import net.maxedgar.coffee.event.tickUntil
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityAAC442
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityBlocksMC
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityDexland
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityGrim2344
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityGrim2371
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityHylex
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityHypixel
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityIntave
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityJumpReset
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityLag
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityModify
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityReduce
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityReversal
import net.maxedgar.coffee.features.module.modules.combat.velocity.mode.VelocityStrafe
import net.maxedgar.coffee.utils.network.isLocalPlayerVelocity
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

/**
 * Velocity module
 *
 * Modifies the amount of velocity you take.
 */

object ModuleVelocity : ClientModule("Velocity", ModuleCategories.COMBAT, aliases = listOf("AntiKnockBack")) {

    val modes = choices(
        "Mode", VelocityModify, arrayOf(
            // Generic modes
            VelocityModify,
            VelocityReversal,
            VelocityStrafe,
            VelocityJumpReset,
            VelocityLag,
            VelocityReduce,

            // Server modes
            VelocityHypixel,
            VelocityDexland,
            VelocityHylex,
            VelocityBlocksMC,

            // Anti cheat modes
            VelocityGrim2371,
            VelocityGrim2344,
            VelocityAAC442,
            VelocityIntave,
        )
    ).apply(::tagBy)

    private val delay by intRange("Delay", 0..0, 0..40, "ticks")
    private val pauseOnFlag by int("PauseOnFlag", 0, 0..20, "ticks")

    internal var pause = 0

    @Suppress("unused")
    private val pauseHandler = handler<GameTickEvent> {
        if (pause > 0) {
            pause--
        }
    }

    @Suppress("unused")
    private val packetHandler = sequenceHandler<PacketEvent>(priority = 1) { event ->
        val packet = event.packet

        if (!event.original || pause > 0) {
            return@sequenceHandler
        }

        if (packet.isLocalPlayerVelocity()) {
            // When delay is above 0, we will delay the velocity update
            if (delay.last > 0) {
                event.cancelEvent()

                delay.random().let { ticks ->
                    if (ticks > 0) {
                        val timeToWait = System.currentTimeMillis() + (ticks * 50L)

                        tickUntil { System.currentTimeMillis() >= timeToWait }
                    }
                }

                val packetEvent = PacketEvent(TransferOrigin.INCOMING, packet, false)
                EventManager.callEvent(packetEvent)

                if (!packetEvent.isCancelled) {
                    @Suppress("UNCHECKED_CAST")
                    (packet as Packet<ClientGamePacketListener>).handle(network)
                }
            }
        } else if (packet is ClientboundPlayerPositionPacket) {
            pause = pauseOnFlag
        }
    }

}
