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

package net.maxedgar.coffee.features.module.modules.player.autobuff.features

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.ScheduleInventoryActionEvent
import net.maxedgar.coffee.features.module.modules.player.autobuff.ModuleAutoBuff
import net.maxedgar.coffee.utils.inventory.InventoryAction
import net.maxedgar.coffee.utils.inventory.PlayerInventoryConstraints
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.kotlin.Priority

object Refill : ToggleableValueGroup(ModuleAutoBuff, "Refill", true) {

    private val inventoryConstraints = tree(PlayerInventoryConstraints())

    fun execute(event: ScheduleInventoryActionEvent) {
        // Check if we have space in the hotbar
        if (!findEmptyHotbarSlot()) {
            return
        }

        val validFeatures = ModuleAutoBuff.activeFeatures

        // Find valid items in the inventory
        val validItems = Slots.Inventory.filter {
            val itemStack = it.itemStack
            validFeatures.any { f -> f.isValidItem(itemStack, false) }
        }

        // Check if we have any valid items
        if (validItems.isEmpty()) {
            return
        }

        // Sort the items by the order of the features
        for (slot in validItems) {
            event.schedule(
                inventoryConstraints, InventoryAction.Click.performQuickMove(slot = slot),
                Priority.IMPORTANT_FOR_USAGE_1
            )
        }
    }

    private fun findEmptyHotbarSlot(): Boolean {
        return Slots.OffhandWithHotbar.findSlot { it.isEmpty } != null
    }

}
