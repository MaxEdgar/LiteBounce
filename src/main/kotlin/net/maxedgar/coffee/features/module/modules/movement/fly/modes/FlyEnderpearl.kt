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

package net.maxedgar.coffee.features.module.modules.movement.fly.modes

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.TransferOrigin
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.player.ModuleFastUse
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.block.isBlockAtPosition
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.entity.box
import net.maxedgar.coffee.utils.entity.withStrafe
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.kotlin.random
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block

internal object FlyEnderpearl : Mode("Enderpearl") {

    override val parent: ModeValueGroup<*>
        get() = ModuleFly.modes

    private val speed by float("Speed", 1f, 0.5f..2f)

    private var threwPearl = false
    private var shouldFly = false

    private val rotations = tree(RotationsValueGroup(this))

    override fun enable() {
        threwPearl = false
        shouldFly = false
    }

    override fun disable() {
        SilentHotbar.resetSlot(this)
        threwPearl = false
        shouldFly = false
    }

    val repeatable = tickHandler {
        if (player.isDeadOrDying || player.isSpectator || player.abilities.instabuild) {
            return@tickHandler
        }

        if (shouldFly) { // Fly after setback/pearl land
            player.deltaMovement = player.deltaMovement.withStrafe(speed = speed.toDouble())

            player.deltaMovement.y = when {
                mc.options.keyJump.isDown -> speed.toDouble()
                mc.options.keyShift.isDown -> -speed.toDouble()
                else -> 0.0
            }

            return@tickHandler
        }

        if (threwPearl) return@tickHandler // Already threw pearl, nothing to do

        // If there isn't a pearl, return
        val slot = Slots.OffhandWithHotbar.findSlot(Items.ENDER_PEARL) ?: return@tickHandler

        if (player.xRot <= 80) {
            RotationManager.setRotationTarget(
                Rotation(player.yRot, (80f..90f).random()),
                valueGroup = rotations,
                provider = ModuleFastUse,
                priority = Priority.IMPORTANT_FOR_USAGE_2
            )
        }

        waitTicks(2)
        SilentHotbar.selectSlotSilently(this, slot, 1)
        interaction.startPrediction(world) { sequence ->
            ServerboundUseItemPacket(slot.useHand, sequence, player.yRot, player.xRot)
        }

        threwPearl = true
    }

    val packetHandler = handler<PacketEvent> { event ->
        if (event.origin == TransferOrigin.OUTGOING && event.packet is ServerboundAcceptTeleportationPacket
            && isABitAboveGround() && threwPearl) { // Pearl landed, accepting teleport -> should fly
            shouldFly = true
        }
    }

    fun isABitAboveGround(): Boolean {
        for (y in 0..5) {
            val boundingBox = player.box
            val detectionBox = boundingBox.setMinY(boundingBox.minY - y)

            if (detectionBox.isBlockAtPosition { it is Block }) return true
        }
        return false
    }
}
