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
package net.maxedgar.coffee.utils.block.hole

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.PlayerPostTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.utils.block.ChunkScanner
import net.maxedgar.coffee.utils.block.MovableRegionScanner
import net.maxedgar.coffee.utils.math.expandToBoundingBox
import net.minecraft.core.BlockPos

object HoleManager : EventListener, MinecraftShortcuts {

    internal val movableRegionScanner = MovableRegionScanner()
    private val activeModules = ReferenceOpenHashSet<HoleManagerSubscriber>()
    private val playerPos = BlockPos.MutableBlockPos()

    override val running: Boolean
        get() = activeModules.isNotEmpty()

    fun subscribe(subscriber: HoleManagerSubscriber) {
        activeModules += subscriber
        if (activeModules.size == 1) {
            ChunkScanner.subscribe(HoleTracker)
            mc.player?.blockPosition()?.let(::updateScanRegion)
        }
    }

    fun unsubscribe(subscriber: HoleManagerSubscriber) {
        activeModules -= subscriber
        if (activeModules.isEmpty()) {
            ChunkScanner.unsubscribe(HoleTracker)
            movableRegionScanner.clearRegion()
        }
    }

    @Suppress("unused")
    private val movementHandler = handler<PlayerPostTickEvent> {
        val currentPos = player.blockPosition()

        // Update when player moves
        if (playerPos.distManhattan(currentPos) >= 4) {
            updateScanRegion(currentPos)
        }
    }

    private fun updateScanRegion(newPlayerPos: BlockPos) {
        playerPos.set(newPlayerPos)

        val horizontalDistance = activeModules.maxOf { it.horizontalDistance() }
        val verticalDistance = activeModules.maxOf { it.verticalDistance() }
        val changedAreas = movableRegionScanner.moveTo(
            playerPos.expandToBoundingBox(
                offsetX = horizontalDistance,
                offsetY = verticalDistance,
                offsetZ = horizontalDistance
            )
        )

        if (changedAreas.none()) {
            return
        }

        val region = movableRegionScanner.currentRegion

        with(HoleTracker) {
            // Remove blocks out of the area
            holes.removeIf { !it.positions.intersects(region) }

            // Update new area
            changedAreas.forEach {
                it.cachedUpdate()
            }
        }
    }

}

interface HoleManagerSubscriber {
    fun horizontalDistance(): Int
    fun verticalDistance(): Int
}
