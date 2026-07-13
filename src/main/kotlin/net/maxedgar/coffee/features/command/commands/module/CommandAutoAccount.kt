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

package net.maxedgar.coffee.features.command.commands.module

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.module.modules.misc.ModuleAutoAccount

/**
 * AutoAccount Command
 *
 * Allows you to manually trigger the actions of [ModuleAutoAccount].
 *
 * Module: [ModuleAutoAccount]
 */
object CommandAutoAccount : Command.Factory {

    @Suppress("SpellCheckingInspection")
    override fun createCommand(): Command {
        return CommandBuilder
            .begin("autoaccount")
            .requiresIngame()
            .hub()
            .subcommand(
                CommandBuilder
                    .begin("register")
                    .handler {
                        ModuleAutoAccount.register()
                    }
                    .build()
            )
            .subcommand(
                CommandBuilder
                    .begin("login")
                    .handler {
                        ModuleAutoAccount.login()
                    }
                    .build()
            )
            .build()
    }

}
