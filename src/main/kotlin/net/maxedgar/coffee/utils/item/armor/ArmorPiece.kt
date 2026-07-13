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
package net.maxedgar.coffee.utils.item.armor

import net.maxedgar.coffee.utils.inventory.ItemSlot
import net.maxedgar.coffee.utils.item.armorKnockbackResistance
import net.maxedgar.coffee.utils.item.armorToughness
import net.maxedgar.coffee.utils.item.armorValue
import net.maxedgar.coffee.utils.item.equipmentSlot
import net.minecraft.world.entity.EquipmentSlot

/**
 * @see net.minecraft.world.item.equipment.ArmorMaterial.createAttributes
 */
@JvmInline
value class ArmorPiece(val itemSlot: ItemSlot) {
    val slotType: EquipmentSlot
        get() = itemSlot.itemStack.equipmentSlot!!
    val entitySlotId: Int
        get() = this.slotType.index
    val inventorySlot: Int
        get() = 36 + entitySlotId
    val isAlreadyEquipped: Boolean
        get() = itemSlot.slotType == ItemSlot.Type.ARMOR
    val isReachableByHand: Boolean
        get() = itemSlot.slotType == ItemSlot.Type.HOTBAR

    val toughness: Float
        get() = itemSlot.itemStack.armorToughness.toFloat()

    val defensePoints: Float
        get() = itemSlot.itemStack.armorValue.toFloat()

    val knockbackResistance: Float
        get() = itemSlot.itemStack.armorKnockbackResistance.toFloat()
}
