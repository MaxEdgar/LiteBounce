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
package net.maxedgar.coffee.features.module.modules.render

import net.maxedgar.coffee.config.types.list.Tagged
import java.text.DecimalFormat

enum class TimeUnit(
    override val tag: String,
) : Tagged {
    TICKS("Ticks") {
        override fun format(ticks: Int): String = ticks.toString()
    },
    SECONDS("Seconds") {
        private val format = DecimalFormat("0.#s")
        override fun format(ticks: Int): String = format.format(ticks * 0.05)
    };

    abstract fun format(ticks: Int): String

}
