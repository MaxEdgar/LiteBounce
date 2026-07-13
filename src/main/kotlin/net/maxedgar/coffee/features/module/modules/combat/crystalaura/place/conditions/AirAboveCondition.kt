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
package net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.conditions

import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.CandidateCache
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.PlacementCondition
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.PlacementContext
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.SubmoduleCrystalPlacer
import net.maxedgar.coffee.utils.block.getState
import net.minecraft.core.BlockPos

/**
 * In 1.13+ crystals need one block air above to be placed.
 *
 * @see net.minecraft.world.item.EndCrystalItem
 */
object AirAboveCondition : PlacementCondition {

    override fun isValid(context: PlacementContext, cache: CandidateCache, candidate: BlockPos): Boolean {
        val state = cache.up.getState()!!
        return if (SubmoduleCrystalPlacer.oldVersion) {
            state.isAir || state.canBeReplaced()
        } else {
            state.isAir
        }
    }

}

/**
 * In 1.12.2 crystals need two blocks air above to be placed.
 *
 * [MCP940 net.minecraft.item.ItemEndCrystal](https://github.com/WangTingZheng/mcp940/blob/d0c030a4139ce7cf3f284b180f0d9ea87bdf8141/src/minecraft/net/minecraft/item/ItemEndCrystal.java#L30)
 */
object AirOldVersionCondition : PlacementCondition {

    override fun isValid(context: PlacementContext, cache: CandidateCache, candidate: BlockPos): Boolean {
        if (!SubmoduleCrystalPlacer.oldVersion) {
            return true
        }

        val state = candidate.above(2).getState()!!
        return state.isAir || state.canBeReplaced()
    }

}
