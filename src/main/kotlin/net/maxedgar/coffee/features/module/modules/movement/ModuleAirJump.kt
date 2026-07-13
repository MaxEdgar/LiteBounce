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
import net.maxedgar.coffee.event.events.BlockShapeEvent
import net.maxedgar.coffee.event.events.PlayerJumpEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.minecraft.world.phys.shapes.Shapes

object ModuleAirJump : ClientModule("AirJump", ModuleCategories.MOVEMENT) {

    val mode by enumChoice("Mode", Mode.JUMP_FREELY)

    private var doubleJump = true

    val allowJump: Boolean
        get() = running && (mode == Mode.JUMP_FREELY || mode == Mode.DOUBLE_JUMP && doubleJump)

    val repeatable = tickHandler {
        if (player.onGround()) {
            doubleJump = true
        }
    }

    @Suppress("unused")
    val jumpEvent = handler<PlayerJumpEvent> {
        if (doubleJump && !player.onGround()) {
            doubleJump = false
        }
    }

    @Suppress("unused")
    val handleBlockBox = handler<BlockShapeEvent> { event ->
        if (mode == Mode.GHOST_BLOCK && event.pos.y < player.blockPosition().y && mc.options.keyJump.isDown) {
            event.shape = Shapes.block()
        }
    }

    enum class Mode(override val tag: String) : Tagged {
        JUMP_FREELY("JumpFreely"),
        DOUBLE_JUMP("DoubleJump"),
        GHOST_BLOCK("GhostBlock"),
    }

}
