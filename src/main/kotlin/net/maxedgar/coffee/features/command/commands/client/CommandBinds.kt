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

import com.mojang.blaze3d.platform.InputConstants
import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.CommandExecutor
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.enumChoice
import net.maxedgar.coffee.features.command.builder.module
import net.maxedgar.coffee.features.command.builder.modules
import net.maxedgar.coffee.features.command.preset.pagedQuery
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleManager
import net.maxedgar.coffee.features.module.modules.render.ModuleClickGui
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.text.asText
import net.maxedgar.coffee.utils.client.bold
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.client.highlight
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.onClickRun
import net.maxedgar.coffee.utils.client.onHover
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.client.withColor
import net.maxedgar.coffee.utils.input.InputBind
import net.maxedgar.coffee.utils.input.availableInputKeys
import net.maxedgar.coffee.utils.input.bind
import net.maxedgar.coffee.utils.input.inputByName
import net.maxedgar.coffee.utils.input.renderText
import net.maxedgar.coffee.utils.input.unbind
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.HoverEvent

/**
 * Binds Command
 *
 * Allows you to manage the bindings of modules to keys.
 * It provides subcommands to add, remove, list and clear bindings.
 */
object CommandBinds : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("binds")
            .hub()
            .subcommand(addSubcommand)
            .subcommand(removeSubcommand)
            .subcommand(listSubcommand)
            .subcommand(clearSubcommand)
            .build()
    }

    private val clearSubcommand = CommandBuilder
        .begin("clear")
        .handler {
            ModuleManager.forEach { it.bindValue.unbind() }
            chat(command.result("bindsCleared"), metadata = MessageMetadata(id = "Binds#global"))
        }
        .build()

    private val listSubcommand = CommandBuilder
        .begin("list")
        .pagedQuery(
            pageSize = 8,
            header = {
                result("bindings").withColor(ChatFormatting.RED).bold(true)
            },
            items = {
                ModuleManager.filter { !it.bind.isUnbound }
            },
            eachRow = { _, module ->
                val bind = module.bind
                "\u2B25 ".asText()
                    .withStyle(ChatFormatting.BLUE)
                    .append(
                        markAsError("[\u2715] ")
                            .onHover(
                                HoverEvent.ShowText(
                                    "Unbind ".asText().append(variable(module.name))
                                )
                            )
                            .onClickRun {
                                runCatching {
                                    handleRemoveBind(setOf(module))
                                }.onFailure(CommandExecutor::handleExceptions)
                            }
                    )
                    .append(highlight(module.name).copyable())
                    .append(regular(": "))
                    .append(bind.renderText())
            }
        )

    private fun handleRemoveBind(modules: Set<ClientModule>) {
        modules.forEach { module ->
            if (module.bind.isUnbound) {
                throw CommandException(removeSubcommand.result("moduleNotBound"))
            }

            module.bindValue.unbind()

            chat(
                regular(removeSubcommand.result("bindRemoved", variable(module.name))),
                metadata = MessageMetadata(id = "Binds#${module.name}")
            )
        }

        ModuleClickGui.sync()
    }

    private val removeSubcommand = CommandBuilder
        .begin("remove")
        .parameter(
            ParameterBuilder.modules { mod -> !mod.bind.isUnbound }
                .required()
                .build()
        )
        .handler {
            val modules = args[0] as Set<ClientModule>
            handleRemoveBind(modules)
        }
        .build()

    private val addSubcommand = CommandBuilder
        .begin("add")
        .parameter(
            ParameterBuilder.module()
                .required()
                .build()
        ).parameter(
            ParameterBuilder
                .begin<String>("key")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedFrom { availableInputKeys }
                .required()
                .build()
        ).parameter(
            ParameterBuilder.enumChoice<InputBind.BindAction>("action")
                .optional()
                .build()
        )
        .parameter(
            ParameterBuilder.enumChoice<InputBind.Modifier>("modifiers")
                .vararg()
                .optional()
                .build()
        )
        .handler {
            val module = args[0] as ClientModule
            val keyName = args[1] as String
            val action = args.getOrNull(2) as InputBind.BindAction? ?: module.bind.action
            val modifiers = args.getOrNull(3) as Set<InputBind.Modifier>? ?: module.bind.modifiers

            val bindKey = inputByName(keyName)
            if (bindKey == InputConstants.UNKNOWN) {
                throw CommandException(command.result("unknownKey"))
            }

            module.bindValue.bind(bindKey, action, modifiers)
            ModuleClickGui.sync()
            chat(
                regular(
                    command.result("moduleBound", variable(module.name), module.bind.renderText())
                ), metadata = MessageMetadata(id = "Binds#${module.name}")
            )
        }
        .build()

}
