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
package net.maxedgar.coffee.features.module.modules.movement.inventorymove.features

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.BlinkPacketEvent
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.ScreenEvent
import net.maxedgar.coffee.event.events.TransferOrigin
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.blink.BlinkManager
import net.maxedgar.coffee.features.module.modules.movement.inventorymove.ModuleInventoryMove
import net.maxedgar.coffee.features.module.modules.player.ModuleBlink
import net.maxedgar.coffee.utils.client.Chronometer
import net.maxedgar.coffee.utils.text.formatAsTime
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.network.isC2SContainerPacket
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object InventoryMoveBlinkFeature : ToggleableValueGroup(ModuleInventoryMove, "Blink", false) {

    /**
     * After reaching this time, we will close the inventory and blink.
     */
    private val maximumTime by int("MaximumTime", 10000, 0..30000, "ms")

    private val chronometer = Chronometer()

    @Suppress("unused")
    private val fakeLagHandler = handler<BlinkPacketEvent> { event ->
        val packet = event.packet

        if (mc.gui.screen() is AbstractContainerScreen<*> && event.origin == TransferOrigin.OUTGOING) {
            event.action = when {
                packet.isC2SContainerPacket() -> BlinkManager.Action.PASS
                else -> BlinkManager.Action.QUEUE
            }
        }
    }

    @Suppress("unused")
    val screenHandler = handler<ScreenEvent> { event ->
        if (event.screen is AbstractContainerScreen<*>) {
            chronometer.reset()

            notification(
                "InventoryMove",
                ModuleBlink.message("blinkStart", maximumTime.formatAsTime()),
                NotificationEvent.Severity.INFO
            )
        }
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (mc.gui.screen() is AbstractContainerScreen<*> && chronometer.hasElapsed(maximumTime.toLong())) {
            player.closeContainer()
            notification(
                "InventoryMove",
                ModuleBlink.message("blinkEnd"),
                NotificationEvent.Severity.INFO
            )
        }
    }

}
