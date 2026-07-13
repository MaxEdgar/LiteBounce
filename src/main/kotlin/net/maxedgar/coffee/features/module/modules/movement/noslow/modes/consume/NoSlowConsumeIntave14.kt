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
package net.maxedgar.coffee.features.module.modules.movement.noslow.modes.consume

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.EventState
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.handler
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket

/**
 * tested on mineblaze.net
 */

internal class NoSlowConsumeIntave14(override val parent: ModeValueGroup<*>) : Mode("Intave14") {
    private val mode by enumChoice("Mode", Mode.RELEASE)

    private fun releasePacket() {
        network.send(
            ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                player.blockPosition(),
                Direction.UP
            )
        )
    }

    @Suppress("unused")
    private val onNetworkTick = handler<PlayerNetworkMovementTickEvent> { event ->
        if (event.state == EventState.PRE) {
            when (mode) {
                Mode.RELEASE -> {
                    if (player.isUsingItem) {
                        releasePacket()
                    }

                    if (player.ticksUsingItem == 3) {
                        player.releaseUsingItem()
                        releasePacket()
                    }
                }

                Mode.NEW -> {
                    if (player.ticksUsingItem <= 2 || player.useItemRemainingTicks == 0) {
                        releasePacket()
                    }
                }
            }
        }
    }

    private enum class Mode(override val tag: String) : Tagged {
        RELEASE("Release"),
        NEW("New")
    }
}
