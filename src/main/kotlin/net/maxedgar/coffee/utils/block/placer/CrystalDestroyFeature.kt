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
package net.maxedgar.coffee.utils.block.placer

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.utils.aiming.NoRotationMode
import net.maxedgar.coffee.utils.aiming.NormalRotationMode
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.utils.raytraceBox
import net.maxedgar.coffee.utils.block.SwingMode
import net.maxedgar.coffee.utils.client.Chronometer
import net.maxedgar.coffee.utils.combat.attackEntity
import net.maxedgar.coffee.utils.entity.getExplosionDamageFromEntity
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.raytracing.isLookingAtEntity
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.boss.enderdragon.EndCrystal

class CrystalDestroyFeature(eventListener: EventListener, private val module: ClientModule) :
    ToggleableValueGroup(eventListener, "DestroyCrystals", true) {

    private val range by float("Range", 4.5f, 1f..6f)
    private val wallRange by float("WallRange", 4.5f, 0f..6f)
    private val delay by int("Delay", 0, 0..1000, "ms")
    private val swingMode by enumChoice("SwingMode", SwingMode.DO_NOT_HIDE)

    private val rotationMode = modes(this, "RotationMode") {
        arrayOf(NormalRotationMode(it, module, Priority.IMPORTANT_FOR_USAGE_3), NoRotationMode(it, module))
    }

    private val chronometer = Chronometer()

    var currentTarget: EndCrystal? = null
        set(value) {
            if (value != field && value != null) {
                raytraceBox(
                    player.eyePosition,
                    value.boundingBox,
                    range = range.toDouble(),
                    wallsRange = wallRange.toDouble(),
                )?.let { field = value }
            } else {
                field = value
            }
        }

    val repeatable = tickHandler {
        val target = currentTarget ?: return@tickHandler

        if (!chronometer.hasElapsed(delay.toLong())) {
            return@tickHandler
        }

        if (wouldKill(target)) {
            currentTarget = null
            return@tickHandler
        }

        // find the best spot (and skip if no spot was found)
        val (rotation, _) =
            raytraceBox(
                player.eyePosition,
                target.boundingBox,
                range = range.toDouble(),
                wallsRange = wallRange.toDouble(),
            ) ?: return@tickHandler

        rotationMode.activeMode.rotate(rotation, isFinished = {
            isLookingAtEntity(
                toEntity = target,
                rotation = RotationManager.serverRotation,
                range = range.toDouble(),
                throughWallsRange = wallRange.toDouble()
            ) != null
        }, onFinished = {
            if (!chronometer.hasElapsed(delay.toLong())) {
                return@rotate
            }

            val target = currentTarget ?: return@rotate

            if (wouldKill(target)) {
                currentTarget = null
                return@rotate
            }

            attackEntity(target, swingMode)
            chronometer.reset()
            currentTarget = null
        })
    }

    /**
     * Checks whether the crystal would kill us.
     */
    private fun wouldKill(target: EndCrystal): Boolean {
        val health = player.health + player.absorptionAmount
        return health - player.getExplosionDamageFromEntity(target) <= 0f
    }

    @Suppress("unused")
    val destroyEntityHandler = handler<PacketEvent> {
        val target = currentTarget ?: return@handler

        val packet = it.packet
        if (packet is ClientboundRemoveEntitiesPacket && target.id in packet.entityIds) {
            currentTarget = null
        }
    }

    /**
     * This should be called when the module using this destroyer is disabled.
     */
    fun onDisable() {
        currentTarget = null
    }

}
