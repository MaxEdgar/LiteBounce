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
package net.maxedgar.coffee.api.models.client

import com.google.gson.annotations.SerializedName
import net.maxedgar.coffee.api.types.enums.AutoSettingsStatusType
import net.maxedgar.coffee.api.types.enums.AutoSettingsType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class AutoSettings(
    @SerializedName("setting_id") val settingId: String,
    val name: String,
    @SerializedName("setting_type") val type: AutoSettingsType,
    val description: String,
    val date: LocalDateTime,
    val contributors: String,
    @SerializedName("status_type") val statusType: AutoSettingsStatusType,
    @SerializedName("status_date") val statusDate: LocalDateTime,
    @SerializedName("server_address") val serverAddress: String?
) {
    val dateFormatted: String
        get() = date.format(FORMATTER)

    val statusDateFormatted: String
        get() = statusDate.format(FORMATTER)

    companion object {
        @JvmField
        internal val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    }
}
