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

package net.maxedgar.coffee.render.utils

import net.maxedgar.coffee.config.types.CurveValue
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.utils.client.clientStartDurationMs

abstract class AnimatedValueGroup(name: String) : ValueGroup(name) {
    protected abstract val curve: CurveValue
    private val period by int("Period", 1000, 10..20000, "ms")
    private val symmetric by boolean("Symmetric", true)

    fun current(): Float = if (symmetric) {
        val p = (clientStartDurationMs % (period * 2)) / period.toFloat()
        this.curve.transform(if (p > 1) 2f - p else p)
    } else {
        this.curve.transform((clientStartDurationMs % period) / period.toFloat())
    }
}
