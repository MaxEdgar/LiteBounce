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
package net.maxedgar.coffee.script.bindings.api

import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.event.EventManager
import net.maxedgar.coffee.features.command.CommandManager
import net.maxedgar.coffee.features.module.ModuleManager
import net.maxedgar.coffee.script.ScriptManager
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.combat.CombatManager

/**
 * The main hub of the ScriptAPI client that provides access to a useful set of members.
 *
 * Access variables using `client` in the script
 * client.getEventManager()...
 * client.getConfigSystem()...
 * client.getModuleManager()...
 *
 * @since 1.0
 */
@Suppress("unused")
object ScriptClient {

    val eventManager = EventManager
    val configSystem = ConfigSystem
    val moduleManager = ModuleManager
    val commandManager = CommandManager
    val scriptManager = ScriptManager
    val combatManager = CombatManager

    /**
     * Shows [message] in the client-chat
     */
    @Suppress("unused")
    @JvmName("displayChatMessage")
    fun displayChatMessage(message: String) = chat(message)

}
