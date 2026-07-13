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

package net.maxedgar.coffee.features.module.modules.combat.aimbot.autobow

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.combat.aimbot.ModuleAutoBow
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import net.maxedgar.coffee.utils.combat.TargetPriority
import net.maxedgar.coffee.utils.combat.TargetTracker
import net.maxedgar.coffee.utils.entity.handItems
import net.maxedgar.coffee.utils.entity.usingItemOrNull
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.render.TargetRenderer
import net.maxedgar.coffee.utils.render.trajectory.HeldItemTrajectoryResolver
import net.minecraft.world.item.BowItem
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.TridentItem

/**
 * Automatically shoots with your bow when you aim correctly at an enemy or when the bow is fully charged.
 */
object AutoBowAimbotFeature : ToggleableValueGroup(ModuleAutoBow, "BowAimbot", true) {

    val targetTracker = TargetTracker(TargetPriority.DISTANCE)
    private val rotations = RotationsValueGroup(this)
    private val throughWalls by boolean("ThroughWalls", true)

    init {
        tree(targetTracker)
        tree(rotations)
        tree(TargetRenderer(AutoBowAimbotFeature, targetTracker))
    }

    @Suppress("unused")
    private val tickRepeatable = handler<GameTickEvent> {
        targetTracker.reset()

        // Should check if player is using bow
        val activeStack = player.usingItemOrNull ?: player.handItems.firstOrNull {
            it.item is CrossbowItem && CrossbowItem.isCharged(it)
        }
        val activeItem = activeStack?.item

        if (activeItem !is BowItem && activeItem !is TridentItem && activeItem !is CrossbowItem) {
            return@handler
        }

        val trajectoryDescriptor = HeldItemTrajectoryResolver.resolveHeldItemPrimaryShot(
            player,
            activeStack,
            true
        ) ?: return@handler

        var rotation: Rotation? = null
        val calculator = if (throughWalls) {
            SituationalProjectileAngleCalculator
        } else {
            SituationalProjectileAngleCalculator.VerifyHitResult
        }
        targetTracker.selectFirst { enemy ->
            rotation = calculator.calculateAngleForEntity(trajectoryDescriptor.trajectoryInfo, enemy)
            rotation != null
        } ?: return@handler

        RotationManager.setRotationTarget(
            rotation!!,
            priority = Priority.IMPORTANT_FOR_USAGE_1,
            provider = ModuleAutoBow,
            valueGroup = rotations
        )
    }

}
