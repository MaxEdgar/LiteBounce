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

package net.maxedgar.coffee.test

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap

/**
 * Initializes the minimal vanilla bootstrap required by tests that touch registry-backed or statically bootstrapped
 * Minecraft classes.
 *
 * @see net.minecraft.SharedConstants.tryDetectVersion
 * @see net.minecraft.server.Bootstrap.bootStrap
 */
object MinecraftBootstrap {

    private val lock = Any()
    @Volatile
    private var initialized = false

    fun ensureInitialized() {
        if (initialized) {
            return
        }

        synchronized(lock) {
            if (initialized) {
                return
            }

            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()

            initialized = true
        }
    }

}
