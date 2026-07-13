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
package net.maxedgar.coffee.utils.client

class Chronometer @JvmOverloads constructor(private var lastUpdate: Long = 0) {

    val elapsed: Long
        get() = System.currentTimeMillis() - lastUpdate

    fun elapsedUntil(time: Long) = time - lastUpdate

    @JvmOverloads
    fun hasElapsed(ms: Long = 0) = lastUpdate + ms < System.currentTimeMillis()

    @JvmOverloads
    fun hasAtLeastElapsed(ms: Long = 0) = lastUpdate + ms <= System.currentTimeMillis()

    @JvmOverloads
    fun reset(lastUpdate: Long = System.currentTimeMillis()) {
        this.lastUpdate = lastUpdate
    }

    fun waitForAtLeast(ms: Long) {
        this.lastUpdate = this.lastUpdate.coerceAtLeast(System.currentTimeMillis() + ms)
    }

    override fun toString(): String {
        return "Chronometer(lastUpdate=$lastUpdate)"
    }

}
