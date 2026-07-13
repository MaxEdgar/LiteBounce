/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
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
package net.maxedgar.coffee.utils.block.placer

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.utils.aiming.PostRotationExecutor
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.block.getState
import net.maxedgar.coffee.utils.block.targetfinding.BlockPlacementTarget
import net.maxedgar.coffee.utils.client.RestrictedSingleUseAction
import net.maxedgar.coffee.utils.raytracing.raytraceBlock
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.phys.HitResult
import kotlin.math.max

abstract class BlockPlacerRotationMode(
    name: String,
    private val modeValueGroup: ModeValueGroup<BlockPlacerRotationMode>,
    val placer: BlockPlacer
) : Mode(name), MinecraftShortcuts {

    val postMove by boolean("PostMove", false)

    abstract operator fun invoke(isSupport: Boolean, pos: BlockPos, placementTarget: BlockPlacementTarget): Boolean

    open fun getVerificationRotation(targetedRotation: Rotation) = targetedRotation

    open fun onTickStart() {}

    override val parent: ModeValueGroup<*>
        get() = modeValueGroup

}

/**
 * Normal rotations.
 * Only one placement per tick is possible, possible less because rotating takes some time.
 */
class NormalRotationMode(modeValueGroup: ModeValueGroup<BlockPlacerRotationMode>, placer: BlockPlacer)
    : BlockPlacerRotationMode("Normal", modeValueGroup, placer) {

    val rotations = tree(RotationsValueGroup(this))

    override fun invoke(isSupport: Boolean, pos: BlockPos, placementTarget: BlockPlacementTarget): Boolean {
        val interactedBlockPos = placementTarget.interactedBlockPos
        RotationManager.setRotationTarget(
            placementTarget.rotation,
            considerInventory = !placer.ignoreOpenInventory,
            valueGroup = rotations,
            provider = placer.module,
            priority = placer.priority,
            whenReached = RestrictedSingleUseAction({
                val raytraceResult = raytraceBlock(
                    max(placer.range, placer.wallRange).toDouble(),
                    RotationManager.currentRotation ?: return@RestrictedSingleUseAction false,
                    interactedBlockPos,
                    interactedBlockPos.getState()!!
                ) ?: return@RestrictedSingleUseAction false

                raytraceResult.type == HitResult.Type.BLOCK && raytraceResult.blockPos == interactedBlockPos
            }, {
                PostRotationExecutor.addTask(placer.module, postMove, priority = true) {
                    if (placer.ticksToWait > 0) {
                        return@addTask
                    }

                    placer.doPlacement(isSupport, pos, placementTarget)
                    placer.ranAction = true
                }
            })
        )

        return true
    }

    override fun getVerificationRotation(targetedRotation: Rotation): Rotation = RotationManager.serverRotation

}

/**
 * No rotations, or just a packet containing the rotation target.
 */
class NoRotationMode(modeValueGroup: ModeValueGroup<BlockPlacerRotationMode>, placer: BlockPlacer)
    : BlockPlacerRotationMode("None", modeValueGroup, placer) {

    val send by boolean("SendRotationPacket", false)

    /**
     * Not rotating properly allows doing multiple placements. "b/o" stands for blocker per operation.
     */
    private val placements by int("Placements", 1, 1..10, "b/o")

    private var placementsDone = 0

    override fun invoke(isSupport: Boolean, pos: BlockPos, placementTarget: BlockPlacementTarget): Boolean {
        PostRotationExecutor.addTask(placer.module, postMove, task = {
            if (placer.ticksToWait > 0) {
                return@addTask
            }

            if (send) {
                val rotation = placementTarget.rotation.normalize()
                network.send(
                    ServerboundMovePlayerPacket.Rot(rotation.yaw, rotation.pitch, player.onGround(),
                        player.horizontalCollision)
                )
            }

            placer.doPlacement(isSupport, pos, placementTarget)
            placer.ranAction = true
        })

        placementsDone++
        return placementsDone == placements
    }

    override fun onTickStart() {
        placementsDone = 0
    }

}
