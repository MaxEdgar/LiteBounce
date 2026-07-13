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
import net.maxedgar.coffee.event.EventState
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.blocking.NoSlowBlock.modes
import net.maxedgar.coffee.utils.client.InteractionTracker.blockingHand
import net.maxedgar.coffee.utils.client.InteractionTracker.untracked
import net.maxedgar.coffee.utils.network.sendHeldItemChange
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

internal object NoSlowBlockingInteract : Mode("Interact") {

    override val parent: ModeValueGroup<Mode>
        get() = modes

    @Suppress("unused")
    val onNetworkTick = handler<PlayerNetworkMovementTickEvent> { event ->
        blockingHand?.let { blockingHand ->
            if (event.state == EventState.POST) {
                untracked {
                    network.sendHeldItemChange(player.inventory.selectedSlot)
                    interaction.startPrediction(world) { sequence ->
                        ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                    }
                }
            }
        }
    }

}
