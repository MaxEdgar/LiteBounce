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
package net.maxedgar.coffee.features.command.commands.client

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.modules
import net.maxedgar.coffee.features.command.preset.pagedQuery
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleManager
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.text.asText
import net.maxedgar.coffee.utils.client.bold
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.text.joinToText
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.client.withColor
import net.minecraft.ChatFormatting

/**
 * Hide Command
 *
 * Allows you to hide specific modules.
 */
object CommandHide : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("hide")
            .hub()
            .subcommand(hideSubcommand())
            .subcommand(unhideSubcommand())
            .subcommand(listSubcommand())
            .subcommand(clearSubcommand())
            .build()
    }

    private fun clearSubcommand() = CommandBuilder
        .begin("clear")
        .handler {
            ModuleManager.forEach { it.hidden = false }
            chat(
                regular(command.result("modulesUnhidden")),
                metadata = MessageMetadata(id = "CHide#info")
            )
        }
        .build()

    private fun listSubcommand() = CommandBuilder
        .begin("list")
        .pagedQuery(
            pageSize = 8,
            header = {
                result("hidden").withColor(ChatFormatting.RED).bold(true)
            },
            items = {
                ModuleManager.filter { it.hidden }
            },
            eachRow = { _, module ->
                "\u2B25 ".asText()
                    .withStyle(ChatFormatting.BLUE)
                    .append(variable(module.name).copyable())
                    .append(regular(" ("))
                    .append(regular(result("hidden"))) // TODO: click to unhide?
                    .append(regular(")"))
            }
        )

    private fun unhideSubcommand() = CommandBuilder
        .begin("unhide")
        .parameter(
            ParameterBuilder.modules { it.hidden }
                .required()
                .build()
        )
        .handler {
            val modules = args[0] as Set<ClientModule>
            modules.forEach { it.hidden = false }

            chat(
                command.result(
                    "moduleUnhidden",
                    modules.map { variable(it.name) }.joinToText(", ".asPlainText())
                ),
                metadata = MessageMetadata(id = "CHide#info")
            )
        }
        .build()

    private fun hideSubcommand() = CommandBuilder
        .begin("hide")
        .parameter(
            ParameterBuilder.modules { !it.hidden }
                .required()
                .build()
        )
        .handler {
            val modules = args[0] as Set<ClientModule>
            modules.forEach { it.hidden = true }

            chat(
                command.result(
                    "moduleHidden",
                    modules.map { variable(it.name) }.joinToText(", ".asPlainText())
                ),
                metadata = MessageMetadata(id = "CHide#info")
            )
        }
        .build()

}
