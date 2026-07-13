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

package net.maxedgar.coffee.features.module.modules.player

import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.injection.mixins.minecraft.client.MinecraftAccessor
import net.maxedgar.coffee.utils.block.getBlock
import net.maxedgar.coffee.utils.block.getState
import net.maxedgar.coffee.utils.block.isInteractable
import net.maxedgar.coffee.utils.item.isInteractable
import net.minecraft.world.phys.BlockHitResult

/**
 * NoBlockInteract module
 *
 * Allows to use items without interacting with blocks.
 */
object ModuleNoBlockInteract : ClientModule("NoBlockInteract", ModuleCategories.PLAYER) {

    private var sneaking = false
    private var interacting = false

    fun startSneaking() {
        sneaking = true
    }

    @Suppress("unused")
    private val handleMovementInput = handler<MovementInputEvent> { event ->
        if (sneaking) {
            event.sneak = true
            interacting = true
        }
    }

    @Suppress("unused")
    private val handleGameTick = handler<GameTickEvent> {
        if (interacting) {
            (mc as MinecraftAccessor).callStartUseItem()
            interacting = false
            sneaking = false
        }
    }

    fun shouldSneak(blockHitResult: BlockHitResult): Boolean {
        if (player.isShiftKeyDown) {
            return false
        }

        val blockPos = blockHitResult.blockPos
        if (!blockPos.getBlock().isInteractable(blockPos.getState())) {
            return false
        }

        return player.mainHandItem.isInteractable() || player.offhandItem.isInteractable()
    }
}
