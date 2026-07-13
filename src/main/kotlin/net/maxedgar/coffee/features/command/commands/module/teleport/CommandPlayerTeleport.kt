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
package net.maxedgar.coffee.features.command.commands.module.teleport

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.features.module.modules.movement.ModuleTeleport
import org.lwjgl.glfw.GLFW
import kotlin.math.floor

/**
 * Teleport Command
 *
 * Allows you to teleport.
 *
 * Module: [ModuleTeleport]
 */
object CommandPlayerTeleport : Command.Factory, MinecraftShortcuts {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("playerteleport")
            .alias("playertp", "ptp")
            .requiresIngame()
            .parameter(
                ParameterBuilder
                    .begin<String>("player")
                    .required()
                    .build(),
            )
            .parameter(
                ParameterBuilder
                    .begin<String>("copy")
                    .optional()
                    .build()
            )
            .handler {
                val player = world.players().find { it.gameProfile.name.equals(args[0] as String, true) }
                    ?: throw CommandException(command.result("playerNotFound"))

                val y = if (ModuleTeleport.highTp) {
                        ModuleTeleport.highTpAmount
                    } else {
                        player.y
                    }

                if (args.size > 1 && args[1] == "copy") {
                    val clipboard = ".teleport ${floor(player.x).toInt()} " +
                        "${floor(y.toDouble()).toInt()} ${floor(player.z).toInt()}"

                    GLFW.glfwSetClipboardString(mc.window.handle(), clipboard)
                    return@handler
                }

                ModuleTeleport.indicateTeleport(player.x, y.toDouble(), player.z)
            }
            .build()
    }
}
