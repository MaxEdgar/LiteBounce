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

package net.maxedgar.coffee.render.engine.font.processor

import it.unimi.dsi.fastutil.ints.IntList
import net.maxedgar.coffee.render.engine.font.FontStyle
import net.maxedgar.coffee.render.engine.type.Color4b

interface ProcessedText {
    val chars: List<ProcessedChar>

    /**
     * Elements: start char index, to char index, ...
     *
     * Size should be even,
     */
    val underlines: IntList

    /**
     * Elements: start char index, to char index, ...
     *
     * Size should be even,
     */
    val strikeThroughs: IntList

    @JvmRecord
    data class ProcessedChar(val char: Char, val font: @FontStyle Int, val obfuscated: Boolean, val color: Color4b)

}
