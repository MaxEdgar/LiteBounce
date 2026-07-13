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
package net.maxedgar.coffee.features.module.modules.world.autobuild

import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.modules.world.autobuild.ModuleAutoBuild.placer
import net.maxedgar.coffee.utils.block.getState
import net.maxedgar.coffee.utils.collection.Filter
import net.maxedgar.coffee.utils.collection.blockSortedSetOf
import net.maxedgar.coffee.utils.collection.getSlot
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks

object PlatformMode : ModuleAutoBuild.AutoBuildMode("Platform") {

    private val disableOnYChange by boolean("DisableOnYChange", true)
    private val filter by enumChoice("Filter", Filter.WHITELIST)
    private val blocks by blocks("Blocks", blockSortedSetOf(Blocks.OBSIDIAN))
    private val platformSize by int("Size", 3, 1..6)

    private var startY = 0.0

    override fun enabled() {
        startY = player.position().y
    }

    @Suppress("unused")
    private val repeatable = tickHandler {
        if (disableOnYChange && player.position().y != startY) {
            ModuleAutoBuild.enabled = false
        }
    }

    @Suppress("unused")
    private val targetUpdater = handler<RotationUpdateEvent> {
        val blocks1 = hashSetOf<BlockPos>()
        val center = BlockPos.containing(player.position()).below()
        val pos = center.mutable()
        for (x in center.x - platformSize..center.x + platformSize) {
            for (z in center.z - platformSize..center.z + platformSize) {
                pos.x = x
                pos.z = z
                if (pos.getState()!!.canBeReplaced()) {
                    blocks1.add(pos.immutable())
                }
            }
        }

        placer.update(blocks1)
    }

    override fun getSlot(): HotbarItemSlot? = filter.getSlot(blocks)

}
