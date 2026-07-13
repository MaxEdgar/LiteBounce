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

package net.maxedgar.coffee.features.module.modules.render.murdermystery

import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.entity.handItems
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.Items

object MurderMysteryInfectionMode : UuidBasedMurderMysteryMode("Infection") {

    val tickHandler = handler<GameTickEvent> {
        world.players()
            .filter {
                it.isUsingItem && player.handItems.any { stack -> stack.item is BowItem } ||
                    player.handItems.any { stack -> stack.item == Items.ARROW }
            }
            .forEach { playerEntity ->
                handleHasBow(playerEntity)
            }
    }

    override fun handleHasSword(entity: AbstractClientPlayer) {
        if (murdererPlayers.add(entity.gameProfile.id) && murdererPlayers.size == 1) {
            chat(entity.gameProfile.name + " is the first infected.")

            ModuleMurderMystery.playHurt = true
        }
    }

    override fun disallowsArrowDodge(): Boolean {
        // Don't dodge if we are not dead yet.
        return currentPlayerType == MurderMysteryMode.PlayerType.DETECTIVE_LIKE
    }

}
