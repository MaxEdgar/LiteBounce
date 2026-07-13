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
import net.maxedgar.coffee.utils.aiming.utils.canSeeUpperBlockSide
import net.minecraft.core.BlockPos

/**
 * Depending on the settings, we only take upper block sides but is the checked side in range (and visible)?
 */
object OnlyAboveCondition : PlacementCondition {

    override fun isValid(context: PlacementContext, cache: CandidateCache, candidate: BlockPos): Boolean {
        return !SubmoduleCrystalPlacer.onlyAbove || canSeeUpperBlockSide(
            context.eyePos,
            candidate,
            context.range,
            context.wallsRange
        )
    }

}
