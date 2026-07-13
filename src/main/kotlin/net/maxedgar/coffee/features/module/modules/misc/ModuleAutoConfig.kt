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

import kotlinx.coroutines.launch
import net.maxedgar.coffee.config.autoconfig.AutoConfig
import net.maxedgar.coffee.event.eventListenerScope
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.ServerConnectEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.misc.HideAppearance
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.text.dropPort
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.text.rootDomain
import net.minecraft.client.gui.screens.ConnectScreen

object ModuleAutoConfig : ClientModule(
    "AutoConfig",
    ModuleCategories.MISC,
    state = true,
    aliases = listOf("AutoSettings")
) {

    private val blacklistedServer = mutableListOf(
        // Common anticheat test server
        "poke.sexy",
        "loyisa.cn",
        "anticheat-test.com"
    )
    @Volatile
    private var isScheduled = false

    init {
        doNotIncludeAlways()
    }

    override suspend fun enabledEffect() {
        val currentServerEntry = mc.currentServer

        if (currentServerEntry == null) {
            notification(
                "AutoConfig", "You are not connected to a server.",
                NotificationEvent.Severity.ERROR
            )
            return
        }

        loadServerConfig(currentServerEntry.ip.dropPort().rootDomain(), null)
    }

    @Suppress("unused")
    private val handleServerConnect = handler<ServerConnectEvent> { event ->
        if (isScheduled) {
            return@handler
        }

        // This will stop us from connecting to the server right away
        event.cancelEvent()

        eventListenerScope.launch {
            try {
                isScheduled = true
                val address = event.serverInfo.ip.dropPort().rootDomain()

                loadServerConfig(address, event.connectScreen)
            } finally {
                // Proceed to connect to the server
                event.connectScreen.connect(mc, event.address, event.serverInfo, event.cookieStorage)
                isScheduled = false
            }
        }
    }

    /**
     * Loads the config for the given server address
     */
    private suspend fun loadServerConfig(
        address: String,
        connectScreen: ConnectScreen? = null
    ) {
        if (blacklistedServer.any { address.endsWith(it, true) }) {
            notification(
                "Auto Config", "This server is blacklisted.",
                NotificationEvent.Severity.INFO
            )
            return
        }

        // Get config with the shortest name, as it is most likely the correct one.
        // There can be multiple configs for the same server, but with different names
        // and the global config is likely named e.g "hypixel", while the more specific ones are named
        // "hypixel-csgo", "hypixel-legit", etc.
        val autoConfig = (AutoConfig.configs ?: return).filter { config ->
            config.serverAddress?.rootDomain().equals(address, true) ||
                config.serverAddress.equals(address, true)
        }.minByOrNull { config -> config.name.length }

        if (autoConfig == null) {
            notification(
                "Auto Config", "There is no known config for $address.",
                NotificationEvent.Severity.ERROR
            )
            return
        }

        connectScreen?.updateStatus(regular(message("loading", address)))
        runCatching {
            AutoConfig.loadAutoConfig(autoConfig)
        }.onFailure { error ->
            logger.error("Failed to load config ${autoConfig.name} for $address.", error)
            connectScreen?.updateStatus(markAsError(message("failed", address)))
            notification(
                "Auto Config", "Failed to load config ${autoConfig.name}.",
                NotificationEvent.Severity.ERROR
            )
        }.onSuccess {
            connectScreen?.updateStatus(regular(message("loaded", address)))
            notification(
                "Auto Config", "Successfully loaded config ${autoConfig.name}.",
                NotificationEvent.Severity.SUCCESS
            )
        }
    }

    /**
     * Overwrites the condition requirement for being in-game
     */
    override val running
        get() = !HideAppearance.isDestructed && enabled

}
