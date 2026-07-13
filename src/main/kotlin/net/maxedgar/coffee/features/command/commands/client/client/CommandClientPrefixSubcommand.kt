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

import net.maxedgar.coffee.features.command.CommandManager
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable

object CommandClientPrefixSubcommand {
    fun prefixCommand() = CommandBuilder.begin("prefix")
        .parameter(
            ParameterBuilder
                .begin<String>("prefix")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .required()
                .build()
        )
        .handler {
            val prefix = args[0] as String
            CommandManager.GlobalSettings.prefix = prefix
            chat(regular(command.result("prefixChanged", variable(prefix))))
        }
        .build()
}
