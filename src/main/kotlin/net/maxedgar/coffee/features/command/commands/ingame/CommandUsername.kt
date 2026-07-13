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
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.utils.client.bypassNameProtection
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.client.italic
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.underline
import net.maxedgar.coffee.utils.client.variable
import org.lwjgl.glfw.GLFW

/**
 * CommandUsername
 *
 * Displays the current username.
 */
object CommandUsername : Command.Factory, MinecraftShortcuts {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("username")
            .requiresIngame()
            .handler {
                val username = player.name.string
                val formattedUsernameWithEvents = variable(username)
                    .bypassNameProtection()
                    .copyable(copyContent = username)
                    .italic(true)
                    .underline(true)

                chat(regular(command.result("username", formattedUsernameWithEvents)), command)
                GLFW.glfwSetClipboardString(mc.window.handle(), username)
            }
            .build()
    }

}
