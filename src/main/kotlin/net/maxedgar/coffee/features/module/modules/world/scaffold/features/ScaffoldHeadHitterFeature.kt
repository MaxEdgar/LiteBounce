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
package net.maxedgar.coffee.features.module.modules.world.scaffold.features

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.world.scaffold.techniques.ScaffoldNormalTechnique
import net.maxedgar.coffee.utils.block.collisionShape
import net.maxedgar.coffee.utils.entity.moving

object ScaffoldHeadHitterFeature : ToggleableValueGroup(ScaffoldNormalTechnique, "HeadHitter", false) {
    private fun canHeadHit() =
        !player.blockPosition().above(2).collisionShape.isEmpty && player.onGround()

    private val jumpDelay by intRange("JumpDelay", 0..0, 0..20, "ticks")
    private var jumpCooldown = 0

    val repeatable = handler<GameTickEvent> {
        if (jumpCooldown > 0) {
            jumpCooldown--
            return@handler
        }

        if (canHeadHit() && player.moving) {
            jumpCooldown = jumpDelay.random()
            player.jumpFromGround()
        }
    }
}
