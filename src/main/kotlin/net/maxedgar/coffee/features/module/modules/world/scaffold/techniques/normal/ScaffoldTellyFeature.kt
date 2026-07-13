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
package net.maxedgar.coffee.features.module.modules.world.scaffold.techniques.normal

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PlayerAfterJumpEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.features.module.modules.world.scaffold.techniques.ScaffoldNormalTechnique
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.entity.airTicks
import net.maxedgar.coffee.utils.entity.moving

/**
 * Telly feature
 *
 * This is based on the telly technique and means that the player will jump when moving.
 * That allows for a faster scaffold.
 * Depending on the SameY setting, we might scaffold upwards.
 *
 * @see ModuleScaffold
 */
object ScaffoldTellyFeature : ToggleableValueGroup(ScaffoldNormalTechnique, "Telly", false) {

    val doNotAim: Boolean
        get() = player.airTicks <= straightTicks &&
                ticksUntilJump >= jumpTicks &&
                !(ModuleScaffold.isTowering && aimOnTower)

    /** New val to determine if the player is telly bridging */
    val isTellyBridging: Boolean
        get() = ticksUntilJump >= jumpTicks && player.moving && enabled

    private var ticksUntilJump = 0

    val resetMode by enumChoice("ResetMode", Mode.RESET)
    private val straightTicks by int("Straight", 0, 0..5, "ticks")
    private val jumpTicksOpt by intRange("Jump", 0..0, 0..10, "ticks")
    private val aimOnTower by boolean("AimOnTower", true)
    private var jumpTicks = jumpTicksOpt.random()

    @Suppress("unused")
    private val gameHandler = handler<GameTickEvent> {
        if (player.onGround()) {
            ticksUntilJump++
        }
    }

    @Suppress("unused")
    private val movementInputHandler = handler<MovementInputEvent> { event ->
        if (!player.moving || ModuleScaffold.blockCount <= 0 || !player.onGround()) {
            return@handler
        }

        val isStraight = RotationManager.currentRotation == null || straightTicks == 0

        when (resetMode) {
            Mode.REVERSE -> event.jump = true
            Mode.RESET -> if (isStraight && ticksUntilJump >= jumpTicks) event.jump = true
        }
    }

    @Suppress("unused")
    private val afterJumpHandler = handler<PlayerAfterJumpEvent> {
        ticksUntilJump = 0
        jumpTicks = jumpTicksOpt.random()
    }

    enum class Mode(override val tag: String) : Tagged {
        REVERSE("Reverse"),
        RESET("Reset")
    }

}
