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

package net.maxedgar.coffee.features.module.modules.world.nuker.area

import net.maxedgar.coffee.features.module.modules.world.nuker.ModuleNuker.wasTarget
import net.maxedgar.coffee.utils.block.searchBlocksInCuboid
import net.maxedgar.coffee.utils.entity.box
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

object SphereNukerArea : NukerArea("Sphere") {

    override fun lookupTargets(radius: Float, count: Int?): List<Pair<BlockPos, BlockState>> {
        val rangeSquared = (radius * radius).toDouble()
        val eyesPos = player.eyePosition

        val positions = eyesPos.searchBlocksInCuboid(radius) { pos, state ->
            isPositionAvailable(eyesPos, rangeSquared, pos, state)
        }.toMutableList()

        positions.sortBy { (pos, _) ->
            // If there is a last target, sort by distance to it, otherwise go by hardness
            pos.distSqr(wasTarget ?: player.blockPosition())
        }

        val boundingBox = player.box.move(0.0, -1.0, 0.0)
        val nonStandingPositions = positions.filter { (pos, _) ->
            !boundingBox.intersects(pos)
        }

        // If there are more than one target, we should remove blocks that we are standing on
        val list = nonStandingPositions.ifEmpty { positions }

        return if (count != null) {
            list.take(count)
        } else {
            list
        }
    }

}
