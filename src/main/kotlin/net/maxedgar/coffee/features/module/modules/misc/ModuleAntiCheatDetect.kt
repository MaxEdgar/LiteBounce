/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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

import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.client.ServerObserver
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable

/**
 * Module Anti Cheat Detect
 *
 * Attempts to detect the anti-cheat used by the server.
 */
object ModuleAntiCheatDetect : ClientModule("AntiCheatDetect", ModuleCategories.MISC) {

    init {
        doNotIncludeAlways()
    }

    override fun onEnabled() {
        alertAboutAntiCheat()
        super.onEnabled()
    }

    /**
     * Called by [ServerObserver] when enough transactions have been received.
     */
    fun completed() {
        if (enabled) {
            alertAboutAntiCheat()
        }
    }

    private fun alertAboutAntiCheat() {
        val antiCheat = ServerObserver.guessAntiCheat(mc.currentServer?.ip) ?: return
        chat(regular(message("detected", variable(antiCheat))))
    }

}
