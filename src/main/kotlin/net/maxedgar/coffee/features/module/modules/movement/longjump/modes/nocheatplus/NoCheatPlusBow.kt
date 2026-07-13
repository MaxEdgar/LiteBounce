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

package net.maxedgar.coffee.features.module.modules.movement.longjump.modes.nocheatplus

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.KeybindIsPressedEvent
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.movement.longjump.ModuleLongJump
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.entity.withStrafe
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.movement.DirectionalInput
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket

/**
 * @anticheat NoCheatPlus
 * @anticheatVersion 3.16.1-SNAPSHOT-sMD5NET-b115s
 * @testedOn eu.loyisa.cn
 */

internal object NoCheatPlusBow : Mode("NoCheatPlusBow") {

    override val parent: ModeValueGroup<*>
        get() = ModuleLongJump.mode

    private var arrowBoost = 0f
    private var shotArrows = 0f

    val rotations = tree(RotationsValueGroup(this))
    private val charged by int("Charged", 4, 3..20)
    val speed by float("Speed", 2.5f, 0f..20f)
    private val arrowsToShoot by int("ArrowsToShoot", 8, 0..20)
    val fallDistance by float("FallDistanceToJump", 0.42f, 0f..2f)

    private var stopMovement = false
    private var forceUseKey = false

    val movementInputHandler = handler<MovementInputEvent> {
        if (stopMovement) {
            it.directionalInput = DirectionalInput.NONE
            stopMovement = false
        }
    }

    @Suppress("unused")
    private val keyBindIsPressedHandler = handler<KeybindIsPressedEvent> { event ->
        if (event.keyBinding == mc.options.keyUse && forceUseKey) {
            event.isPressed = true
        }
    }

    @Suppress("unused")
    private val tickJumpHandler = tickHandler {
        if (arrowBoost <= arrowsToShoot) {
            forceUseKey = true
            RotationManager.setRotationTarget(
                Rotation(player.yRot, -90f),
                valueGroup = rotations,
                priority = Priority.IMPORTANT_FOR_USAGE_2,
                provider = ModuleLongJump
            )

            // Stops moving
            stopMovement = true

            // Shoots arrow
            if (player.ticksUsingItem >= charged) {
                interaction.releaseUsingItem(player)
                shotArrows++
            }
        } else {
            forceUseKey = false
            if (player.isUsingItem) {
                interaction.releaseUsingItem(player)
            }

            shotArrows = 0f
            waitTicks(5)
            player.jumpFromGround()
            player.deltaMovement = player.deltaMovement.withStrafe(speed = speed.toDouble())
            waitTicks(5)
            arrowBoost = 0f
        }
    }

    // what, why two events here?
    @Suppress("unused")
    private val handleMovementInput = handler<MovementInputEvent> {
        if (arrowBoost <= arrowsToShoot) {
            return@handler
        }

        if (player.fallDistance >= fallDistance) {
            it.jump = true
            player.fallDistance = 0.0
        }
    }

    @Suppress("unused")
    private val velocityHandler = handler<PacketEvent> {
        val packet = it.packet

        if (packet is ClientboundSetEntityMotionPacket && packet.id == player.id && shotArrows > 0.0) {
            shotArrows--
            arrowBoost++
        }
    }

    override fun disable() {
        shotArrows = 0.0f
        arrowBoost = 0.0f
    }

}
