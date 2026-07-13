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
package net.maxedgar.coffee.features.module.modules.world.scaffold.techniques.normal

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PlayerSafeWalkEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.world.scaffold.techniques.ScaffoldNormalTechnique
import net.maxedgar.coffee.utils.block.canStandOn
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention

object ScaffoldDownFeature : ToggleableValueGroup(ScaffoldNormalTechnique, "Down", false) {

    val handleMoveInput = handler<MovementInputEvent>(priority = EventPriorityConvention.OBJECTION_AGAINST_EVERYTHING) {
        if (shouldFallOffBlock()) {
            it.sneak = false
        }
    }

    @Suppress("unused")
    val handleSafeWalk = handler<PlayerSafeWalkEvent>(priority = EventPriorityConvention.OBJECTION_AGAINST_EVERYTHING) {
        if (shouldFallOffBlock()) {
            it.isSafeWalk = false
        }
    }

    val shouldGoDown: Boolean
        get() = enabled && mc.options.keyShift.isDown

    /**
     * When we are using the down scaffold, we want to jump down on the next block in some situations
     */
    internal fun shouldFallOffBlock() = shouldGoDown && player.blockPosition().offset(0, -2, 0).canStandOn()
}
