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
package net.maxedgar.coffee.features.module.modules.world.scaffold.techniques

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.block.targetfinding.BlockPlacementTarget
import net.maxedgar.coffee.utils.block.targetfinding.BlockPlacementTargetFindingOptions
import net.maxedgar.coffee.utils.math.geometry.Line
import net.maxedgar.coffee.utils.raytracing.traceFromPlayer
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Pose
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

sealed class ScaffoldTechnique(name: String) : Mode(name) {
    final override val parent: ModeValueGroup<ScaffoldTechnique>
        get() = ModuleScaffold.technique

    abstract fun findPlacementTarget(
        predictedPos: Vec3,
        predictedPose: Pose,
        optimalLine: Line?,
        bestStack: ItemStack
    ): BlockPlacementTarget?

    open fun getRotations(target: BlockPlacementTarget?) = target?.rotation

    open fun getCrosshairTarget(target: BlockPlacementTarget?, rotation: Rotation): BlockHitResult? =
        traceFromPlayer(rotation)

    /**
     * Prioritize the block that is closest to the line, if there was no line found, prioritize the nearest block.
     */
    protected fun priorityComparator(
        predictedPos: Vec3,
        optimalLine: Line?,
    ): Comparator<BlockPos> = if (optimalLine != null) {
        BlockPlacementTargetFindingOptions.leastBlockDistanceToLine(optimalLine)
    } else {
        BlockPlacementTargetFindingOptions.leastBlockDistanceToPos(predictedPos)
    }

}
