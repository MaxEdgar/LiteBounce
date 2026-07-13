/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.utils.math.geometry

import net.maxedgar.coffee.utils.math.isLikelyZero
import net.minecraft.world.phys.Vec3

data class LineSegment(
    val start: Vec3,
    val end: Vec3,
) : LinearGeometry3 {

    init {
        require(!end.subtract(start).isLikelyZero) {
            "Line segment must not have zero length, actual: $start -> $end"
        }
    }

    override val anchor: Vec3
        get() = start

    override val direction: Vec3
        get() = end.subtract(start)

    val length: Double
        get() = direction.length()

}
