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

package net.maxedgar.coffee.features.module.modules.movement.longjump.modes.nocheatplus

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.PlayerMoveEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.modules.movement.longjump.ModuleLongJump
import net.maxedgar.coffee.utils.entity.moving
import net.maxedgar.coffee.utils.math.multiply
import net.maxedgar.coffee.utils.movement.stopXZVelocity

/**
 * @anticheat NoCheatPlus
 * @anticheatVersion 3.16.1-SNAPSHOT-sMD5NET-b115s
 * @testedOn eu.loyisa.cn
 */
internal object NoCheatPlusBoost : Mode("NoCheatPlusBoost") {
    override val parent: ModeValueGroup<*>
        get() = ModuleLongJump.mode

    val ncpBoost by float("NCPBoost", 4.25f, 1f..10f)

    val repeatable = tickHandler {
        if (ModuleLongJump.canBoost) {
            player.deltaMovement =
                player.deltaMovement.multiply(factorX = ncpBoost, factorZ = ncpBoost)
            ModuleLongJump.boosted = true
        }
        ModuleLongJump.canBoost = false
    }

    val moveHandler = handler<PlayerMoveEvent> {
        if (!player.moving && ModuleLongJump.jumped) {
            player.stopXZVelocity()
        }
    }
}
