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
package net.maxedgar.coffee.features.module.modules.player.antivoid.mode

import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.EventState
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.until
import net.maxedgar.coffee.features.module.modules.player.antivoid.ModuleAntiVoid

object AntiVoidFlagMode : AntiVoidMode("Flag") {

    private val fallDistance by float("FallDistance", 0.5f, 0.0f..6.0f)
    private val height by float("Height", 0.42f, 0.01f..10.0f)
    private val silent by boolean("Silent", false)

    override val parent: ModeValueGroup<*>
        get() = ModuleAntiVoid.mode

    override fun rescue(): Boolean {
        if (player.fallDistance >= fallDistance) {
            if (silent) {
                until<PlayerNetworkMovementTickEvent> { event ->
                    event.y += height

                    // The code above does nothing on POST
                    event.state == EventState.PRE
                }
            } else {
                player.setPos(player.position().add(0.0, height.toDouble(), 0.0))
            }
            return true
        }

        return false
    }

}
