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

import net.maxedgar.coffee.event.events.KeybindIsPressedEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickUntil
import net.maxedgar.coffee.features.module.modules.player.autobuff.StatusEffectBasedBuff
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PotionItem
import net.minecraft.world.item.SplashPotionItem

internal object Drink : StatusEffectBasedBuff("Drink") {

    private var forceUseKey = false

    override suspend fun execute(slot: HotbarItemSlot) {
        forceUseKey = true
        tickUntil { !passesRequirements }
        forceUseKey = false
    }

    @Suppress("unused")
    private val keyBindIsPressedHandler = handler<KeybindIsPressedEvent> { event ->
        if (event.keyBinding == mc.options.keyUse && forceUseKey) {
            event.isPressed = true
        }
    }

    override fun onDisabled() {
        forceUseKey = false
        super.onDisabled()
    }

    override fun isValidPotion(stack: ItemStack) =
        stack.item is PotionItem && stack.item !is SplashPotionItem

}
