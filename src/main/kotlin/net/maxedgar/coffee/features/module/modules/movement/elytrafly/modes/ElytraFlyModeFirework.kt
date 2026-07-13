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
package net.maxedgar.coffee.features.module.modules.movement.elytrafly.modes

import net.maxedgar.coffee.additions.shooter
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.ScheduleInventoryActionEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.inventory.InventoryAction
import net.maxedgar.coffee.utils.inventory.PlayerInventoryConstraints
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.inventory.useHotbarSlotOrOffhand
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.Items

internal object ElytraFlyModeFirework : ElytraFlyMode("Firework") {

    private object ConsiderInventory : ToggleableValueGroup(this, "ConsiderInventory", enabled = false) {
        val constraints = tree(PlayerInventoryConstraints())
    }

    init {
        tree(ConsiderInventory)
    }

    private val cooldown by intRange("Cooldown", 20..20, 0..300, "ticks")

    private val ALL_WITHOUT_ARMOR = Slots.OffhandWithHotbar + Slots.Inventory
    private val slotsToSearch get() = if (ConsiderInventory.enabled) ALL_WITHOUT_ARMOR else Slots.OffhandWithHotbar

    private fun shouldUseFirework(): Boolean {
        return if (!player.isFallFlying or player.isUsingItem) {
            false
        } else {
            world.entitiesForRendering().none {
                it is FireworkRocketEntity && it.shooter === player
            }
        }
    }

    private var skipTicks = 0

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        skipTicks--
    }

    @Suppress("unused")
    private val scheduleInventoryActionHandler = handler<ScheduleInventoryActionEvent> { event ->
        if (skipTicks > 0 || !shouldUseFirework()) return@handler

        val fireworkSlot = slotsToSearch.findSlot(Items.FIREWORK_ROCKET) ?: return@handler
        if (fireworkSlot is HotbarItemSlot) {
            useHotbarSlotOrOffhand(fireworkSlot)
        } else {
            val targetSlot = if (HotbarItemSlot.OFFHAND.canBeSwapTarget) {
                HotbarItemSlot.OFFHAND
            } else {
                HotbarItemSlot(player.inventory.selectedSlot)
            }

            val actions = listOf<InventoryAction>(
                InventoryAction.Click.performSwap(from = fireworkSlot, to = targetSlot),
                InventoryAction.UseItem(targetSlot, this),
                InventoryAction.Click.performSwap(from = fireworkSlot, to = targetSlot),
            )
            event.schedule(ConsiderInventory.constraints, actions)
        }

        skipTicks = cooldown.random()
    }
}
