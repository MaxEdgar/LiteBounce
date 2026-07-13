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

package net.maxedgar.coffee.features.module.modules.movement.noslow.modes.blocking

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.BlinkPacketEvent
import net.maxedgar.coffee.event.events.TransferOrigin
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.blink.BlinkManager
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.blocking.NoSlowBlock.modes
import net.maxedgar.coffee.utils.entity.isBlockAction
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

internal object NoSlowBlockingBlink : Mode("Blink") {

    override val parent: ModeValueGroup<Mode>
        get() = modes

    @Suppress("unused")
    private val fakeLagHandler = handler<BlinkPacketEvent> { event ->
        if (event.origin != TransferOrigin.OUTGOING || !player.isBlockAction) {
            return@handler
        }

        event.action = if (event.packet is ServerboundMovePlayerPacket) {
             BlinkManager.Action.QUEUE
        } else if (event.action == BlinkManager.Action.FLUSH) {
            BlinkManager.Action.PASS
        } else {
            return@handler
        }
    }

}
