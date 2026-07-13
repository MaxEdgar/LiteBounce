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

package net.maxedgar.coffee.event.events

import io.netty.channel.ChannelPipeline
import net.maxedgar.coffee.annotations.Tag
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.CancellableEvent
import net.maxedgar.coffee.event.Event
import net.maxedgar.coffee.features.blink.BlinkManager
import net.minecraft.network.protocol.Packet

@Tag("pipeline")
class PipelineEvent(val channelPipeline: ChannelPipeline, val local: Boolean) : Event()

@Tag("packet")
class PacketEvent(val origin: TransferOrigin, val packet: Packet<*>, val original: Boolean = true) : CancellableEvent()

@Tag("queuePacket")
class BlinkPacketEvent(
    val packet: Packet<*>?,
    val origin: TransferOrigin
) : Event() {

    var action: BlinkManager.Action = BlinkManager.Action.FLUSH
        set(value) {
            if (field == value || field.priority >= value.priority) {
                return
            }

            field = value
        }

}

enum class TransferOrigin(override val tag: String) : Tagged {
    INCOMING("Incoming"),
    OUTGOING("Outgoing");
}
