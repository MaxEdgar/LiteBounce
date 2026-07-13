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

import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder.Companion.BOOLEAN_VALIDATOR
import net.maxedgar.coffee.features.misc.HideAppearance.destructClient
import net.maxedgar.coffee.features.misc.HideAppearance.wipeClient
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.regular

object CommandClientDestructSubcommand {
    fun destructCommand() = CommandBuilder.begin("destruct")
        .parameter(
            ParameterBuilder.begin<Boolean>("confirm")
                .verifiedBy(BOOLEAN_VALIDATOR)
                .optional()
                .build()
        )
        .parameter(
            ParameterBuilder.begin<Boolean>("wipe")
                .verifiedBy(BOOLEAN_VALIDATOR)
                .optional()
                .build()
        )
        .handler {
            val confirm = args.getOrNull(0) as Boolean? == true
            if (!confirm) {
                chat(
                    regular("Do you really want to destruct the client? " +
                    "If so, type the command again with 'yes' at the end.")
                )
                chat(markAsError("If you also want to wipe the client, add an additional 'yes' at the end."))
                chat(regular("For full destruct: .client destruct yes yes"))
                chat(regular("For temporary destruct: .client destruct yes"))
                return@handler
            }

            val wipe = args.getOrNull(1) as Boolean? == true

            chat(regular("Coffee is being destructed from your client..."))
            if (!wipe) {
                chat(
                    regular("WARNING: You have not wiped the client (missing wipe parameter) - therefore " +
                    "some files may still be present!")
                )
            }

            destructClient()
            chat(
                regular("Coffee has been destructed from your client. " +
                "You can clear your chat using F3+D. If wipe was enabled, the chat will be cleared automatically.")
            )

            if (wipe) {
                chat(regular("Wiping client..."))
                // Runs on a separate thread to prevent blocking the main thread and
                // repeating the process when required
                wipeClient()
            }
        }.build()
}
