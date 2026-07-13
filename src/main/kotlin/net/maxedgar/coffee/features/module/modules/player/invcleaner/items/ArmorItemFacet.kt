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

import net.maxedgar.coffee.features.module.modules.player.invcleaner.ItemCategory
import net.maxedgar.coffee.features.module.modules.player.invcleaner.ItemType
import net.maxedgar.coffee.utils.inventory.ItemSlot
import net.maxedgar.coffee.utils.item.armor.ArmorComparator
import net.maxedgar.coffee.utils.item.armor.ArmorPiece

/**
 * @param stacksToKeep armor items which should be kept since they might be strong in future situations
 */
class ArmorItemFacet(
    itemSlot: ItemSlot,
    private val stacksToKeep: List<ItemSlot>,
    private val armorComparator: ArmorComparator
) : ItemFacet(itemSlot) {
    private val armorPiece = ArmorPiece(itemSlot)

    override val category: ItemCategory
        get() = ItemCategory(ItemType.ARMOR, armorPiece.entitySlotId)

    override fun shouldKeep(): Boolean {
        return this.stacksToKeep.contains(this.itemSlot)
    }

    override fun compareTo(other: ItemFacet): Int {
        return armorComparator.compare(this.armorPiece, (other as ArmorItemFacet).armorPiece)
    }
}
