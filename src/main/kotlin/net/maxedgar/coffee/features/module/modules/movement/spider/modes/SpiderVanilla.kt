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
package net.maxedgar.coffee.features.module.modules.movement.spider.modes

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.modules.movement.spider.ModuleSpider

internal object SpiderVanilla : Mode("Vanilla") {

    private val motion by float("Motion", 0.3F, 0.05f..0.8F)

    override val parent: ModeValueGroup<Mode>
        get() = ModuleSpider.modes

    val repeatable = tickHandler {
        if (player.horizontalCollision) {
            player.deltaMovement.y = motion.toDouble()
        }
    }
}
