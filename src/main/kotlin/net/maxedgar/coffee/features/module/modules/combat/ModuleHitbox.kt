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
package net.maxedgar.coffee.features.module.modules.combat

import net.maxedgar.coffee.event.events.EntityMarginEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.combat.shouldBeAttacked

/**
 * Hitbox module
 *
 * Enlarges the hitbox of other entities.
 */
object ModuleHitbox : ClientModule("Hitbox", ModuleCategories.COMBAT) {

    val size by float("Size", 0.1f, 0f..1f).apply { tagBy(this) }

    val applyToDebugHitbox by boolean("ApplyToDebugHitbox", true)

    /**
     * Apply to [net.minecraft.world.item.component.AttackRange.hitboxMargin]
     */
    val applyToComponent by boolean("ApplyToComponent", true)

    @Suppress("unused")
    private val marginHandler = handler<EntityMarginEvent> { event ->
        if (event.entity.shouldBeAttacked()) {
            event.margin = size
        }
    }

}
