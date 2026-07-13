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

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder

/**
 * Client Command
 *
 * Provides subcommands for client management.
 */
object CommandClient : Command.Factory {

    /**
     * Creates client command with a variety of subcommands.
     */
    override fun createCommand(): Command {
        return CommandBuilder.begin("client")
            .hub()
            .subcommand(CommandClientInfoSubcommand.infoCommand())
            .subcommand(CommandClientLanguageSubcommand.languageCommand())
            .subcommand(CommandClientAppearanceSubcommand.appearanceCommand())
            .subcommand(CommandClientPrefixSubcommand.prefixCommand())
            .subcommand(CommandClientDestructSubcommand.destructCommand())
            .subcommand(CommandClientAccountSubcommand.accountCommand())
            .subcommand(CommandClientCosmeticsSubcommand.cosmeticsCommand())
            .subcommand(CommandClientConfigSubcommand.configCommand())
            .build()
    }

}
