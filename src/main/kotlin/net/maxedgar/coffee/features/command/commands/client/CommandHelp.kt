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
package net.maxedgar.coffee.features.command.commands.client

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandManager
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.preset.pagedQuery
import net.maxedgar.coffee.lang.translation
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.text.asText
import net.maxedgar.coffee.utils.client.bold
import net.maxedgar.coffee.utils.client.onClick
import net.maxedgar.coffee.utils.client.onHover
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.withColor
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

/**
 * Help Command
 *
 * Provides a help page for displaying other commands.
 */
object CommandHelp : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("help")
            .pagedQuery(
                pageSize = 8,
                header = {
                    result("help").withColor(ChatFormatting.RED).bold(true)
                },
                items = {
                    CommandManager.sortedBy { it.name }
                },
                eachRow = { _, command ->
                    val commandStart = CommandManager.GlobalSettings.prefix + command.name
                    "\u2B25 ".asText()
                        .withStyle(ChatFormatting.BLUE)
                        .onHover(
                            HoverEvent.ShowText(
                                translation("liquidbounce.command.${command.name}.description")
                            )
                        )
                        .append(
                            commandStart.asText()
                                .withStyle(ChatFormatting.GRAY)
                                .onClick(ClickEvent.SuggestCommand(commandStart))
                        )
                        .append(buildAliasesText(command))
                }
            )
    }

    private fun buildAliasesText(cmd: Command): Component = buildList {
        if (cmd.aliases.isNotEmpty()) {
            cmd.aliases.forEach { alias ->
                this += ", ".asPlainText(ChatFormatting.DARK_GRAY)
                this += regular(alias).withStyle(ChatFormatting.GRAY)
                    .onClick(ClickEvent.SuggestCommand(CommandManager.GlobalSettings.prefix + alias))
            }
        }
    }.asText()

}
