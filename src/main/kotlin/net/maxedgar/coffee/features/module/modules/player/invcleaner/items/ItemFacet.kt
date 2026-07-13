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
package net.maxedgar.coffee.features.module.modules.player.invcleaner.items

import it.unimi.dsi.fastutil.objects.ObjectIntPair
import net.maxedgar.coffee.features.module.modules.player.invcleaner.ItemCategory
import net.maxedgar.coffee.features.module.modules.player.invcleaner.ItemFunction
import net.maxedgar.coffee.features.module.modules.player.invcleaner.ItemType
import net.maxedgar.coffee.utils.inventory.ItemSlot
import net.maxedgar.coffee.utils.item.ItemStackHolder
import net.maxedgar.coffee.utils.item.durability
import net.minecraft.core.component.DataComponents

open class ItemFacet(val itemSlot: ItemSlot) : Comparable<ItemFacet>, ItemStackHolder by itemSlot {
    open val category: ItemCategory
        get() = ItemType.NONE.defaultCategory

    open val providedItemFunctions: List<ObjectIntPair<ItemFunction>>
        get() = emptyList()

    val isInHotbar: Boolean
        get() = this.itemSlot.slotType == ItemSlot.Type.HOTBAR || this.itemSlot.slotType == ItemSlot.Type.OFFHAND

    open fun isSignificantlyBetter(other: ItemFacet): Boolean {
        return false
    }

    /**
     * Should this item be kept, even if it is not allocated to any slot?
     */
    open fun shouldKeep(): Boolean = false

    override fun compareTo(other: ItemFacet): Int = compareValuesBy(this, other, ItemFacet::isInHotbar)

    companion object {
        @JvmField
        protected val PREFER_ENCHANTABLE: Comparator<in ItemStackHolder> = Comparator.comparingInt {
            it.itemStack[DataComponents.ENCHANTABLE]?.value ?: 0
        }

        @JvmField
        protected val PREFER_ITEMS_IN_HOTBAR: Comparator<ItemFacet> = compareBy(ItemFacet::isInHotbar)

        @JvmField
        protected val STABILIZE_COMPARISON: Comparator<in ItemStackHolder> = Comparator.comparingInt {
            it.itemStack.hashCode()
        }
        @JvmField
        protected val PREFER_BETTER_DURABILITY: Comparator<in ItemStackHolder> = Comparator.comparingInt {
            it.itemStack.durability
        }
    }

}
