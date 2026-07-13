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

package net.maxedgar.coffee.features.command.preset

import net.maxedgar.coffee.api.models.auth.ClientAccount
import net.maxedgar.coffee.api.models.auth.ClientAccount.Companion.EMPTY_ACCOUNT
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.cosmetic.ClientAccountManager
import net.maxedgar.coffee.lang.translation

internal fun ClientAccountManager.accountOrException(): ClientAccount {
    val clientAccount = clientAccount
    if (clientAccount == EMPTY_ACCOUNT) {
        throw CommandException(translation("liquidbounce.command.marketplace.error.notLoggedIn"))
    }
    return clientAccount
}
