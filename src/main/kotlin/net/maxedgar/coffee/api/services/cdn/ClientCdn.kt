/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.api.services.cdn

import net.maxedgar.coffee.api.core.ApiConfig.Companion.CLIENT_CDN
import net.maxedgar.coffee.api.core.BaseApi
import net.maxedgar.coffee.api.core.utf8Lines
import okio.BufferedSource

object ClientCdn : BaseApi(CLIENT_CDN) {
    suspend fun requestStaffList(address: String): Set<String> =
        get<BufferedSource>("/staffs/$address").utf8Lines().asSequence().toHashSet()
}
