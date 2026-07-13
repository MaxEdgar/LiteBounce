/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.features.module.modules.player.autoshop.serializable

data class ShopConfig (
    val traderTitles: List<String>,
    val initialCategorySlot: Int,
    val itemsWithTiers: Map<String, List<String>>? = emptyMap(),
    val elements: List<ShopElement>
) {
    companion object {
        @JvmField
        val Empty = ShopConfig(emptyList(), -1, emptyMap(), emptyList())
    }
}
