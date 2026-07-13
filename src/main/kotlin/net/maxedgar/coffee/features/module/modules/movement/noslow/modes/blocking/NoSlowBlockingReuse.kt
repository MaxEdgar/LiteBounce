/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

internal object NoSlowBlockingReuse : Mode("Reuse") {

    override val parent: ModeValueGroup<Mode>
        get() = modes

    private val timingMode by enumChoice("Timing", TimingMode.PRE_POST)

    @Suppress("unused")
    private val networkTickHandler = handler<PlayerNetworkMovementTickEvent> { event ->
        blockingHand?.let { blockingHand ->
            when (timingMode) {
                TimingMode.PRE_TICK -> {
                    if (event.state == EventState.PRE) {
                        untracked {
                            network.send(
                                ServerboundPlayerActionPacket(
                                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
                            ))
                            interaction.startPrediction(world) { sequence ->
                                ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                            }
                        }
                    }
                }

                TimingMode.POST_TICK -> {
                    if (event.state == EventState.POST) {
                        untracked {
                            network.send(
                                ServerboundPlayerActionPacket(
                                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
                            ))
                            interaction.startPrediction(world) { sequence ->
                                ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                            }
                        }
                    }
                }

                TimingMode.PRE_POST -> {
                    when (event.state) {
                        EventState.PRE -> {
                            untracked {
                                network.send(
                                    ServerboundPlayerActionPacket(
                                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
                                ))
                            }
                        }

                        EventState.POST -> {
                            untracked {
                                interaction.startPrediction(world) { sequence ->
                                    ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
                                }
                            }
                        }
                    }
                }
            }
        }


        blockingHand?.let { blockingHand ->
            when (event.state) {
                EventState.PRE -> {

                }

                EventState.POST -> {
                    untracked {
                        interaction.startPrediction(world) { sequence ->
                            ServerboundUseItemPacket(blockingHand, sequence, player.yRot, player.xRot)
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
