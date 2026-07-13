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

package net.maxedgar.coffee.features.module.modules.world.scaffold.tower

import net.maxedgar.coffee.event.events.PlayerJumpEvent
import net.maxedgar.coffee.event.sequenceHandler
import net.maxedgar.coffee.event.tickUntil
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold.isBlockBelow
import net.maxedgar.coffee.utils.client.Timer
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention.READ_FINAL_STATE
import net.maxedgar.coffee.utils.kotlin.Priority

object ScaffoldTowerKarhu : ScaffoldTower("Karhu") {

    private val timerSpeed by float("Timer", 5f, 0.1f..10f)
    private val triggerMotion by float("Trigger", 0.06f, 0.0f..0.2f, "Y/v")
    private val pulldown by boolean("Pulldown", true)

    @Suppress("unused")
    private val jumpHandler = sequenceHandler<PlayerJumpEvent>(priority = READ_FINAL_STATE) { event ->
        if (event.motion == 0f || event.isCancelled) {
            return@sequenceHandler
        }

        tickUntil { !player.onGround() }
        Timer.requestTimerSpeed(timerSpeed, Priority.IMPORTANT_FOR_USAGE_1, ModuleScaffold)

        if (pulldown) {
            tickUntil { !player.onGround() && player.deltaMovement.y < triggerMotion }

            if (!isBlockBelow) {
                return@sequenceHandler
            }

            player.deltaMovement.y -= 1f
        }
    }

}
