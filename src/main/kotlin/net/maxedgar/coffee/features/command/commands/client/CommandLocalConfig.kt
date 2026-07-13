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

import kotlinx.coroutines.async
import net.maxedgar.coffee.api.core.ioScope
import net.maxedgar.coffee.api.models.client.AutoSettings
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.autoconfig.AutoConfig
import net.maxedgar.coffee.config.autoconfig.AutoConfig.serializeAutoConfig
import net.maxedgar.coffee.config.autoconfig.AutoConfigMetadata
import net.maxedgar.coffee.config.autoconfig.IncludeConfiguration
import net.maxedgar.coffee.config.gson.publicGson
import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.CommandManager
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.boolean
import net.maxedgar.coffee.features.command.builder.modules
import net.maxedgar.coffee.features.command.preset.pagedQuery
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.clickablePath
import net.maxedgar.coffee.utils.client.highlight
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.onClick
import net.maxedgar.coffee.utils.client.onHover
import net.maxedgar.coffee.utils.text.plus
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.text.textOf
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.kotlin.unmodifiable
import net.maxedgar.coffee.utils.text.AsyncLoadingText
import net.maxedgar.coffee.utils.text.PlainText
import net.minecraft.ChatFormatting
import net.minecraft.SharedConstants
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.util.Util
import java.io.File
import java.time.Instant
import java.time.ZoneId

/**
 * LocalConfig Command
 *
 * Allows you to load, list, and create local configurations.
 */
object CommandLocalConfig : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("localconfig")
            .hub()
            .subcommand(loadSubcommand())
            .subcommand(listSubcommand())
            .subcommand(browseSubcommand())
            .subcommand(saveSubcommand())
            .build()
    }

    private fun hoverText(file: File, settingName: String) =
        textOf(
            "Click to load ".asPlainText(ChatFormatting.GRAY),
            settingName.asPlainText(Style.EMPTY + ChatFormatting.AQUA + ChatFormatting.BOLD),
            PlainText.NEW_LINE,
            AsyncLoadingText(
                ioScope.async {
                    file.bufferedReader().use { r ->
                        publicGson.fromJson(r, AutoConfigMetadata::class.java)
                    }.asText()
                }
            )
        )

    private fun saveSubcommand() = CommandBuilder
        .begin("save")
        .alias("create")
        .parameter(
            ParameterBuilder.begin<String>("name")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedFrom {
                    ConfigSystem.userConfigsFolder.listFiles()?.map { it.nameWithoutExtension }
                }
                .required()
                .build()
        )
        .parameter(
            ParameterBuilder.boolean("overwrite")
                .optional()
                .build()
        )
        .parameter(
            ParameterBuilder.begin<String>("include")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedFrom { listOf("binds", "hidden") }
                .vararg()
                .optional()
                .build()
        )
        .handler {
            val name = args[0] as String

            if (name.isBlank() || name.indexOfAny(SharedConstants.ILLEGAL_FILE_CHARACTERS) != -1) {
                throw CommandException(command.result("invalidFileName", variable(name)))
            }

            val overwrite = args.getOrNull(1) as Boolean? ?: false
            @Suppress("UNCHECKED_CAST")
            val include = args.getOrNull(2) as Array<*>? ?: emptyArray<String>()

            val includeConfiguration = IncludeConfiguration(
                includeBinds = include.contains("binds"),
                includeHidden = include.contains("hidden"),
            )

            val file = ConfigSystem.userConfigsFolder.resolve("$name.json")
            try {
                if (file.exists()) {
                    if (overwrite) {
                        file.delete()
                    } else {
                        chat(markAsError(command.result("alreadyExists", variable(name))))
                        return@handler
                    }
                }

                file.createNewFile()
                serializeAutoConfig(file.bufferedWriter(), includeConfiguration)
                chat(regular(command.result("created", variable(name))))
            } catch (e: Exception) {
                chat(regular(command.result("failedToCreate", variable(name))))
                logger.error("Failed to create local config '$name'", e)
            }
        }
        .build()

    private fun browseSubcommand() = CommandBuilder.begin("browse").handler {
        Util.getPlatform().openFile(ConfigSystem.userConfigsFolder)
        chat(regular(command.result("browse", clickablePath(ConfigSystem.userConfigsFolder))))
    }.build()

    private fun listSubcommand() = CommandBuilder
        .begin("list")
        .pagedQuery(
            pageSize = 8,
            header = {
                highlight("Local Configs:")
            },
            items = {
                ConfigSystem.userConfigsFolder.listFiles { _, name ->
                    name.endsWith(".json", ignoreCase = true)
                }.unmodifiable()
            },
            eachRow = { _, file ->
                val settingName = file.name.removeSuffix(".json")

                val lastModified = Instant.ofEpochMilli(file.lastModified())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(AutoSettings.FORMATTER)

                textOf(
                    "\u2B25 ".asPlainText(ChatFormatting.BLUE),
                    variable(file.name)
                        .onClick(
                            ClickEvent.SuggestCommand(
                                CommandManager.GlobalSettings.prefix + "localconfig load $settingName"
                            )
                        )
                        .onHover(HoverEvent.ShowText(hoverText(file, settingName))),
                    regular(" ($lastModified)"),
                )
            }
        )

    private fun loadSubcommand() = CommandBuilder
        .begin("load")
        .parameter(
            ParameterBuilder
                .begin<String>("name")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .autocompletedFrom {
                    ConfigSystem.userConfigsFolder.listFiles()?.map { it.nameWithoutExtension }
                }
                .required()
                .build()
        )
        .parameter(
            ParameterBuilder.modules()
                .optional()
                .build()
        )
        .handler {
            val name = args[0] as String
            val modules = args.getOrNull(1) as Set<ClientModule>? ?: emptySet()

            ConfigSystem.userConfigsFolder.resolve("$name.json").runCatching {
                if (!exists()) {
                    chat(regular(command.result("notFound", variable(name))))
                    return@handler
                }

                bufferedReader().use { r ->
                    AutoConfig.withLoading {
                        AutoConfig.loadAutoConfig(r, modules)
                    }
                }
            }.onFailure { error ->
                logger.error("Failed to load config $name", error)
                chat(markAsError(command.result("failedToLoad", variable(name))))
            }.onSuccess {
                chat(regular(command.result("loaded", variable(name))))
            }
        }
        .build()

}
