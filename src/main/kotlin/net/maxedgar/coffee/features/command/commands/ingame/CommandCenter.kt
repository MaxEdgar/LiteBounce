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
package net.maxedgar.coffee.features.command.commands.ingame

import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.EventState
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.maxedgar.coffee.utils.math.center

/**
 * Center command
 *
 * Centers you at your current position.
 */
object CommandCenter : Command.Factory, EventListener {

    var state = CenterHandlerState.INACTIVE

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("center")
            .requiresIngame()
            .handler { state = CenterHandlerState.APPLY_ON_NEXT_EVENT }
            .build()
    }

    @Suppress("unused")
    private val moveHandler =
        handler<PlayerNetworkMovementTickEvent>(priority = EventPriorityConvention.SAFETY_FEATURE) {
            if (it.state == EventState.POST) {
                return@handler
            }

            val pos = player.blockPosition().center
            val delta = player.position().subtract(pos)
            it.x = delta.x
            it.y = delta.y
            it.z = delta.z
            state = CenterHandlerState.INACTIVE
        }

    override val running: Boolean
        get() = super.running && inGame && state == CenterHandlerState.APPLY_ON_NEXT_EVENT

    enum class CenterHandlerState {
        INACTIVE,
        APPLY_ON_NEXT_EVENT
    }

}
