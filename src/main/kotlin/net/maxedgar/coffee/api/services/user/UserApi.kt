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
package net.maxedgar.coffee.api.services.user

import com.google.gson.JsonObject
import net.maxedgar.coffee.api.core.ApiConfig.Companion.config
import net.maxedgar.coffee.api.core.BaseApi
import net.maxedgar.coffee.api.models.auth.OAuthSession
import net.maxedgar.coffee.api.models.auth.addAuth
import net.maxedgar.coffee.api.models.cosmetics.Cosmetic
import net.maxedgar.coffee.api.models.user.UserInformation
import net.maxedgar.coffee.authlib.utils.toRequestBody
import java.util.UUID

/**
 * API for user-related endpoints that require authentication
 */
object UserApi : BaseApi(config.apiEndpointV3) {

    suspend fun getUserInformation(session: OAuthSession) = get<UserInformation>(
        "/oauth/user",
        headers = { addAuth(session) }
    )

    suspend fun getCosmetics(session: OAuthSession) = get<Set<Cosmetic>>(
        "/cosmetics/self",
        headers = { addAuth(session) }
    )

    suspend fun transferTemporaryOwnership(session: OAuthSession, uuid: UUID) = put<Unit>(
        "/cosmetics/self",
        JsonObject().apply {
            addProperty("uuid", uuid.toString())
        }.toRequestBody(),
        headers = { addAuth(session) }
    )
}
