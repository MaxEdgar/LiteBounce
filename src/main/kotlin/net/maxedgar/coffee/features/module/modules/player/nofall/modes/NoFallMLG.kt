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
package net.maxedgar.coffee.features.module.modules.player.nofall.modes

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.repeated
import net.maxedgar.coffee.features.module.modules.movement.ModuleFreeze
import net.maxedgar.coffee.features.module.modules.player.nofall.ModuleNoFall
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.block.doPlacement
import net.maxedgar.coffee.utils.block.fallDamageMultiplier
import net.maxedgar.coffee.utils.block.liquid.TimedPickupTracker
import net.maxedgar.coffee.utils.block.liquid.planPlacementAtPos
import net.maxedgar.coffee.utils.block.targetfinding.PlacementPlan
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.entity.FallingPlayer
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.inventory.findClosestSlot
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.raytracing.traceFromPlayer
import net.maxedgar.coffee.utils.world.waterEvaporates
import net.minecraft.world.item.Items

internal object NoFallMLG : NoFallMode("MLG") {
    private const val PICKUP_TRACKER_CAPACITY = 8

    private val minFallDist by float("MinFallDistance", 5f, 2f..50f)

    private object PickupWater : ToggleableValueGroup(NoFallMLG, "PickUpWater", true) {
        /**
         * Don't pick up before the lower bound, don't pick up after the upper bound
         */
        val pickupSpan by intRange("PickupSpan", 200..1000, 0..10000, "ms")
    }

    private val rotations = tree(RotationsValueGroup(this))

    private var currentTarget: PlacementPlan? = null
    private val pickupTracker = TimedPickupTracker(PICKUP_TRACKER_CAPACITY)

    private val netherItems =
        setOf(
            // overworld
            Items.SCAFFOLDING,
            Items.COBWEB,
            Items.POWDER_SNOW_BUCKET,
            Items.HAY_BLOCK,
            Items.SLIME_BLOCK,
            Items.HONEY_BLOCK,
            // nether
            Items.TWISTING_VINES,
        )
    private val normalItems = netherItems + Items.WATER_BUCKET

    private val itemsForMLG
        get() = if (world.waterEvaporates) netherItems else normalItems

    init {
        tree(PickupWater)
    }

    /**
     * We need to sneak for at least 3 ticks to eliminate
     * the fall damage.
     */
    const val SCAFFOLDING_SNEAKING_TICKS = 3

    override val running: Boolean
        get() = super.running && !ModuleFreeze.running

    override fun disable() {
        SilentHotbar.resetSlot(this)
    }

    @Suppress("unused")
    private val tickMovementHandler =
        handler<RotationUpdateEvent> {
            val currentGoal = this.getCurrentGoal()

            this.currentTarget = currentGoal

            if (currentGoal == null) {
                return@handler
            }

            RotationManager.setRotationTarget(
                currentGoal.placementTarget.rotation,
                valueGroup = rotations,
                priority = Priority.IMPORTANT_FOR_PLAYER_LIFE,
                provider = ModuleNoFall,
            )
        }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        val target = currentTarget ?: return@handler

        val rayTraceResult = traceFromPlayer()

        if (!target.doesCorrespondTo(rayTraceResult)) {
            return@handler
        }

        SilentHotbar.selectSlotSilently(this, target.hotbarItemSlot, 1)

        val onSuccess: () -> Boolean = {
            pickupTracker.record(target.targetPos)

            if (target.hotbarItemSlot.itemStack.item == Items.SCAFFOLDING) {
                repeated<MovementInputEvent>(SCAFFOLDING_SNEAKING_TICKS) { event ->
                    event.sneak = true
                }
            }

            true
        }

        doPlacement(
            rayTraceResult,
            hand = target.hotbarItemSlot.useHand,
            onItemUseSuccess = onSuccess,
            onPlacementSuccess = onSuccess,
        )

        currentTarget = null
    }

    /**
     * Finds something to do, either
     * 1. Preventing fall damage by placing something
     * 2. Picking up water which we placed earlier to prevent fall damage
     */
    private fun getCurrentGoal(): PlacementPlan? {
        getCurrentMLGPlacementPlan()?.let {
            return it
        }

        if (PickupWater.enabled) {
            return getCurrentPickupTarget()
        }

        return null
    }

    /**
     * Finds a position to pickup placed water from
     */
    private fun getCurrentPickupTarget(): PlacementPlan? {
        if (!canPickUpWaterSafely()) {
            return null
        }

        val bestPickupItem = Slots.OffhandWithHotbar.findClosestSlot(Items.BUCKET) ?: return null

        // Remove all time outed/invalid pickup targets from the list
        pickupTracker.prune(PickupWater.pickupSpan.last.toLong(), TimedPickupTracker.PickupFilter.WATER)

        val pickupPos = pickupTracker.firstEligible(PickupWater.pickupSpan.first.toLong()) ?: return null
        return planPlacementAtPos(pickupPos, bestPickupItem)
    }

    private fun canPickUpWaterSafely(): Boolean {
        return player.isInWater || player.onGround() || player.fallDistance <= minFallDist
    }

    /**
     * Find a way to prevent fall damage if we are falling.
     */
    private fun getCurrentMLGPlacementPlan(): PlacementPlan? {
        val itemForMLG = Slots.OffhandWithHotbar.findClosestSlot(items = itemsForMLG)

        if (player.fallDistance <= minFallDist || itemForMLG == null) {
            return null
        }

        val collision = FallingPlayer.fromPlayer(player).findCollision(20)?.pos ?: return null

        if (collision.fallDamageMultiplier(player) <= 0f) {
            return null
        }

        return planPlacementAtPos(collision.above(), itemForMLG)
    }
}
