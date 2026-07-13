/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.features.command.commands.client

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.module
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable

/**
 * Toggle Command
 *
 * Allows you to enable or disable a specific module.
 */
object CommandToggle : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("toggle")
            .alias("t")
            .parameter(
                ParameterBuilder.module()
                    .required()
                    .build()
            )
            .handler {
                val module = args[0] as ClientModule

                val isEnabled = !module.enabled
                module.enabled = isEnabled
                chat(
                    regular(
                        command.result(
                            "moduleToggled",
                            variable(module.name),
                            variable(if (isEnabled) command.result("enabled") else command.result("disabled"))
                        )
                    ),
                    metadata = MessageMetadata(id = "CToggle#success${module.name}")
                )
            }
            .build()
    }

}
