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
package net.maxedgar.coffee.features.command.commands.ingame

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.playerName
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.network
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.client.world
import net.minecraft.ChatFormatting
import org.apache.commons.lang3.StringUtils

/**
 * Coordinates Command
 *
 * Copies your coordinates to your clipboard.
 */
object CommandCoordinates : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("coordinates")
            .alias("position", "coords")
            .hub()
            .requiresIngame()
            .subcommand(
                CommandBuilder.begin("whisper")
                    .parameter(
                        ParameterBuilder.playerName()
                            .required()
                            .build()
                    )
                    .handler {
                        val name = args[0] as String
                        network.sendCommand("msg $name ${getCoordinates(fancy = true)}")
                    }
                    .build()
            )
            .subcommand(
                CommandBuilder.begin("copy")
                    .handler {
                        mc.keyboardHandler.clipboard = getCoordinates()
                        chat(command.result("success"), command)
                    }
                    .build()
            )
            .subcommand(
                CommandBuilder.begin("info")
                    .handler {
                        chat(getCoordinates().asPlainText(ChatFormatting.GRAY), command)
                    }
                    .build()
            )
            .build()
    }

    private fun getCoordinates(fancy: Boolean = false): String {
        val pos = player.blockPosition()
        val dimension = StringUtils.capitalize(world.dimension().identifier().path)
        val start = if (fancy) "My coordinates are: " else ""
        return start +
            "x: ${pos.x}, y: ${pos.y}, z: ${pos.z} " +
            "in the $dimension"
    }

}
