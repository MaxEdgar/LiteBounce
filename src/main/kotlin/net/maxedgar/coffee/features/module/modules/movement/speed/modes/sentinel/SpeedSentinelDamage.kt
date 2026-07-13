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
package net.maxedgar.coffee.features.module.modules.movement.speed.modes.sentinel

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PlayerMoveEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.exploit.ModulePingSpoof
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed
import net.maxedgar.coffee.utils.entity.moving
import net.maxedgar.coffee.utils.entity.withStrafe
import net.maxedgar.coffee.utils.movement.stopXZVelocity
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.entity.MoverType
import kotlin.math.ceil
import kotlin.math.floor

/**
 * @anticheat Sentinel
 * @anticheatVersion 30.06.2024
 * @testedOn cubecraft.net
 */
class SpeedSentinelDamage(override val parent: ModeValueGroup<*>) : Mode("SentinelDamage") {

    private val speed by float("Speed", 0.5f, 0.1f..5f)
    private val reboostTicks by int("ReboostTicks", 30, 10..50)

    private var hasBeenHurt = false
    private var adjusted = false
    private var damageDelay = 0
    private var enabledTime = 0L
    private var externalDamageAdjust = 0
    private var lastDamage = 0L

    override fun enable() {
        if (!ModulePingSpoof.enabled) {
            ModulePingSpoof.enabled = true
        }
        hasBeenHurt = false
        damageDelay = 0
        adjusted = false
        externalDamageAdjust = 0
        enabledTime = System.currentTimeMillis()

        super.enable()
    }

    val repeatable = tickHandler {
        if (!player.moving) {
            return@tickHandler
        }

        if (externalDamageAdjust != 0) {
            waitTicks(externalDamageAdjust)
        }

        lastDamage = System.currentTimeMillis()
        boost()
        waitTicks(reboostTicks)
    }

    override fun disable() {
        player.stopXZVelocity()
    }

    @Suppress("unused")
    private val moveHandler = handler<PlayerMoveEvent> { event ->
        if (ModuleFly.enabled) {
            ModuleSpeed.enabled = false
            return@handler
        }

        if (player.hurtTime > 0  && !hasBeenHurt) {
            hasBeenHurt = true
            damageDelay = floor(enabledTime/50.0).toInt()
            adjusted = true
        } else if (player.hurtTime == 10) {
            externalDamageAdjust = ceil((System.currentTimeMillis() - lastDamage) / 50.0).toInt()
        }

        if (!hasBeenHurt && !adjusted) {
            return@handler
        }

        if (event.type == MoverType.SELF && player.moving) {
            event.movement = event.movement.withStrafe(strength = 1.0, speed = speed.toDouble())
        }
    }

    @Suppress("unused")
    private val movementInputHandler = handler<MovementInputEvent> { event ->
        if (event.directionalInput.isMoving && hasBeenHurt) {
            event.jump = true
        }
    }

    private fun boost() {
        externalDamageAdjust = 0
        hasBeenHurt = false
        enabledTime = System.currentTimeMillis()
        network.send(ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, false, false))
        network.send(
            ServerboundMovePlayerPacket.Pos(
                player.x, player.y + 3.25, player.z,
            false, false))
        network.send(ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, false, false))
        network.send(ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, true, false))
    }

}
