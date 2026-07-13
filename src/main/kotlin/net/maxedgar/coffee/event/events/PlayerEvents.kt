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

package net.maxedgar.coffee.event.events

import net.maxedgar.coffee.annotations.Tag
import net.maxedgar.coffee.event.CancellableEvent
import net.maxedgar.coffee.event.Event
import net.maxedgar.coffee.event.EventState
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.Vec3

// Entity events bound to client-user entity
@Tag("healthUpdate")
class HealthUpdateEvent(val health: Float, val food: Int, val saturation: Float, val previousHealth: Float) : Event()

@Tag("death")
object DeathEvent : Event()

@Tag("playerTick")
class PlayerTickEvent : CancellableEvent()

@Tag("playerPostTick")
object PlayerPostTickEvent : Event()

@Tag("playerMovementTick")
object PlayerMovementTickEvent : Event()

@Tag("playerNetworkMovementTick")
class PlayerNetworkMovementTickEvent(
    val state: EventState,
    var x: Double,
    var y: Double,
    var z: Double,
    var ground: Boolean
) : CancellableEvent()

@Tag("playerPushOut")
class PlayerPushOutEvent : CancellableEvent()

@Tag("playerMove")
class PlayerMoveEvent(val type: MoverType, var movement: Vec3) : Event()

@Tag("playerJump")
class PlayerJumpEvent(var motion: Float, var yaw: Float) : CancellableEvent()

@Tag("playerAfterJump")
object PlayerAfterJumpEvent : Event()

@Tag("playerUseMultiplier")
class PlayerUseMultiplier(var forward: Float, var sideways: Float) : Event()

@Tag("playerSneakMultiplier")
class PlayerSneakMultiplier(var multiplier: Double) : Event()

/**
 * Warning: UseHotbarSlotOrOffHand won't stimulate this event
 */
@Tag("playerInteractItem")
class PlayerInteractItemEvent(val player: Player, val hand: InteractionHand) : CancellableEvent()

@Tag("playerInteractedItem")
class PlayerInteractedItemEvent(
    val player: Player,
    val hand: InteractionHand,
    val actionResult: InteractionResult,
) : Event()

@Tag("playerStrafe")
class PlayerVelocityStrafe(val movementInput: Vec3, val speed: Float, val yaw: Float, var velocity: Vec3) : Event()

@Tag("playerStride")
class PlayerStrideEvent(var strideForce: Float) : Event()

@Tag("playerSafeWalk")
class PlayerSafeWalkEvent(var isSafeWalk: Boolean = false) : Event()

@Tag("playerStep")
class PlayerStepEvent(var height: Float) : Event()

@Tag("playerStepSuccess")
class PlayerStepSuccessEvent(val movementVec: Vec3, var adjustedVec: Vec3) : Event()

@Tag("playerFluidCollisionCheck")
class PlayerFluidCollisionCheckEvent(val fluid: TagKey<Fluid>) : CancellableEvent()
