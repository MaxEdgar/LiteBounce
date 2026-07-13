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
package net.maxedgar.coffee.features.command.commands.ingame

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.playerName
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable

/**
 * RemoteView Command
 *
 * Allows you to view from the perspective of another player in the game.
 */
object CommandRemoteView : Command.Factory, MinecraftShortcuts {

    private var pName: String? = null

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("remoteview")
            .alias("rv")
            .hub()
            .requiresIngame()
            .subcommand(offSubcommand())
            .subcommand(viewSubcommand())
            .build()
    }

    private fun viewSubcommand() = CommandBuilder
        .begin("view")
        .parameter(
            ParameterBuilder.playerName()
                .required()
                .build()
        )
        .handler {
            val name = args[0] as String
            for (entity in mc.level!!.entitiesForRendering()) {
                if (name.equals(entity.scoreboardName, true)) {
                    if (mc.cameraEntity == entity) {
                        chat(
                            regular(command.result("alreadyViewing", variable(entity.scoreboardName))),
                            metadata = MessageMetadata(id = "CRemoteView#info")
                        )
                        return@handler
                    }

                    mc.cameraEntity = entity
                    pName = entity.scoreboardName
                    chat(
                        regular(command.result("viewPlayer", variable(entity.scoreboardName))),
                        metadata = MessageMetadata(id = "CRemoteView#info")
                    )
                    chat(
                        regular(command.result("caseOff", variable(entity.scoreboardName))),
                        metadata = MessageMetadata(id = "CRemoteView#info", remove = false)
                    )

                    break
                }
            }
        }
        .build()

    private fun offSubcommand() = CommandBuilder
        .begin("off")
        .handler {
            if (mc.cameraEntity != player) {
                mc.cameraEntity = player
                chat(
                    regular(command.result("off", variable(pName.toString()))),
                    metadata = MessageMetadata(id = "CRemoteView#info")
                )
                pName = null
            } else {
                chat(
                    regular(command.result("alreadyOff")),
                    metadata = MessageMetadata(id = "CRemoteView#info")
                )
            }
        }
        .build()

}
