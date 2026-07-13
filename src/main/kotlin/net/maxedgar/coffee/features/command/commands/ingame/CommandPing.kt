/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
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

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable

/**
 * Ping Command
 *
 * Verifies the latency of the current player.
 */
object CommandPing : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("ping")
            .requiresIngame()
            .handler {
                val ping = requireNotNull(player.playerInfo?.latency) { "Player Info Is Null" }
                chat(regular(command.result("pingCheck", variable(ping.toString()))), command)
            }
            .build()
    }

}
