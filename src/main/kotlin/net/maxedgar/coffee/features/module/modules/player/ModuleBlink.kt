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
package net.maxedgar.coffee.features.module.modules.player

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.BlinkPacketEvent
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.PlayerMovementTickEvent
import net.maxedgar.coffee.event.events.TransferOrigin
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.blink.BlinkManager
import net.maxedgar.coffee.features.blink.BlinkManager.Action
import net.maxedgar.coffee.features.blink.BlinkManager.positions
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.movement.autododge.ModuleAutoDodge
import net.maxedgar.coffee.features.module.modules.player.ModuleBlink.dummyPlayer
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.minecraft.client.player.RemotePlayer
import net.minecraft.network.protocol.game.ServerboundAttackPacket
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityLookup
import java.util.UUID

/**
 * Blink module
 *
 * Suspends packets before they are sent to/received from the server.
 */

object ModuleBlink : ClientModule("Blink", ModuleCategories.PLAYER) {

    private val directions by multiEnumChoice("Directions", TransferOrigin.OUTGOING, canBeNone = false)

    private val dummy by boolean("Dummy", false)
    private val ambush by boolean("Ambush", false)
    private val autoDisable by boolean("AutoDisable", true)

    private object AutoResetOption : ToggleableValueGroup(this, "AutoReset", false) {
        val resetAfter by int("ResetAfter", 100, 1..1000)
        val action by enumChoice("ResetAction", ResetAction.RESET)
    }

    private var dummyPlayer: RemotePlayer? = null
    private var tickCounter = 0

    init {
        tree(AutoResetOption)
    }

    override fun onEnabled() {
        tickCounter = 0
        if (dummy) {
            val clone = RemotePlayer(world, player.gameProfile)

            clone.yHeadRot = player.yHeadRot
            clone.copyPosition(player)
            /**
             * A different UUID has to be set, to avoid [dummyPlayer] from being invisible to [player]
             * @see EntityLookup.add
             */
            clone.setUUID(UUID.randomUUID())
            world.addEntity(clone)

            dummyPlayer = clone
        }
    }

    override fun onDisabled() {
        directions.forEach { BlinkManager.flush(it) }
        removeClone()
    }

    private fun removeClone() {
        val clone = dummyPlayer ?: return

        world.removeEntity(clone.id, Entity.RemovalReason.DISCARDED)
        dummyPlayer = null
    }

    val packetHandler = handler<PacketEvent>(priority = EventPriorityConvention.MODEL_STATE) { event ->
        val packet = event.packet

        if (event.isCancelled || !directions.contains(event.origin)) {
            return@handler
        }

        if (ambush &&
            (packet is ServerboundInteractPacket
                || packet is ServerboundAttackPacket
                || packet is ServerboundSpectatorActionPacket)) {
            enabled = false
            return@handler
        }
    }

    @Suppress("unused")
    private val tickTask = tickHandler {
        if (ModuleAutoDodge.enabled) {
            val playerPosition = positions.firstOrNull() ?: return@tickHandler

            if (ModuleAutoDodge.getInflictedHit(playerPosition) == null) {
                return@tickHandler
            }

            val evadingPacket = ModuleAutoDodge.findAvoidingArrowPosition()

            // We have found no packet that avoids getting hit? Then we default to blinking.
            // AutoDoge might save the situation...
            if (evadingPacket == null) {
                notification(
                    "Blink", "Unable to evade arrow. Blinking.",
                    NotificationEvent.Severity.INFO
                )
                enabled = false
            } else if (evadingPacket.ticksToImpact != null) {
                notification("Blink", "Trying to evade arrow...", NotificationEvent.Severity.INFO)
                BlinkManager.flush(evadingPacket.idx + 1)
            } else {
                notification("Blink", "Arrow evaded.", NotificationEvent.Severity.INFO)
                BlinkManager.flush(evadingPacket.idx + 1)
            }
        }
    }

    @Suppress("unused")
    private val playerMoveHandler = handler<PlayerMovementTickEvent> {
        tickCounter++

        if (AutoResetOption.enabled && tickCounter > AutoResetOption.resetAfter) {
            tickCounter = 0
            when (AutoResetOption.action) {
                ResetAction.RESET -> BlinkManager.cancel()
                ResetAction.BLINK -> {
                    directions.forEach { BlinkManager.flush(it) }
                    dummyPlayer?.copyPosition(player)
                }
            }

            notification("Blink", "Auto reset", NotificationEvent.Severity.INFO)
            if (autoDisable) {
                enabled = false
            }
        }
    }

    @Suppress("unused")
    private val fakeLagHandler = handler<BlinkPacketEvent> { event ->
        if (directions.contains(event.origin)) {
            event.action = Action.QUEUE
        }
    }

    enum class ResetAction(override val tag: String) : Tagged {
        RESET("Reset"),
        BLINK("Blink");
    }

    fun isDummyPlayer(entityId: Int): Boolean {
        return entityId == dummyPlayer?.id
    }
}
