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

package net.maxedgar.coffee.config.autoconfig

import net.maxedgar.coffee.api.types.enums.AutoSettingsStatusType
import net.maxedgar.coffee.api.types.enums.AutoSettingsType
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.text.joinToText
import net.maxedgar.coffee.utils.text.textOf
import net.maxedgar.coffee.utils.text.PlainText.NEW_LINE
import net.maxedgar.coffee.utils.text.PlainText.SPACE
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

@JvmRecord
data class AutoConfigMetadata(
    val name: String,
    val author: String?,
    val date: String?,
    val time: String?,
    val clientVersion: String?,
    val clientCommit: String?,
    val protocolName: String?,
    val protocolVersion: Int?,
    val serverAddress: String?,
    val type: AutoSettingsType?,
    val status: AutoSettingsStatusType?,
    val chat: List<String>?,
) {

    private fun asTexts(): List<Component> = buildList {
        val colon = ":".asPlainText(ChatFormatting.DARK_GRAY)
        fun addEntry(key: String, value: String?) {
            if (value.isNullOrBlank()) return

            this += textOf(
                key.asPlainText(ChatFormatting.GRAY),
                colon,
                SPACE,
                value.asPlainText(ChatFormatting.WHITE),
            )
        }

        addEntry("Name", name)
        addEntry("Author", author)
        addEntry("Date", date)
        addEntry("Time", time)
        addEntry("Client Version", clientVersion)
        addEntry("Client Commit", clientCommit)
        addEntry("Protocol Name", protocolName)
        addEntry("Protocol Version", protocolVersion?.toString())
        addEntry("Type", type?.displayName)
        addEntry("Status", status?.displayName)
        addEntry("Chat", chat?.joinToString("\n    "))
    }

    fun asText(): Component = asTexts().joinToText(NEW_LINE)

}
