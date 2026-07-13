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
package net.maxedgar.coffee.api.models.marketplace

import com.google.gson.annotations.SerializedName
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.script.ScriptManager

enum class MarketplaceItemType(
    override val tag: String,
    val isListable: Boolean,
    val isSubscribable: Boolean
) : Tagged {
    @SerializedName("Config")
    CONFIG("Config", false, false),
    @SerializedName("Script")
    SCRIPT("Script", true, true),
    @SerializedName("Theme")
    THEME("Theme", true, true),
    @SerializedName("Other")
    OTHER("Other", false, false);

    suspend fun reload() = when (this) {
        SCRIPT -> ScriptManager.reload()
        else -> { }
    }
}
