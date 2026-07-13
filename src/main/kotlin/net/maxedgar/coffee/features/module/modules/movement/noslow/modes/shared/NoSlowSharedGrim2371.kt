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
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.minecraft.network.protocol.game.ServerboundUseItemPacket

/**
 * Bypassing Grim 2.3.71
 * @from https://github.com/GrimAnticheat/Grim/issues/2216
 */
internal class NoSlowSharedGrim2371(override val parent: ModeValueGroup<*>) : Mode("Grim2371") {

    companion object {
        @JvmStatic
        var shouldPreventNoSlow = false
            private set
    }

    @Suppress("unused")
    private val tickHandler = tickHandler(onCancellation = { shouldPreventNoSlow = false }) {
        repeat(2) {
            waitTicks(1)
            shouldPreventNoSlow = false
            interaction.startPrediction(world) { sequence ->
                ServerboundUseItemPacket(
                    player.usedItemHand, sequence,
                    player.yRot, player.xRot
                )
            }
        }

        shouldPreventNoSlow = true
    }

    override fun disable() {
        shouldPreventNoSlow = false
        super.disable()
    }

}
