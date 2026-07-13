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

package net.maxedgar.coffee.features.module.modules.player

import net.maxedgar.coffee.event.events.ScheduleInventoryActionEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.collection.Filter
import net.maxedgar.coffee.utils.collection.itemSortedSetOf
import net.maxedgar.coffee.utils.inventory.CheckScreenHandlerTypeValueGroup
import net.maxedgar.coffee.utils.inventory.CheckScreenTitleValueGroup
import net.maxedgar.coffee.utils.inventory.InventoryAction
import net.maxedgar.coffee.utils.inventory.PlayerInventoryConstraints
import net.maxedgar.coffee.utils.inventory.findItemsInContainer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen

/**
 * ChestCleaner module
 *
 * Automatically drops unwanted items from a chest.
 *
 */
object ModuleChestCleaner : ClientModule(
    "ChestCleaner", ModuleCategories.PLAYER,
    aliases = listOf("ContainerCleaner")
) {
    private val filter by enumChoice("Filter", Filter.WHITELIST)
    private val itemsList by items("Items", itemSortedSetOf())
    private val autoClose by boolean("AutoClose", true)

    private val inventoryConstraints = tree(PlayerInventoryConstraints())
    private val checkScreenHandlerType = tree(CheckScreenHandlerTypeValueGroup(this))
    private val checkScreenTitle = tree(CheckScreenTitleValueGroup(this))

    @Suppress("unused")
    private val scheduleInventoryAction = handler<ScheduleInventoryActionEvent> { event ->
        val screen = mc.gui.screen() as? AbstractContainerScreen<*> ?: return@handler
        if (screen is InventoryScreen) return@handler
        if (!checkScreenHandlerType.isValid(screen) || !checkScreenTitle.isValid(screen)) return@handler

        val slots = screen.findItemsInContainer()
        val selectedSlots = slots.filter { !it.itemStack.isEmpty && filter(it.itemStack.item, itemsList) }

        if (selectedSlots.isEmpty()) {
            if (autoClose) {
                event.schedule(inventoryConstraints, InventoryAction.CloseScreen(screen))
            }
        } else {
            val actions = selectedSlots.map { slot -> InventoryAction.Click.performThrow(screen, slot) }
            event.schedule(inventoryConstraints, actions)
        }
    }
}
