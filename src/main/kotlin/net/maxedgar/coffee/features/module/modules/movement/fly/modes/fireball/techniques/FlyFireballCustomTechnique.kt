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

package net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball.techniques

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PlayerMoveEvent
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.sequenceHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball.FlyFireball
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.movement.DirectionalInput
import net.minecraft.world.entity.MoverType

object FlyFireballCustomTechnique : Mode("Custom") {

    override val parent: ModeValueGroup<Mode>
        get() = FlyFireball.technique

    private val disableDelay by int("DisableDelay", 10, 0..20)
    private val throwDelay by int("ThrowDelay", 2, 0..20)

    object Jump : ToggleableValueGroup(this, "Jump", true) {
        val delay by int("JumpDelay", 1, 0..20, "ticks")
    }

    object YVelocity : ToggleableValueGroup(this, "YVelocity", true) {
        val velocity by float("Velocity", 0f, -5f..5f)
        val delay by int("Delay", 0, 0..20, "ticks")
    }

    val sprint by boolean("Sprint", true)
    //  Stop moving when module is active to avoid falling off, for example a bridge
    val stopMove by boolean("StopMove", true)

    object Rotations : RotationsValueGroup(this) {
        val pitch by float("Pitch", 90f, 0f..90f)
    }

    var canMove = true

    init {
        tree(Jump)
        tree(YVelocity)
        tree(Rotations)
    }

    @Suppress("unused")
    private val rotationUpdateHandler = handler<RotationUpdateEvent> {
        RotationManager.setRotationTarget(
            Rotation(player.yRot, Rotations.pitch),
            valueGroup = Rotations,
            priority = Priority.IMPORTANT_FOR_PLAYER_LIFE,
            provider = ModuleFly
        )
    }

    @Suppress("unused")
    private val movementInputHandler = sequenceHandler<MovementInputEvent> { event ->
        if (stopMove && !canMove) {
            event.directionalInput = DirectionalInput.BACKWARDS // Cancel out movement.
        }
    }

    @Suppress("unused")
    val playerMoveHandler = sequenceHandler<PlayerMoveEvent> {
        if (it.type != MoverType.SELF) return@sequenceHandler

        if (player.onGround()) {
            if (Jump.enabled) {
                waitTicks(Jump.delay)
                player.jumpFromGround()
            }

            waitTicks(throwDelay)

            FlyFireball.throwFireball()

            if (sprint) {
                player.isSprinting = true
            }
        }

        if (YVelocity.enabled) {
            waitTicks(YVelocity.delay)
            player.deltaMovement.y = YVelocity.velocity.toDouble()
        }

        waitTicks(disableDelay)

        ModuleFly.enabled = false // Disable after the fireball was thrown
        canMove = true
        FlyFireball.wasTriggered = false
    }

}
