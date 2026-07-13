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

package net.maxedgar.coffee.features.module.modules.player.autobuff

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.player.autobuff.ModuleAutoBuff.AutoSwap
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.combat.CombatManager
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.inventory.InventoryManager
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.inventory.findClosestSlot
import net.minecraft.world.item.ItemStack

abstract class Buff(
    name: String,
) : ToggleableValueGroup(ModuleAutoBuff, name, true) {

    internal open val passesRequirements: Boolean
        get() = enabled && !InventoryManager.isInventoryOpen

    /**
     * Try to run feature if possible, otherwise return false
     */
    internal suspend fun runIfPossible(): Boolean {
        if (!enabled || !passesRequirements) {
            return false
        }

        // Check if the item is in the hotbar
        val slot = Slots.OffhandWithHotbar.findClosestSlot { isValidItem(it, true) } ?: return false

        CombatManager.pauseCombatForAtLeast(ModuleAutoBuff.combatPauseTime)

        if (slot.isSelected) {
            // Check main hand and offhand
            execute(slot)
            return true
        } else if (AutoSwap.enabled) {
            // Check if we should auto swap
            // todo: do not hardcode ticksUntilReset
            SilentHotbar.selectSlotSilently(ModuleAutoBuff, slot, 300)
            waitTicks(AutoSwap.delayIn.random())
            execute(slot)
            waitTicks(AutoSwap.delayOut.random())
            SilentHotbar.resetSlot(ModuleAutoBuff)
            return true
        } else {
            return false
        }
    }

    abstract fun isValidItem(stack: ItemStack, forUse: Boolean): Boolean

    abstract suspend fun execute(slot: HotbarItemSlot)

}

