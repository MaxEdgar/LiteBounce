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
package net.maxedgar.coffee.utils.client

import net.maxedgar.coffee.additions.realSelectedSlot
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.EventManager
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.SelectHotbarSlotSilentlyEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot

/**
 * Manages things like [ModuleScaffold]'s silent mode.
 * Not thread safe, please only use this on the main-thread of minecraft
 */
object SilentHotbar : EventListener {

    private var hotbarState: SilentHotbarState? = null
    private var ticksSinceLastUpdate: Int = 0

    /**
     * Returns the slot that interactions would take place with
     */
    val serversideSlot: Int
        get() = hotbarState?.enforcedHotbarSlot ?: mc.player?.inventory?.realSelectedSlot ?: 0

    val clientsideSlot: Int
        get() = hotbarState?.clientsideSlot ?: mc.player?.inventory?.realSelectedSlot ?: 0

    /**
     * Silently selects a main-hand hotbar slot for duration of [ticksUntilReset].
     * Offhand is ignored because it is not selected through held-item changes.
     */
    fun selectSlotSilently(requester: Any?, slot: HotbarItemSlot, ticksUntilReset: Int) {
        slot.hotbarIndex?.let {
            selectSlotSilently(requester, it, ticksUntilReset)
        }
    }

    fun selectSlotSilently(requester: Any?, slot: Int, ticksUntilReset: Int) {
        val event = EventManager.callEvent(SelectHotbarSlotSilentlyEvent(requester, slot))
        if (event.isCancelled) {
            return
        }

        hotbarState = SilentHotbarState(slot, requester, ticksUntilReset, clientsideSlot)
        ticksSinceLastUpdate = 0
    }

    fun resetSlot(requester: Any?) {
        if (hotbarState?.requester == requester) {
            hotbarState = null
        }
    }

    fun isSlotModified() = hotbarState != null

    /**
     * Returns if the slot is currently getting modified by a given requester
     */
    fun isSlotModifiedBy(requester: Any?) = hotbarState?.requester == requester

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent>(priority = 1001) {
        val hotbarState = hotbarState ?: return@handler

        if (ticksSinceLastUpdate >= hotbarState.ticksUntilReset) {
            this.hotbarState = null
            return@handler
        }

        ticksSinceLastUpdate++
    }
}

private class SilentHotbarState(
    val enforcedHotbarSlot: Int,
    val requester: Any?,
    val ticksUntilReset: Int,
    val clientsideSlot: Int
)
