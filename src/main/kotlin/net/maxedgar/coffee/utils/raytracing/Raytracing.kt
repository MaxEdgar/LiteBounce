/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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

@file:Suppress("NOTHING_TO_INLINE")

package net.maxedgar.coffee.utils.raytracing

import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.client.world
import net.maxedgar.coffee.utils.entity.rotation
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import kotlin.math.max

inline fun BlockGetter.clip(
    from: Vec3,
    to: Vec3,
    block: ClipContext.Block,
    fluid: ClipContext.Fluid,
    entity: Entity,
): BlockHitResult = this.clip(ClipContext(from, to, block, fluid, entity))

inline fun BlockGetter.clip(
    from: Vec3,
    to: Vec3,
    block: ClipContext.Block,
    fluid: ClipContext.Fluid,
    collisionContext: CollisionContext,
): BlockHitResult = this.clip(ClipContext(from, to, block, fluid, collisionContext))

fun traceFromPlayer(
    rotation: Rotation = RotationManager.currentRotation ?: player.rotation,
    range: Double = max(player.blockInteractionRange(), player.entityInteractionRange()),
    block: ClipContext.Block = ClipContext.Block.OUTLINE,
    includeFluids: Boolean = false,
    tickDelta: Float = 1f,
): BlockHitResult {
    return traceFromPoint(
        range = range,
        block = block,
        includeFluids = includeFluids,
        start = player.getEyePosition(tickDelta),
        direction = rotation.directionVector
    )
}

fun traceFromPoint(
    range: Double = max(player.blockInteractionRange(), player.entityInteractionRange()),
    block: ClipContext.Block = ClipContext.Block.OUTLINE,
    includeFluids: Boolean = false,
    start: Vec3,
    direction: Vec3,
    entity: Entity = mc.cameraEntity!!,
): BlockHitResult {
    val end = start.add(direction.x * range, direction.y * range, direction.z * range)

    return world.clip(
        start,
        end,
        block,
        if (includeFluids) ClipContext.Fluid.ANY else ClipContext.Fluid.NONE,
        entity,
    )
}

/**
 * Allows you to check if a point is behind a wall
 *
 * @see net.minecraft.world.entity.LivingEntity.hasLineOfSight
 */
@JvmOverloads
fun hasLineOfSight(
    eyes: Vec3,
    vec3: Vec3,
    entity: Entity = player,
): Boolean {
    return entity.level().clip(
        eyes,
        vec3,
        ClipContext.Block.COLLIDER,
        ClipContext.Fluid.NONE,
        entity,
    ).type == HitResult.Type.MISS
}
