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
package net.maxedgar.coffee.features.command.commands.client.client

import net.ccbluex.fastutil.mapToArray
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.global.GlobalManager
import net.maxedgar.coffee.lang.LanguageManager
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular

object CommandClientLanguageSubcommand {
    fun languageCommand() = CommandBuilder.begin("language")
        .hub()
        .subcommand(listSubcommand())
        .subcommand(setSubcommand())
        .subcommand(unsetSubcommand())
        .build()

    private fun unsetSubcommand() = CommandBuilder.begin("unset")
        .handler {
            chat(regular("Unset override language..."))
            LanguageManager.clientLanguage = LanguageManager.ClientLanguage.AUTO
            ConfigSystem.store(GlobalManager)
        }.build()

    private fun setSubcommand() = CommandBuilder.begin("set")
        .parameter(
            ParameterBuilder.begin<String>("language")
                .autocompletedFrom { LanguageManager.languageCodes }
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR).required()
                .build()
        ).handler {
            val language = args[0] as String
            val choice = LanguageManager.languageChoiceFromCode(language)
            if (choice == null) {
                chat(regular("Language not found."))
                return@handler
            }

            chat(regular("Setting language to ${choice.tag}..."))
            LanguageManager.clientLanguage = choice

            ConfigSystem.store(GlobalManager)
        }.build()

    private fun listSubcommand() = CommandBuilder.begin("list")
        .handler {
            chat(regular("Available languages:"))
            chat(texts = LanguageManager.languageCodes.mapToArray { regular("-> $it") })
        }.build()
}
