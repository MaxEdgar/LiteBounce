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
package net.maxedgar.coffee.features.module.modules.movement

import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.entity.PlayerSimulationCache
import net.maxedgar.coffee.utils.entity.moving

/**
 * Parkour module
 *
 * Automatically jumps at the very edge of a block.
 */
object ModuleParkour : ClientModule("Parkour", ModuleCategories.MOVEMENT) {

    @Suppress("unused")
    private val simulatedTickHandler = handler<MovementInputEvent> { event ->
        val simulatedPlayer = PlayerSimulationCache.getSimulationForLocalPlayer()
        val shouldJump = player.moving &&
                player.onGround() &&
                !player.isShiftKeyDown &&
                !mc.options.keyShift.isDown &&
                !mc.options.keyJump.isDown &&
                !simulatedPlayer.getSnapshotAt(1).onGround

        if (shouldJump) {
            event.jump = true
        }
    }

}
