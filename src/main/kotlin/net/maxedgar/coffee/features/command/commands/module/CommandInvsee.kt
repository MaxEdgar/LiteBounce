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
package net.maxedgar.coffee.features.command.commands.module

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.playerName
import net.maxedgar.coffee.features.module.modules.misc.ModuleInventoryTracker
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.network
import net.maxedgar.coffee.utils.client.world
import net.maxedgar.coffee.utils.inventory.ViewedInventoryScreen
import java.util.UUID

/**
 * Command Invsee
 *
 * ???
 *
 * Module: [ModuleInventoryTracker]
 */
object CommandInvsee : Command.Factory {

    var viewedPlayer: UUID? = null

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("invsee")
            .requiresIngame()
            .parameter(
                ParameterBuilder.playerName()
                    .required()
                    .build()
            )
            .handler {
                val inputName = args[0] as String
                val playerID = network.onlinePlayers.find { it.profile.name.equals(inputName, true) }?.profile?.id
                val player = { playerID?.let(world::getPlayerByUUID) ?: ModuleInventoryTracker.playerMap[playerID] }

                if (playerID == null || player() == null) {
                    throw CommandException(command.result("playerNotFound", inputName))
                }

                mc.schedule {
                    mc.gui.setScreen(ViewedInventoryScreen(player))
                }

                viewedPlayer = playerID
            }
            .build()
    }
}
