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

package net.maxedgar.coffee.features.account

import net.maxedgar.coffee.authlib.account.AlteningAccount
import net.maxedgar.coffee.authlib.account.CrackedAccount
import net.maxedgar.coffee.authlib.account.MicrosoftAccount
import net.maxedgar.coffee.authlib.account.MinecraftAccount
import net.maxedgar.coffee.authlib.account.SessionAccount
import net.maxedgar.coffee.config.types.list.Tagged

enum class AccountService(override val tag: String, val canJoinOnline: Boolean) : Tagged {
    MICROSOFT("Microsoft", true),
    SESSION("Session", true),
    THEALTENING("TheAltening", true),
    CRACKED("Cracked", false);

    companion object {
        fun getService(account: MinecraftAccount) = when (account) {
            is MicrosoftAccount -> MICROSOFT
            is SessionAccount -> SESSION
            is AlteningAccount -> THEALTENING
            is CrackedAccount -> CRACKED
            else -> throw IllegalArgumentException("Unknown account type: ${account::class.java.name}")
        }
    }

}
