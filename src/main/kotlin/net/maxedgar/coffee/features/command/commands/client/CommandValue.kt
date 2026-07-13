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

import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.valueGroupKeyPath
import net.maxedgar.coffee.features.command.builder.valueKeyPath
import net.maxedgar.coffee.features.command.builder.valueType
import net.maxedgar.coffee.features.module.modules.render.ModuleClickGui
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable

/**
 * Value Command
 *
 * Allows you to change values by key path.
 */
@Suppress("SwallowedException")
object CommandValue : Command.Factory {

    override fun createCommand() = CommandBuilder
        .begin("value")
        .hub()
        .subcommand(setSubCommand())
        .subcommand(resetSubCommand())
        .subcommand(resetAllSubCommand())
        .build()

    private fun setSubCommand() = CommandBuilder
        .begin("set")
        .parameter(
            ParameterBuilder.valueKeyPath("path")
                .required()
                .build()
        )
        .parameter(
            ParameterBuilder.valueType()
                .required()
                .build()
        )
        .handler {
            val valueKey = args[0] as String
            val valueString = args[1] as String

            val value = ConfigSystem.findValueByKey(valueKey)
                ?: throw CommandException(command.result("valueNotFound", valueKey))

            try {
                value.setByString(valueString)
                ModuleClickGui.sync()
            } catch (e: Exception) {
                throw CommandException(command.result("valueError", valueKey, e.message ?: ""))
            }

            chat(
                regular(command.result("success", variable(valueKey))),
                metadata = MessageMetadata(id = "CValue#success${valueKey}")
            )
        }
        .build()

    private fun resetSubCommand() = CommandBuilder
        .begin("reset")
        .parameter(
            ParameterBuilder.valueKeyPath("path")
                .required()
                .build()
        )
        .handler {
            val valueKey = args[0] as String

            val value = ConfigSystem.findValueByKey(valueKey)
                ?: throw CommandException(command.result("valueNotFound", valueKey))

            value.restore()
            ModuleClickGui.sync()
            chat(
                regular(command.result("resetSuccess", variable(valueKey))),
                metadata = MessageMetadata(id = "CValue#reset${valueKey}")
            )
        }
        .build()

    private fun resetAllSubCommand() = CommandBuilder
        .begin("reset-all")
        .parameter(
            ParameterBuilder.valueGroupKeyPath("valueGroupPath")
                .required()
                .build()
        )
        .handler {
            val valueGroupKey = args[0] as String
            val valueGroup = ConfigSystem.findValueGroupByKey(valueGroupKey)
                ?: throw CommandException(command.result("valueGroupNotFound", valueGroupKey))

            valueGroup.collectValuesRecursively()
                .filter { !it.name.equals("Bind", true) }
                .forEach { it.restore() }
            ModuleClickGui.sync()
            chat(
                regular(command.result("resetAllSuccess", variable(valueGroupKey))),
                metadata = MessageMetadata(id = "CValue#resetAll${valueGroupKey}")
            )
        }
        .build()

}
