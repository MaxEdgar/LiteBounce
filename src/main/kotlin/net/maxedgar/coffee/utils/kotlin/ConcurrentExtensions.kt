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
@file:Suppress("NOTHING_TO_INLINE")

package net.maxedgar.coffee.utils.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import net.maxedgar.coffee.utils.client.mc
import kotlin.reflect.KProperty

inline operator fun <T> ThreadLocal<T>.getValue(receiver: Any?, property: KProperty<*>): T = get()

inline operator fun <T> ThreadLocal<T>.setValue(receiver: Any?, property: KProperty<*>, value: T) = set(value)

@JvmField
val MinecraftDispatcher = mc.asCoroutineDispatcher()

inline val Dispatchers.Minecraft get() = MinecraftDispatcher

suspend inline fun Array<out Job>.joinAll() = forEach { it.join() }
