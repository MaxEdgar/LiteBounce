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

package net.maxedgar.coffee.features.module.modules.misc

import net.ccbluex.fastutil.objectLinkedSetOf
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.text.repeat

/**
 * TextFieldProtect Module
 *
 * Hides rendered text of text field widget when it matches certain patterns.
 */
object ModuleTextFieldProtect : ClientModule("TextFieldProtect", ModuleCategories.MISC) {

    private val patterns by regexList("Patterns",
        objectLinkedSetOf(
            Regex("^/register.*"),
            Regex("^/login.*"),
            Regex("^/email.*"),
        ),
    )

    private const val MASK_CHAR = '*'

    fun protect(input: String, firstCharacterIndex: Int): String {
        return if (!running || patterns.none { it.matches(input) }) {
            input
        } else {
            MASK_CHAR.repeat(firstCharacterIndex)
        }
    }
}
