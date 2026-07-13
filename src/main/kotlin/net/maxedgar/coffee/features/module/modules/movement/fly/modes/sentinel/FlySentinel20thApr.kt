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

package net.maxedgar.coffee.features.module.modules.movement.fly.modes.sentinel

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.PlayerMoveEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.exploit.ModulePingSpoof
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed
import net.maxedgar.coffee.lang.translation
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.entity.withStrafe
import net.maxedgar.coffee.utils.movement.stopXZVelocity
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * @anticheat Sentinel
 * @anticheatVersion 20.04.2024
 * @testedOn cubecraft.net
 *
 * @note Tested in SkyWars - fly as long as you want. REQUIRES PING SPOOF TO BE ENABLED.
 *
 * Thanks to the_bi11iona1re for making me aware that Sentinal folds to Verus Damage exploit.
 */
internal object FlySentinel20thApr : Mode("Sentinel20thApr") {

    private val horizontalSpeed by float("HorizontalSpeed", 3.5f, 0.1f..10f)
    private val constantSpeed by boolean("ConstantSpeed", false)
    private val verticalSpeed by float("VerticalSpeed", 0.7f, 0.1f..1f)
    private val reboostTicks by int("ReboostTicks", 30, 10..50)
    private val boostOnce by boolean("BoostOnce", false)
    private val nostalgia by boolean("Nostalgia", false)

    override val parent: ModeValueGroup<*>
        get() = ModuleFly.modes

    private var hasBeenHurt = false
    private var hasBeenTeleported = false

    override fun enable() {
        if (!ModulePingSpoof.enabled) {
            ModulePingSpoof.enabled = true
        }

        if (ModuleSpeed.enabled) {
            ModuleSpeed.enabled = false
        }

        hasBeenHurt = false
        hasBeenTeleported = false

        chat(regular(translation("liquidbounce.module.fly.messages.cubecraft20thAprBoostUsage")))
        super.enable()
    }

    override fun disable() {
        player.stopXZVelocity()
    }

    val repeatable = tickHandler {
        boost()
        waitTicks(reboostTicks)

        if (boostOnce) {
            ModuleFly.enabled = false
            player.stopXZVelocity()
        }
    }

    val moveHandler = handler<PlayerMoveEvent> { event ->
        if (player.hurtTime > 0  && !hasBeenHurt) {
            hasBeenHurt = true
            player.deltaMovement = player.deltaMovement.withStrafe(speed = horizontalSpeed.toDouble())
            notification(
                "Fly",
                translation("liquidbounce.module.fly.messages.cubecraft20thAprBoostMessage"),
                NotificationEvent.Severity.INFO
            )

            // Nostalgia mode
            if (!hasBeenTeleported && nostalgia) {
                hasBeenTeleported = true
                player.setPos(
                    player.x,
                    player.y + 0.42,
                    player.z
                )
            }
        }

        if (!hasBeenHurt) {
            return@handler
        }

        event.movement.y = when {
            mc.options.keyJump.isDown -> verticalSpeed.toDouble()
            mc.options.keyShift.isDown -> (-verticalSpeed).toDouble()
            else -> 0.0
        }

        if (constantSpeed) {
            event.movement = event.movement.withStrafe(speed = horizontalSpeed.toDouble())
        }
    }

    private fun boost() {
        hasBeenHurt = false
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, false,
            player.horizontalCollision))
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y + 3.25, player.z,
            false, player.horizontalCollision))
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, false,
            player.horizontalCollision))
        network.send(
            ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, true,
            player.horizontalCollision))
    }

}
