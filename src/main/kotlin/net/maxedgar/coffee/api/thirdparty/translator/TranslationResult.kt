/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
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

package net.maxedgar.coffee.api.thirdparty.translator

import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.text.textOf
import net.minecraft.network.chat.Component

sealed class TranslationResult(
    val isValid: Boolean
) {
    abstract fun toResultText(): Component

    data class Success(
        val origin: String,
        val translation: String,
        val fromLanguage: TranslateLanguage,
        val toLanguage: TranslateLanguage
    ) : TranslationResult(
        origin != translation && fromLanguage != toLanguage
    ) {
        override fun toResultText() = textOf(
            regular("("),
            variable(fromLanguage.literal),
            regular("->"),
            variable(toLanguage.literal),
            regular(") "),
            regular(translation).copyable(copyContent = translation),
        )
    }

    data class Failure(
        val ex: Exception,
    ) : TranslationResult(false) {
        override fun toResultText() = markAsError("Failed to translate (${ex.javaClass.simpleName}): ${ex.message}")
    }
}
