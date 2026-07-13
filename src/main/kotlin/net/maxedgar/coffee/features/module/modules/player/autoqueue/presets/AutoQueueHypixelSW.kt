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

package net.maxedgar.coffee.features.module.modules.player.autoqueue.presets

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.player.autoqueue.ModuleAutoQueue
import net.maxedgar.coffee.features.module.modules.player.autoqueue.ModuleAutoQueue.presets
import net.maxedgar.coffee.utils.inventory.Slots
import net.minecraft.world.item.Items

object AutoQueueHypixelSW : Mode("HypixelSW") {

    override val parent: ModeValueGroup<Mode>
        get() = presets

    private val gameMode by enumChoice("GameMode", SkyWarsGameMode.NORMAL)

    private val hasPaper
        get() = Slots.Hotbar.findSlot(Items.PAPER) != null

    val repeatable = tickHandler {
        if (ModuleAutoQueue.shouldPause) {
            return@tickHandler
        }

        // Check if we have paper in our hotbar
        if (!hasPaper) {
            return@tickHandler
        }

        // Send join command
        network.sendCommand("play ${gameMode.joinName}")
        waitTicks(20)
    }

    @Suppress("unused")
    enum class SkyWarsGameMode(override val tag: String, val joinName: String) : Tagged {
        NORMAL("Normal", "solo_normal"),
        INSANE("Insane", "solo_insane");
    }

}
