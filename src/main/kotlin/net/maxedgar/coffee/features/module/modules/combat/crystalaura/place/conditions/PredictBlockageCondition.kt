/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
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

import net.maxedgar.coffee.features.module.modules.combat.crystalaura.PredictFeature
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.CandidateCache
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.PlacementCondition
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.PlacementContext
import net.maxedgar.coffee.utils.math.plus
import net.minecraft.core.BlockPos

/**
 * Uses the prediction to check if the future crystal will be blocked by players in the next ticks.
 */
object PredictBlockageCondition : PlacementCondition {

    override fun isValid(context: PlacementContext, cache: CandidateCache, candidate: BlockPos): Boolean {
        val up = cache.up
        return !PredictFeature.willBeBlocked(
            context.expectedCrystal + up,
            context.target,
            !cache.canPlace
        )
    }

}
