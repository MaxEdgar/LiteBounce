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
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.EventState
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.blocking.NoSlowBlock.modes
import net.maxedgar.coffee.utils.client.InteractionTracker.blockingHand
import net.maxedgar.coffee.utils.client.InteractionTracker.untracked
import net.maxedgar.coffee.utils.network.sendHeldItemChange
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

internal object NoSlowBlockingSwitch : Mode("Switch") {

    override val parent: ModeValueGroup<Mode>
        get() = modes

    private val timingMode by enumChoice("Timing", TimingMode.PRE_POST)

    @Suppress("unused")
    val onNetworkTick = handler<PlayerNetworkMovementTickEvent> { event ->
        // This should if done correctly only work with main-hand blocking.
        // But as we know from experience often things are not done correctly on anti-cheats.
        // Main-hand blocking only applies when using VFP 1.8 client-side protocol translation.

        blockingHand?.let { blockingHand ->
            when (timingMode) {
                TimingMode.PRE_TICK -> {
                    if (event.state == EventState.PRE) {
                        untracked {
                            network.send(
                                ServerboundSetCarriedItemPacket(
                                (player.inventory.selectedSlot + 1) % 8)
                            )
                            network.sendHeldItemChange(player.inventory.selectedSlot)

                            // For some reason we do not have to re-interact with the item to start blocking again.
                            // The server will still think we are blocking.
                        }
                    }
                }
                TimingMode.POST_TICK -> {
                    if (event.state == EventState.POST) {
                        untracked {
                            network.send(
                                ServerboundSetCarriedItemPacket(
                                (player.inventory.selectedSlot + 1) % 8)
                            )
                            network.sendHeldItemChange(player.inventory.selectedSlot)

                            // For some reason we do not have to re-interact with the item to start blocking again.
                            // The server will still think we are blocking.
                        }
                    }
                }

                /**
                 * On PreAndPost, we first switch to the off-hand slot, then back to the main-hand slot and
                 * start blocking again.
                 */
                TimingMode.PRE_POST -> {
                    when (event.state) {
                        EventState.PRE -> {
                            untracked {
                                network.send(
                                    ServerboundSetCarriedItemPacket(
                                    (player.inventory.selectedSlot + 1) % 8)
                                )
                            }
                        }

                        EventState.POST -> {
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
        }
    }

    private enum class TimingMode(override val tag: String) : Tagged {
        PRE_POST("PreAndPost"),
        PRE_TICK("Pre"),
        POST_TICK("Post")
    }

}
