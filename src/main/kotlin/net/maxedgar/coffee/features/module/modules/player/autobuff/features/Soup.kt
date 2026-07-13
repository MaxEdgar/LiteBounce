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

package net.maxedgar.coffee.features.module.modules.player.autobuff.features

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.player.autobuff.HealthBasedBuff
import net.maxedgar.coffee.features.module.modules.player.autobuff.features.Soup.DropAfterUse.assumeEmptyBowl
import net.maxedgar.coffee.features.module.modules.player.autobuff.features.Soup.DropAfterUse.wait
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.inventory.useHotbarSlotOrOffhand
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

internal object Soup : HealthBasedBuff("Soup") {

    private object DropAfterUse : ToggleableValueGroup(this, "DropAfterUse", true) {
        val assumeEmptyBowl by boolean("AssumeEmptyBowl", true)
        val wait by intRange("Wait", 1..2, 1..20, "ticks")
    }

    init {
        tree(DropAfterUse)
    }

    override fun isValidItem(stack: ItemStack, forUse: Boolean): Boolean {
        return stack.`is`(Items.MUSHROOM_STEW)
    }

    override suspend fun execute(slot: HotbarItemSlot) {
        // Use item (be aware, it will always return false in this case)
        useHotbarSlotOrOffhand(slot)

        if (DropAfterUse.enabled) {
            waitTicks(wait.random())

            if (assumeEmptyBowl || slot.itemStack.`is`(Items.BOWL) && slot != HotbarItemSlot.OFFHAND) {
                if (player.drop(true)) {
                    player.swing(InteractionHand.MAIN_HAND)
                }
            }
        }
    }


}
