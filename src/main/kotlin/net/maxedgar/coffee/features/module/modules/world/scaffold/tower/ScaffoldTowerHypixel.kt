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
package net.maxedgar.coffee.features.module.modules.world.scaffold.tower

import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold.isBlockBelow
import net.maxedgar.coffee.utils.block.getCenterDistanceSquared
import net.maxedgar.coffee.utils.block.getState
import net.maxedgar.coffee.utils.entity.airTicks
import net.maxedgar.coffee.utils.entity.moving
import net.maxedgar.coffee.utils.entity.withStrafe
import net.minecraft.core.BlockPos
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.round

object ScaffoldTowerHypixel : ScaffoldTower("Hypixel") {

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (!mc.options.keyJump.isDown || ModuleScaffold.blockCount <= 0 || !isBlockBelow) {
            return@tickHandler
        }

        if (player.x % 1.0 != 0.0 && !player.moving) {
            player.deltaMovement.x = (round(player.x) - player.x).coerceAtMost(0.281)
        }

        if (player.airTicks > 14) {
            player.deltaMovement.y -= 0.09
            player.deltaMovement = player.deltaMovement.multiply(
                0.6,
                1.0,
                0.6
            )
            return@tickHandler
        }
        when (player.airTicks % 3) {
            0 -> {
                player.deltaMovement.y = 0.42
                player.deltaMovement =
                    player.deltaMovement.withStrafe(speed = 0.247 - (ThreadLocalRandom.current().nextFloat() / 100f))
            }
            2 -> player.deltaMovement.y = 1 - (player.y % 1.0)
        }
    }

    override fun getTargetedPosition(blockPos: BlockPos): BlockPos {
        if (!player.moving) {
            // Find the block closest to the player
            val blocks = arrayOf(
                blockPos.offset(0, 0, 1),
                blockPos.offset(0, 0, -1),
                blockPos.offset(1, 0, 0),
                blockPos.offset(-1, 0, 0)
            )

            val blockOffset = blocks.minByOrNull { blockPos ->
                blockPos.getCenterDistanceSquared()
            }?.below() ?: blockPos

            // Check if block next to the player is solid
            if (!blockOffset.getState()!!.isRedstoneConductor(world, blockOffset)) {
                return blockOffset
            }
        }

        return super.getTargetedPosition(blockPos)
    }


}
