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
import net.maxedgar.coffee.features.command.builder.block
import net.maxedgar.coffee.features.command.preset.pagedQuery
import net.maxedgar.coffee.features.module.modules.render.ModuleXRay
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.bold
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.client.withColor
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block

/**
 * XRay Command
 *
 * Allows you to add, remove, list, clear, and reset blocks for the XRay module.
 *
 * Module: [ModuleXRay]
 */
object CommandXRay : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("xray")
            .hub()
            .subcommand(andSubcommand())
            .subcommand(removeSubcommand())
            .subcommand(listSubcommand())
            .subcommand(clearSubcommand())
            .subcommand(resetSubcommand())
            .build()
    }

    private fun resetSubcommand() = CommandBuilder
        .begin("reset")
        .handler {
            ModuleXRay.applyDefaults()
            chat(
                regular(command.result("Reset the blocks to the default values")),
                metadata = MessageMetadata(id = "CXRay#global")
            )
        }
        .build()

    private fun clearSubcommand() = CommandBuilder
        .begin("clear")
        .handler {
            ModuleXRay.blocks.clear()
            chat(
                regular(command.result("blocksCleared")),
                metadata = MessageMetadata(id = "CXRay#global")
            )
        }
        .build()

    private fun listSubcommand() = CommandBuilder
        .begin("list")
        .pagedQuery(
            pageSize = 8,
            header = { result("list").withColor(ChatFormatting.RED).bold(true) },
            items = {
                ModuleXRay.blocks.sortedBy { it.descriptionId }
            },
            eachRow = { _, block ->
                regular("\u2B25 ")
                    .append(variable(block.name).copyable())
                    .append(regular(" ("))
                    .append(variable(BuiltInRegistries.BLOCK.getKey(block).toString()).copyable())
                    .append(regular(")"))
            }
        )

    private fun removeSubcommand() = CommandBuilder
        .begin("remove")
        .parameter(
            ParameterBuilder.block()
                .required()
                .build()
        )
        .handler {
            val block = args[0] as Block
            if (!ModuleXRay.blocks.remove(block)) {
                throw CommandException(command.result("blockNotFound", block.name))
            }

            chat(
                regular(command.result("blockRemoved", block.name)),
                metadata = MessageMetadata(id = "CXRay#info")
            )
        }
        .build()

    private fun andSubcommand() = CommandBuilder
        .begin("add")
        .parameter(
            ParameterBuilder.block()
                .required()
                .build()
        )
        .handler {
            val block = args[0] as Block
            if (!ModuleXRay.blocks.add(block)) {
                throw CommandException(command.result("blockIsPresent", block.name))
            }

            chat(
                regular(command.result("blockAdded", block.name)),
                metadata = MessageMetadata(id = "CXRay#info")
            )
        }
        .build()

}
