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

import com.google.common.base.Predicates
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.utils.block.stateOrEmpty
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
import net.minecraft.world.item.ItemUseAnimation
import java.util.function.Predicate

/**
 * Cancels block interactions allowing to bypass certain anti-cheats
 *
 * Tested on Watchdog-AntiCheat (hypixel.net)
 * Confirmed to be working on 25th of May 2024
 */
internal class NoSlowNoBlockInteract(
    parent: EventListener? = null,
    actionFilter: Predicate<ItemUseAnimation> = Predicates.alwaysTrue(),
) : ToggleableValueGroup(parent, "NoBlockInteract", true) {

    val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet is ServerboundUseItemOnPacket) {
            val useAction =
                player.getItemInHand(packet.hand).useAnimation
            val blockPos = packet.hitResult.blockPos

            // Check if we might click a block that is not air
            if (!blockPos.stateOrEmpty.isAir) {
                return@handler
            }

            if (actionFilter.test(useAction)) {
                event.cancelEvent()
            }
        }
    }

}
