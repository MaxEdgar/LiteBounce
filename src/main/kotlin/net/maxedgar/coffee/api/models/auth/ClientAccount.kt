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
package net.maxedgar.coffee.api.models.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.maxedgar.coffee.api.models.cosmetics.Cosmetic
import net.maxedgar.coffee.api.models.user.UserInformation
import net.maxedgar.coffee.api.services.auth.OAuthClient
import net.maxedgar.coffee.api.services.user.UserApi
import net.maxedgar.coffee.config.gson.stategies.Exclude
import net.maxedgar.coffee.utils.client.env
import java.util.UUID


/**
 * Represents a client account that is used to authenticate with the Coffee API.
 * It might hold additional information that can be obtained from the API.
 */
data class ClientAccount(
    private var session: OAuthSession? = null,
    @Exclude
    var userInformation: UserInformation? = null,
    @Exclude
    var cosmetics: Set<Cosmetic>? = null
) {
    suspend fun takeSession(): OAuthSession = session?.takeIf { !it.accessToken.isExpired() } ?: run {
        renew()
        session ?: error("No session")
    }

    suspend fun updateInfo(): Unit = withContext(Dispatchers.IO) {
        userInformation = UserApi.getUserInformation(takeSession())
    }

    suspend fun updateCosmetics(): Unit = withContext(Dispatchers.IO) {
        cosmetics = UserApi.getCosmetics(takeSession())
    }

    suspend fun transferTemporaryOwnership(uuid: UUID): Unit = withContext(Dispatchers.IO) {
        UserApi.transferTemporaryOwnership(takeSession(), uuid)
    }

    suspend fun renew() = withContext(Dispatchers.IO) {
        session = OAuthClient.renewToken(session ?: error("No session"))
    }

    companion object {
        @JvmField
        val EMPTY_ACCOUNT = ClientAccount(null, null, null)
        @JvmField
        val ENV_ACCOUNT = fromEnv()

        private fun fromEnv(): ClientAccount? {
            val accessToken = env(
                "LB_ACCOUNT_ACCESS_TOKEN",
                "net.maxedgar.coffee.account.accessToken"
            ) ?: return null
            val accessTokenExpiresAt = env(
                "LB_ACCOUNT_ACCESS_TOKEN_EXPIRES_AT",
                "net.maxedgar.coffee.account.accessTokenExpiresAt"
            )?.toLongOrNull() ?: return null
            val refreshToken = env(
                "LB_ACCOUNT_REFRESH_TOKEN",
                "net.maxedgar.coffee.account.refreshToken"
            ) ?: return null

            return ClientAccount(
                session = OAuthSession(
                    accessToken = ExpiryValue(accessToken, accessTokenExpiresAt),
                    refreshToken = refreshToken
                )
            )
        }

    }
}
