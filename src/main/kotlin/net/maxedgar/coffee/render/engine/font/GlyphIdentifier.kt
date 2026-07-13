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

package net.maxedgar.coffee.render.engine.font

import net.maxedgar.coffee.utils.math.high32
import net.maxedgar.coffee.utils.math.longFrom32
import net.maxedgar.coffee.utils.math.low32

@JvmRecord
data class GlyphIdentifier(val codepoint: Char, val style: @FontStyle Int) {
    constructor(fontGlyph: FontGlyph) : this(fontGlyph.codepoint, fontGlyph.font.style)
    constructor(longValue: Long) : this(
        codepoint = unpackCodepoint(longValue),
        style = unpackStyle(longValue),
    )

    fun asLong(): Long = asLong(codepoint, style)

    companion object {
        @JvmStatic
        fun asLong(codepoint: Char, style: @FontStyle Int) = longFrom32(style, codepoint.code)

        @JvmStatic
        fun asLong(fontGlyph: FontGlyph) = asLong(fontGlyph.codepoint, fontGlyph.font.style)

        @JvmStatic
        fun unpackCodepoint(longValue: Long): Char = longValue.low32().toChar()

        @JvmStatic
        fun unpackStyle(longValue: Long): @FontStyle Int = longValue.high32()
    }
}
