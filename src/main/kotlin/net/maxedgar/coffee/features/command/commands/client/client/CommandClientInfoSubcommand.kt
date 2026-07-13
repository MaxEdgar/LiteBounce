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
package net.maxedgar.coffee.features.command.commands.client.client

import net.maxedgar.coffee.Coffee
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable

object CommandClientInfoSubcommand {
    fun infoCommand() = CommandBuilder
        .begin("info")
        .handler {
            chat(
                regular(command.result("clientName", variable(Coffee.CLIENT_NAME))),
                metadata = MessageMetadata(prefix = false)
            )
            chat(
                regular(command.result("clientVersion", variable(Coffee.clientVersion))),
                metadata = MessageMetadata(prefix = false)
            )
            chat(
                regular(command.result("clientAuthor", variable(Coffee.CLIENT_AUTHOR))),
                metadata = MessageMetadata(prefix = false)
            )
        }.build()
}
