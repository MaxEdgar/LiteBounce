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
package net.maxedgar.coffee.features.command.commands.module

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.modules
import net.maxedgar.coffee.features.command.preset.pagedQuery
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.modules.world.ModuleAutoDisable
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.text.asText
import net.maxedgar.coffee.utils.client.bold
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.client.withColor
import net.minecraft.ChatFormatting

/**
 * AutoDisable Command
 *
 * Allows you to manage the list of modules that are automatically disabled.
 * It provides subcommands to add, remove, list and clear modules from the auto-disable list.
 *
 * Module: [ModuleAutoDisable]
 */
object CommandAutoDisable : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("autodisable")
            .hub()
            .subcommand(addSubcommand())
            .subcommand(removeSubcommand())
            .subcommand(listSubcommand())
            .subcommand(clearSubcommand())
            .build()
    }

    private fun clearSubcommand() = CommandBuilder
        .begin("clear")
        .handler {
            ModuleAutoDisable.clear()
            chat(
                command.result("modulesCleared"),
                metadata = MessageMetadata(id = "CAutoDisable#global")
            )
        }
        .build()

    private fun listSubcommand() = CommandBuilder
        .begin("list")
        .pagedQuery(
            pageSize = 8,
            header = {
                result("modules").withColor(ChatFormatting.RED).bold(true)
            },
            items = {
                ModuleAutoDisable.modules
            },
            eachRow = { _, module ->
                "\u2B25 ".asText()
                    .withStyle(ChatFormatting.BLUE)
                    .append(variable(module.name).copyable())
                    .append(regular(" ("))
                    .append(variable(module.bind.keyName).copyable())
                    .append(regular(")"))
            }
        )

    private fun removeSubcommand() = CommandBuilder
        .begin("remove")
        .parameter(
            ParameterBuilder.modules(all = ModuleAutoDisable.modules)
                .required()
                .build()
        )
        .handler {
            val modules = args[0] as Set<ClientModule>

            modules.forEach { module ->
                if (!ModuleAutoDisable.remove(module)) {
                    throw CommandException(command.result("moduleNotPresent", module.name))
                }

                chat(
                    regular(
                        command.result(
                            "moduleRemoved",
                            variable(module.name)
                        )
                    ),
                    command
                )
            }
        }
        .build()

    private fun addSubcommand() = CommandBuilder
        .begin("add")
        .parameter(
            ParameterBuilder.modules()
                .required()
                .build()
        )
        .handler {
            val modules = args[0] as Set<ClientModule>

            modules.forEach { module ->
                if (!ModuleAutoDisable.add(module)) {
                    throw CommandException(command.result("moduleIsPresent", module.name))
                }

                chat(regular(command.result("moduleAdded", variable(module.name))), command)
            }
        }
        .build()

}
