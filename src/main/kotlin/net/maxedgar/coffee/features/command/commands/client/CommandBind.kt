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

import net.ccbluex.fastutil.toEnumSet
import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.enumChoice
import net.maxedgar.coffee.features.command.builder.module
import net.maxedgar.coffee.features.command.dsl.addParam
import net.maxedgar.coffee.features.command.dsl.buildCommand
import net.maxedgar.coffee.features.command.dsl.cast
import net.maxedgar.coffee.features.command.dsl.castNotRequired
import net.maxedgar.coffee.features.command.dsl.castVarargNotRequired
import net.maxedgar.coffee.features.module.modules.render.ModuleClickGui
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.input.InputBind
import net.maxedgar.coffee.utils.input.availableInputKeys
import net.maxedgar.coffee.utils.input.bind
import net.maxedgar.coffee.utils.input.inputByName
import net.maxedgar.coffee.utils.input.renderText
import net.maxedgar.coffee.utils.input.unbind

/**
 * Bind Command
 *
 * Allows you to bind a key to a module, which means that the module will be activated when the key is pressed.
 */
object CommandBind : Command.Factory {

    override fun createCommand() = buildCommand("bind") {
        val module = addParam {
            module().required()
        }

        val key = addParam("key") {
            verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedFrom { availableInputKeys }
                .required()
        }

        val action = addParam {
            enumChoice<InputBind.BindAction>("action")
                .optional()
        }

        val modifiers = addParam {
            enumChoice<InputBind.Modifier>("modifiers")
                .optional()
                .vararg()
        }

        handler {
            val module = module.cast()
            val keyName = key.cast()
            val action = action.castNotRequired() ?: module.bindValue.get().action
            val modifiers = modifiers.castVarargNotRequired()?.toEnumSet() ?: module.bindValue.get().modifiers

            if (keyName.equals("none", true)) {
                module.bindValue.unbind()
                ModuleClickGui.sync()
                chat(
                    regular(command.result("moduleUnbound", variable(module.name))),
                    metadata = MessageMetadata(id = "Bind#${module.name}")
                )
                return@handler
            }

            runCatching {
                module.bindValue.bind(inputByName(keyName), action, modifiers)
                ModuleClickGui.sync()
            }.onSuccess {
                chat(
                    regular(command.result("moduleBound", variable(module.name), module.bind.renderText())),
                    metadata = MessageMetadata(id = "Bind#${module.name}")
                )
            }.onFailure {
                chat(
                    markAsError(command.result("keyNotFound", variable(keyName))),
                    metadata = MessageMetadata(id = "Bind#${module.name}")
                )
            }

        }
    }

}
