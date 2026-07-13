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
import net.maxedgar.coffee.utils.item.PreferStackSize
import net.maxedgar.coffee.utils.item.asHolderComparator
import net.maxedgar.coffee.utils.item.foodComponent
import net.maxedgar.coffee.utils.sorting.ComparatorChain
import net.maxedgar.coffee.utils.sorting.compareByCondition
import net.minecraft.world.item.Items

class FoodItemFacet(itemSlot: ItemSlot) : ItemFacet(itemSlot) {
    companion object {
        private val COMPARATOR =
            ComparatorChain<FoodItemFacet>(
                compareByCondition { it.itemStack.item == Items.ENCHANTED_GOLDEN_APPLE },
                compareByCondition { it.itemStack.item == Items.GOLDEN_APPLE },
                // Nutriment
                compareBy {
                    val foodComponent = it.itemStack.foodComponent!!

                    foodComponent.saturation / foodComponent.nutrition.toFloat()
                },
                compareBy { it.itemStack.foodComponent!!.nutrition },
                compareBy { it.itemStack.foodComponent!!.saturation },
                PreferStackSize.PREFER_FEWER.asHolderComparator(),
                PREFER_ITEMS_IN_HOTBAR,
                STABILIZE_COMPARISON,
            )
    }

    override val providedItemFunctions: List<ObjectIntPair<ItemFunction>>
        get() = listOf(ObjectIntPair.of(ItemFunction.FOOD, itemStack.count * itemStack.foodComponent!!.nutrition))

    override val category: ItemCategory
        get() = ItemType.FOOD.defaultCategory

    override fun compareTo(other: ItemFacet): Int {
        return COMPARATOR.compare(this, other as FoodItemFacet)
    }
}
