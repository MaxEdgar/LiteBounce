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
package net.maxedgar.coffee.features.module.modules.world.traps

import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.killaura.ModuleKillAura
import net.maxedgar.coffee.features.module.modules.combat.criticals.ModuleCriticals
import net.maxedgar.coffee.features.module.modules.world.traps.traps.IgnitionTrapPlanner
import net.maxedgar.coffee.features.module.modules.world.traps.traps.TrapPlayerSimulation
import net.maxedgar.coffee.features.module.modules.world.traps.traps.WebTrapPlanner
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.block.doPlacement
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.combat.CombatManager
import net.maxedgar.coffee.utils.combat.TargetTracker
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.raytracing.traceFromPlayer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

/**
 * Ignite & AutoWeb module
 *
 * Ignite: Automatically sets targets around you on fire.
 * AutoWeb: Automatically places cobwebs at targets around you.
 */
object ModuleAutoTrap : ClientModule("AutoTrap", ModuleCategories.WORLD, aliases = listOf("Ignite", "AutoWeb")) {

    private val range = floatRange("Range", 3.0f..4.5f, 2f..6f)
    private val delay by int("Delay", 20, 0..400, "ticks")
    private val ignoreOpenInventory by boolean("IgnoreOpenInventory", true)

    private val ignitionTrapPlanner = tree(IgnitionTrapPlanner(this))
    private val webTrapPlanner = tree(WebTrapPlanner(this))
    val targetTracker = tree(TargetTracker(range = range))
    private val rotations = tree(RotationsValueGroup(this))

    private var currentPlan: BlockChangeIntent<*>? = null

    private var timeout = false

    override fun onEnabled() {
        resetState()
    }

    override fun onDisabled() {
        resetState()
        SilentHotbar.resetSlot(this)
    }

    private fun resetState() {
        timeout = false
        currentPlan = null
        targetTracker.reset()
    }

    @Suppress("unused")
    private val rotationUpdateHandler = handler<RotationUpdateEvent> {
        if (timeout) {
            return@handler
        }

        if (!ignoreOpenInventory && mc.gui.screen() is AbstractContainerScreen<*>) {
            return@handler
        }

        val enemies = targetTracker.targets()
        TrapPlayerSimulation.runSimulations(enemies)

        currentPlan = webTrapPlanner.plan(enemies) ?: ignitionTrapPlanner.plan(enemies)
        currentPlan?.let { intent ->
            val blockChangeInfo = intent.blockChangeInfo
            if (blockChangeInfo !is BlockChangeInfo.PlaceBlock) {
                currentPlan = null
                return@handler
            }

            RotationManager.setRotationTarget(
                blockChangeInfo.blockPlacementTarget.rotation,
                considerInventory = !ignoreOpenInventory,
                valueGroup = rotations,
                Priority.IMPORTANT_FOR_PLAYER_LIFE,
                this
            )
        }
    }

    @Suppress("unused")
    private val placementHandler = tickHandler {
        if (!ignoreOpenInventory && mc.gui.screen() is AbstractContainerScreen<*>) {
            return@tickHandler
        }

        val plan = currentPlan ?: return@tickHandler
        if (plan.blockChangeInfo !is BlockChangeInfo.PlaceBlock) {
            currentPlan = null
            return@tickHandler
        }

        if (shouldWaitForTiming(plan)) {
            return@tickHandler
        }

        val raycast = traceFromPlayer()
        if (!plan.validate(raycast)) {
            return@tickHandler
        }

        CombatManager.pauseCombatForAtLeast(1)
        SilentHotbar.selectSlotSilently(this, plan.slot, 1)

        var successful = false
        val onSuccess = {
            plan.onIntentFulfilled()
            successful = true
            true
        }

        doPlacement(
            raycast,
            hand = plan.slot.useHand,
            onPlacementSuccess = onSuccess,
            onItemUseSuccess = onSuccess
        )

        if (!successful) {
            return@tickHandler
        }

        timeout = true
        try {
            waitTicks(delay)
        } finally {
            timeout = false
        }
    }

    private fun shouldWaitForTiming(plan: BlockChangeIntent<*>): Boolean {
        return when (plan.timing) {
            IntentTiming.INSTANT -> false

            // Let ongoing combat modules consume the current hit window first, then place during recovery.
            IntentTiming.NEXT_PROPITIOUS_MOMENT -> hasPendingCombatAction() && (
                player.getAttackStrengthScale(0.5f) > 0.9f
                    || ModuleCriticals.wouldDoCriticalHit(ignoreSprint = true)
                )
        }
    }

    private fun hasPendingCombatAction(): Boolean {
        return ModuleKillAura.running && ModuleKillAura.targetTracker.target != null
    }
}
