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
package net.maxedgar.coffee.features.module.modules.combat.killaura.features

import net.maxedgar.coffee.config.types.group.NoneMode
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.AttackEntityEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.combat.killaura.KillAuraClicker.prepareForAttack
import net.maxedgar.coffee.features.module.modules.combat.killaura.ModuleKillAura
import net.maxedgar.coffee.features.module.modules.combat.killaura.ModuleKillAura.canAttackNow
import net.maxedgar.coffee.features.module.modules.combat.killaura.features.KillAuraFailSwing.additionalRange
import net.maxedgar.coffee.features.module.modules.combat.killaura.features.KillAuraNotifyWhenFail.Box
import net.maxedgar.coffee.features.module.modules.combat.killaura.features.KillAuraNotifyWhenFail.Sound
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.combat.findEnemy
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.entity.squaredBoxedDistanceTo
import net.maxedgar.coffee.utils.kotlin.random
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.HitResult
import kotlin.math.pow

internal object KillAuraFailSwing : ToggleableValueGroup(ModuleKillAura, "FailSwing", false) {

    /**
     * Additional range for fail swing to work
     */
    private val additionalRange by floatRange("AdditionalRange", 2.5f..3f, 0f..10f).onChanged { range ->
        currentAdditionalRange = range.random()
    }
    val mode = modes(this, "NotifyWhenFail", activeIndex = 1) {
        arrayOf(NoneMode(it), Box, Sound)
    }.apply {
        doNotIncludeAlways()
    }

    /**
     * Current additional range - randomized from [additionalRange].
     * This will change every time we attack an entity.
     */
    private var currentAdditionalRange: Float = this.additionalRange.random()

    @Suppress("unused")
    private val attackHandler = handler<AttackEntityEvent>{
        currentAdditionalRange = this.additionalRange.random()
    }

    fun dealWithFakeSwing(target: Entity?) {
        if (!enabled || !canAttackNow()) {
            return
        }

        val range = ModuleKillAura.range.interactionRange + currentAdditionalRange
        val entity = target ?: world.findEnemy(0f, range) ?: return
        val raycastType = mc.hitResult?.type

        if (entity.isRemoved || entity.squaredBoxedDistanceTo(player) > range.pow(2)
            || raycastType != HitResult.Type.MISS) {
            return
        }

        // Make it seem like we are blocking
        KillAuraAutoBlock.makeSeemBlock()

        prepareForAttack {
            // [this.crosshairTarget == null] results in a limited attack speed
            if (interaction.hasMissTime()) {
                mc.missTime = 10
            }

            player.swing(InteractionHand.MAIN_HAND)

            // Notify the user about the failed hit
            KillAuraNotifyWhenFail.notifyForFailedHit(entity, RotationManager.currentRotation
                ?: player.rotation)
            true
        }
    }

}
