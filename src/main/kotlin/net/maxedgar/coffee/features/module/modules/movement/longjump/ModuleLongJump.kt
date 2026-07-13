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
package net.maxedgar.coffee.features.module.modules.movement.longjump

import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PlayerJumpEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.movement.longjump.modes.Matrix7145FlagLongJump
import net.maxedgar.coffee.features.module.modules.movement.longjump.modes.VulcanLongJump
import net.maxedgar.coffee.features.module.modules.movement.longjump.modes.nocheatplus.NoCheatPlusBoost
import net.maxedgar.coffee.features.module.modules.movement.longjump.modes.nocheatplus.NoCheatPlusBow
import net.maxedgar.coffee.utils.entity.moving

object ModuleLongJump : ClientModule("LongJump", ModuleCategories.MOVEMENT) {

    val mode = choices(
        "Mode", NoCheatPlusBoost, arrayOf(
            // NoCheatPlus
            NoCheatPlusBoost,
            NoCheatPlusBow,
            VulcanLongJump,
            Matrix7145FlagLongJump
        )
    ).apply { tagBy(this) }
    private val autoJump by boolean("AutoJump", false)
    val autoDisable by boolean("DisableAfterFinished", false)

    var jumped = false
    var canBoost = false
    var boosted = false

    val tickHandler = handler<MovementInputEvent> {
        if (jumped) {
            if (player.onGround() || player.abilities.flying) {
                if (autoDisable && boosted) {
                    enabled = false
                }

                jumped = false
            }
        }

        // AutoJump
        if (autoJump && player.onGround() && player.moving
            && mode.activeMode != NoCheatPlusBow) {
            player.jumpFromGround()
            jumped = true
        }
    }

    @Suppress("unused")
    val manualJumpHandler = handler<PlayerJumpEvent> {
        jumped = true
        canBoost = true
    }
}
