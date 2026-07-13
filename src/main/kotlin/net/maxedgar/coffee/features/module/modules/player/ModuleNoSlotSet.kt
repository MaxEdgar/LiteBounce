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

package net.maxedgar.coffee.features.module.modules.player

import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.network.sendHeldItemChange
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket

object ModuleNoSlotSet : ClientModule("NoSlotSet", ModuleCategories.PLAYER) {
    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        if (event.packet !is ClientboundSetHeldSlotPacket) {
            return@handler
        }

        event.cancelEvent()
        player.connection.sendHeldItemChange(SilentHotbar.serversideSlot)
    }
}
