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

package net.maxedgar.coffee.features.module.modules.movement.noslow.modes.sneaking

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.EventState
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.utils.client.isNewerThanOrEquals1_21_6
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.network.send1_21_5StartSneaking
import net.maxedgar.coffee.utils.network.send1_21_5StopSneaking
import net.maxedgar.coffee.utils.client.usesViaFabricPlus

internal class NoSlowSneakingSwitch(override val parent: ModeValueGroup<*>) : Mode("Switch") {
    private val timingMode by enumChoice("Timing", TimingMode.PRE_POST)

    override fun enable() {
        if (!usesViaFabricPlus || isNewerThanOrEquals1_21_6) {
            notification(
                "Protocol Error",
                "This mode can only be used on server with version earlier than 1.21.6.",
                NotificationEvent.Severity.ERROR,
            )
        }
        super.enable()
    }

    @Suppress("unused")
    private val networkTickHandler = handler<PlayerNetworkMovementTickEvent> { event ->
        when (timingMode) {
            TimingMode.PRE_POST -> when (event.state) {
                EventState.PRE -> network.send1_21_5StartSneaking()
                EventState.POST -> network.send1_21_5StopSneaking()
            }
            TimingMode.PRE_TICK -> if (event.state == EventState.PRE) {
                network.send1_21_5StartSneaking()
                network.send1_21_5StopSneaking()
            }
            TimingMode.POST_TICK -> if (event.state == EventState.POST) {
                network.send1_21_5StartSneaking()
                network.send1_21_5StopSneaking()
            }
        }
    }

    private enum class TimingMode(override val tag: String) : Tagged {
        PRE_POST("PreAndPost"),
        PRE_TICK("Pre"),
        POST_TICK("Post")
    }
}
