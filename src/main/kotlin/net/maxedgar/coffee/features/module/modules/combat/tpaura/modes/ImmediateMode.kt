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

package net.maxedgar.coffee.features.module.modules.combat.tpaura.modes

import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.combat.tpaura.ModuleTpAura.clicker
import net.maxedgar.coffee.features.module.modules.combat.tpaura.ModuleTpAura.desyncPlayerPosition
import net.maxedgar.coffee.features.module.modules.combat.tpaura.ModuleTpAura.stuckChronometer
import net.maxedgar.coffee.features.module.modules.combat.tpaura.ModuleTpAura.targetSelector
import net.maxedgar.coffee.features.module.modules.combat.tpaura.TpAuraMode
import net.maxedgar.coffee.render.drawLine
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.render.renderEnvironment
import net.maxedgar.coffee.utils.network.MovePacketType
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.entity.squaredBoxedDistanceTo
import net.maxedgar.coffee.utils.math.toVec3f
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.floor

object ImmediateMode : TpAuraMode("Immediate") {

    val repeatable = tickHandler {
        if (!clicker.isClickTick) {
            return@tickHandler
        }

        val playerPosition = player.position()
        val enemyPosition = targetSelector.targets()
            .minByOrNull { it.squaredBoxedDistanceTo(playerPosition) }
            ?.position()
            ?: return@tickHandler

        travel(enemyPosition)
        waitTicks(20)
        travel(playerPosition)
        desyncPlayerPosition = null
    }

    val renderHandler = handler<WorldRenderEvent> { event ->
        event.renderEnvironment {
            desyncPlayerPosition?.let { playerPosition ->
                drawLine(
                    relativeToCamera(player.position().add(0.0, 1.0, 0.0)).toVec3f(),
                    relativeToCamera(playerPosition.add(0.0, 1.0, 0.0)).toVec3f(),
                    Color4b.WHITE.argb,
                )
            }
        }
    }

    val packetHandler = handler<PacketEvent> {
        val packet = it.packet

        if (packet is ServerboundMovePlayerPacket) {
            val position = desyncPlayerPosition ?: return@handler

            // Set the packet position to the player position
            packet.x = position.x
            packet.y = position.y
            packet.z = position.z
            packet.hasPos = true
        } else if (packet is ClientboundPlayerPositionPacket) {
            chat(markAsError("Server setback detected - teleport failed!"))
            stuckChronometer.reset()
            desyncPlayerPosition = null
        }
    }

    private fun travel(position: Vec3) {
        val x = position.x
        val y = position.y
        val z = position.z

        val deltaX = x - player.x
        val deltaY = y - player.y
        val deltaZ = z - player.z

        val times = (floor((abs(deltaX) + abs(deltaY) + abs(deltaZ)) / 10) - 1).toInt()
        val packetToSend = MovePacketType.FULL
        repeat(times) {
            network.send(packetToSend.generatePacket().apply {
                this.x = player.x
                this.y = player.y
                this.z = player.z
                this.yRot = player.yRot
                this.xRot = player.xRot
                this.onGround = player.onGround()
            })
        }

        network.send(packetToSend.generatePacket().apply {
            this.x = x
            this.y = y
            this.z = z
            this.yRot = player.yRot
            this.xRot = player.xRot
            this.onGround = player.onGround()
        })

        desyncPlayerPosition = position
    }

}
