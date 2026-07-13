/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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

import net.maxedgar.coffee.config.types.list.MultiChoiceListValue
import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.enumChoice
import net.maxedgar.coffee.features.global.GlobalSettingsTarget
import net.maxedgar.coffee.features.module.modules.render.ModuleClickGui
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.combat.Targets

/**
 * Enemy Command
 *
 * Provides subcommands for enemy configuration.
 */
object CommandTargets : Command.Factory {

    override fun createCommand() = CommandBuilder
        .begin("targets")
        .alias("target", "enemies", "enemy")
        .subcommand(
            CommandBuilder
                .begin("combat")
                .fromTargets(GlobalSettingsTarget.combatChoices)
                .build()
        )
        .subcommand(
            CommandBuilder
                .begin("visual")
                .fromTargets(GlobalSettingsTarget.visualChoices)
                .build()
        )
        .hub()
        .build()

    private fun CommandBuilder.fromTargets(targets: MultiChoiceListValue<Targets>): CommandBuilder {
        this.parameter(
            ParameterBuilder
                .enumChoice<Targets>("category") { it in targets.choices }
                .required()
                .build()
        ).handler {
            val entry = args[0] as Targets

            val state = targets.toggle(entry)
            val localizedState = if (state) {
                "enabled"
            } else {
                "disabled"
            }
            chat(
                regular(command.result(localizedState,
                    entry.name.lowercase().replaceFirstChar { it.uppercase() })
                ),
                metadata = MessageMetadata(id = "CTargets#info")
            )

            ModuleClickGui.sync()
        }

        return this
    }

}
