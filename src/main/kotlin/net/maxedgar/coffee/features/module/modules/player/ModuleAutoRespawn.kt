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
package net.maxedgar.coffee.features.module.modules.player

import net.maxedgar.coffee.event.events.ScreenEvent
import net.maxedgar.coffee.event.sequenceHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.minecraft.client.gui.screens.DeathScreen

/**
 * AutoRespawn module
 *
 * Automatically respawns the player after dying.
 */
object ModuleAutoRespawn : ClientModule("AutoRespawn", ModuleCategories.PLAYER) {

    // There is a delay until the button is clickable on the death screen (20 ticks)
    private val delay by int("Delay", 0, 0..20, "ticks")

    val screenHandler = sequenceHandler<ScreenEvent> {
        if (it.screen is DeathScreen) {
            if (delay > 0) {
                waitTicks(delay)
            }

            player.respawn()
            mc.gui.setScreen(null)
        }
    }

}
