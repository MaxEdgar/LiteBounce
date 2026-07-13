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

package net.maxedgar.coffee.features.module.modules.movement.fly.modes.vulcan

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly.modes

/**
 * @anticheat Vulcan
 * @anticheat Version 2.7.7
 * @testedOn anticheat-test.com
 * @note NA
 */
internal object FlyVulcan277 : Mode("Vulcan277") {

    override val parent: ModeValueGroup<*>
        get() = modes

    val repeatable = tickHandler {
        if (player.fallDistance > 0.1) {
            if (player.tickCount % 2 == 0) {
                player.deltaMovement.y = -0.155
            } else {
                player.deltaMovement.y = -0.1
            }
        }
    }

}

