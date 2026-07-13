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
package net.maxedgar.coffee.features.module.modules.movement.noslow.modes.bow

import net.ccbluex.fastutil.enumSetOf
import net.maxedgar.coffee.config.types.group.NoneMode
import net.maxedgar.coffee.features.module.modules.movement.noslow.NoSlowUseActionHandler
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.shared.NoSlowNoBlockInteract
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.shared.NoSlowSharedGrim2360
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.shared.NoSlowSharedGrim2364MC18
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.shared.NoSlowSharedGrim2371
import net.maxedgar.coffee.features.module.modules.movement.noslow.modes.shared.NoSlowSharedInvalidHand
import net.maxedgar.coffee.utils.client.inGame
import net.minecraft.world.item.ItemUseAnimation

internal object NoSlowBow : NoSlowUseActionHandler("Bow") {

    private val animations = enumSetOf(
        ItemUseAnimation.BOW,
        ItemUseAnimation.CROSSBOW,
        ItemUseAnimation.TRIDENT,
    )

    val modes = modes(this, "Choice") {
        arrayOf(
            NoneMode(it),
            NoSlowSharedGrim2360(it),
            NoSlowSharedGrim2364MC18(it),
            NoSlowSharedGrim2371(it),
            NoSlowSharedInvalidHand(it),
        )
    }

    override val running: Boolean
        get() {
            if (!super.running || !inGame) {
                return false
            }

            // Check if we are using a block item
            return player.isUsingItem && player.useItem.useAnimation in animations
        }

    @Suppress("unused")
    private val noBlockInteract = tree(NoSlowNoBlockInteract(this, animations::contains))

}
