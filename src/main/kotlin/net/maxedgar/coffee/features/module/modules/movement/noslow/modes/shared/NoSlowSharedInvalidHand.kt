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
package net.maxedgar.coffee.features.module.modules.movement.noslow.modes.shared

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.TransferOrigin
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.utils.client.NullableBypass
import net.maxedgar.coffee.utils.network.sendPacketSilently
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

internal class NoSlowSharedInvalidHand(override val parent: ModeValueGroup<*>) : Mode("InvalidHand") {

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent>(priority = EventPriorityConvention.READ_FINAL_STATE) { event ->
        val packet = event.packet

        if (!event.isCancelled && event.origin == TransferOrigin.OUTGOING && packet is ServerboundUseItemPacket) {
            event.cancelEvent()
            sendPacketSilently(NullableBypass.createWithNullHand(packet))
        }
    }

}
