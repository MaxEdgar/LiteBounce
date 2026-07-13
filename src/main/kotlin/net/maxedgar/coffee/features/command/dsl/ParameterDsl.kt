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

package net.maxedgar.coffee.features.command.dsl

import net.maxedgar.coffee.features.command.Parameter
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder

inline fun <T : Any> CommandBuilder.addParam(
    block: ParameterBuilder.Companion.() -> ParameterBuilder<T>
): Parameter<T> = ParameterBuilder.block().build().also { parameter(it) }

inline fun <T : Any> CommandBuilder.addParam(
    name: String,
    block: ParameterBuilder<T>.() -> ParameterBuilder<T>
): Parameter<T> = ParameterBuilder.begin<T>(name).block().build().also { parameter(it) }
