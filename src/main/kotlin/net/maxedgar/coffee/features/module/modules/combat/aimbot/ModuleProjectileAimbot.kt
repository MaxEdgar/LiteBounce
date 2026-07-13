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

package net.maxedgar.coffee.features.module.modules.combat.aimbot

import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import net.maxedgar.coffee.utils.combat.TargetSelector
import net.maxedgar.coffee.utils.entity.handItems
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.render.trajectory.HeldItemTrajectoryResolver

object ModuleProjectileAimbot : ClientModule("ProjectileAimbot", ModuleCategories.COMBAT) {

    private val targetSelector = TargetSelector()
    private val rotations = RotationsValueGroup(this)

    init {
        tree(targetSelector)
        tree(rotations)
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        val target = targetSelector.targets().firstOrNull() ?: return@handler

        val rotation = player.handItems.firstNotNullOfOrNull {
            val trajectoryDescriptor = HeldItemTrajectoryResolver.resolveHeldItemPrimaryShot(
                player,
                it,
                true
            ) ?: return@firstNotNullOfOrNull null

            SituationalProjectileAngleCalculator.calculateAngleForEntity(
                trajectoryDescriptor.trajectoryInfo,
                target
            )
        } ?: return@handler

        RotationManager.setRotationTarget(
            rotation,
            considerInventory = false,
            rotations,
            Priority.IMPORTANT_FOR_USAGE_1,
            ModuleProjectileAimbot
        )
    }



}
