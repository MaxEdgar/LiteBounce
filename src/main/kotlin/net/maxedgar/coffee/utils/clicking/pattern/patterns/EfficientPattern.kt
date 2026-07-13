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
package net.maxedgar.coffee.utils.clicking.pattern.patterns

import net.maxedgar.coffee.utils.clicking.Clicker
import net.maxedgar.coffee.utils.clicking.pattern.ClickPattern

/**
 * Keeps at least one-tick interval between each click.
 */
object EfficientPattern : ClickPattern {

    override fun fill(
        clickArray: IntArray,
        cps: IntRange,
        clicker: Clicker<*>
    ) {
        val clicks = cps.random()

        // Efficient will introduce wide gaps when the CPS is lower than half of the cycle length,
        // so we will use StabilizedPattern instead.
        if (clicks < 10) {
            return StabilizedPattern.fill(clickArray, cps, clicker)
        }

        for (i in 0 until clicks) {
            clickArray[i * 2 % clickArray.size]++
        }
    }

}
