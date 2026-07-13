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
package net.maxedgar.coffee.features.module.modules.misc

import net.maxedgar.coffee.event.events.ScheduleInventoryActionEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.inventory.ArmorItemSlot
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.inventory.InventoryAction
import net.maxedgar.coffee.utils.inventory.ItemSlot
import net.maxedgar.coffee.utils.inventory.PlayerInventoryConstraints
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.item.isChestArmor
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * ModuleElytraSwap
 *
 * Allows you to quickly replace your chestplate with an elytra and vice versa.
 *
 * @author sqlerrorthing
 * @since 2/13/2025
 **/
object ModuleElytraSwap : ClientModule(
    "ElytraSwap",
    ModuleCategories.PLAYER,
    aliases = listOf("ChestSwap"),
    disableOnQuit = true
) {

    private val constraints = tree(PlayerInventoryConstraints())

    private val slotsToSearch = Slots.Hotbar + Slots.Inventory + HotbarItemSlot.OFFHAND
    private val chestplateSlot inline get() = ArmorItemSlot.CHEST

    @Suppress("unused")
    private val scheduleInventoryActionHandler = handler<ScheduleInventoryActionEvent>(
        EventPriorityConvention.CRITICAL_MODIFICATION
    ) { event ->
        val elytraItem = slotsToSearch.findSlot { it.isElytra() && !it.nextDamageWillBreak() }
        val chestplateItem = slotsToSearch.findSlot { it.isChestArmor }

        val chestplateStack = chestplateSlot.itemStack
        when {
            // put on elytra
            chestplateStack.isEmpty && elytraItem != null -> event.doSwap(elytraItem)

            // replacing of elytra with a chestplate
            chestplateStack.isElytra() && chestplateItem != null -> event.doSwap(chestplateItem)

            // replacing the chestplate with elytra
            chestplateStack.isChestArmor && elytraItem != null -> event.doSwap(elytraItem)
        }

        enabled = false
    }

    private fun ScheduleInventoryActionEvent.doSwap(slot: ItemSlot) {
        var exchange: InventoryAction? = null
        if (!chestplateSlot.itemStack.isEmpty) {
            exchange = InventoryAction.Click.performPickup(slot = slot)
        }

        val actions = listOfNotNull(
            InventoryAction.Click.performPickup(slot = slot),
            InventoryAction.Click.performPickup(slot = chestplateSlot),
            exchange
        )

        schedule(constraints, actions)
    }

    private fun ItemStack.isElytra() = this.item == Items.ELYTRA

}
