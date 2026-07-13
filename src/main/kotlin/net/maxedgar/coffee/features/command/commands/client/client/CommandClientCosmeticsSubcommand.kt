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

import kotlinx.coroutines.suspendCancellableCoroutine
import net.maxedgar.coffee.features.command.CommandExecutor.suspendHandler
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.cosmetic.ClientAccountManager
import net.maxedgar.coffee.features.cosmetic.CosmeticService
import net.maxedgar.coffee.utils.client.browseUrl
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import kotlin.coroutines.resume

object CommandClientCosmeticsSubcommand {
    fun cosmeticsCommand() = CommandBuilder
        .begin("cosmetics")
        .hub()
        .subcommand(refreshSubcommand())
        .subcommand(manageSubcommand())
        .build()

    private fun manageSubcommand() = CommandBuilder.begin("manage")
        .handler {
            browseUrl("https://user.liquidbounce.net/cosmetics")
        }
        .build()

    private fun refreshSubcommand() = CommandBuilder.begin("refresh")
        .suspendHandler {
            chat(
                regular(
                    "Refreshing cosmetics..."
                )
            )
            CosmeticService.carriersCosmetics.clear()
            ClientAccountManager.clientAccount.cosmetics = null

            suspendCancellableCoroutine { continuation ->
                CosmeticService.refreshCarriers(true) {
                    chat(
                        regular(
                            "Cosmetic System has been refreshed."
                        )
                    )
                    continuation.resume(Unit)
                }
            }
        }
        .build()
}
