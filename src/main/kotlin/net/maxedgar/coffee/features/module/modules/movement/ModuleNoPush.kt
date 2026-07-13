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
package net.maxedgar.coffee.features.module.modules.movement

import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories

/**
 * NoPush module
 *
 * Disables pushing from other players and some other situations where someone/something can push.
 */
object ModuleNoPush : ClientModule("NoPush", ModuleCategories.MOVEMENT) {
    private val noPushBy = multiEnumChoice("PushBy",
        NoPushBy.ENTITIES,
        NoPushBy.LIQUIDS
    )

    @JvmStatic
    fun canPush(by: NoPushBy) = !running || by in noPushBy

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (NoPushBy.SINKING !in noPushBy) {
            return@handler
        }

        if (mc.options.keyJump.isDown || mc.options.keyShift.isDown) {
            return@handler
        }

        if ((player.isInWater || player.isInLava) && player.deltaMovement.y < 0) {
            player.deltaMovement.y = 0.0
        }
    }
}

enum class NoPushBy(override val tag: String): Tagged {
    ENTITIES("Entities"),
    BLOCKS("Blocks"),
    FISHING_ROD("FishingRod"),
    LIQUIDS("Liquids"),
    SINKING("Sinking")
}
