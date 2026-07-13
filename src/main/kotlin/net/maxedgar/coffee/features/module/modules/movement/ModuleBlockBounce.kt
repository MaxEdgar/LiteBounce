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
package net.maxedgar.coffee.features.module.modules.movement

import net.maxedgar.coffee.event.events.PlayerJumpEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.block.isBlockAtPosition
import net.maxedgar.coffee.utils.entity.box
import net.minecraft.world.level.block.BedBlock
import net.minecraft.world.level.block.HoneyBlock
import net.minecraft.world.level.block.SlimeBlock

/**
 * BlockBounce module
 *
 * Allows you to bounce higher on bouncy blocks.
 */
object ModuleBlockBounce : ClientModule("BlockBounce", ModuleCategories.MOVEMENT) {

    private val motion by float("Motion", 0.42f, 0.2f..2f)

    @Suppress("unused")
    val jumpHandler = handler<PlayerJumpEvent> { event ->
        if (standingOnBouncyBlock()) {
            event.motion += motion
        }
    }

    private fun standingOnBouncyBlock(): Boolean {
        val boundingBox = player.box
        val detectionBox = boundingBox.setMinY(boundingBox.minY - 0.01)

        return detectionBox.isBlockAtPosition { block ->
            block is SlimeBlock || block is BedBlock || block is HoneyBlock
        }
    }
}
