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
package net.maxedgar.coffee.features.module.modules.render

import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.event.EventManager
import net.maxedgar.coffee.event.events.DisconnectEvent
import net.maxedgar.coffee.event.events.ScreenEvent
import net.maxedgar.coffee.event.events.SpaceSeperatedNamesChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.misc.HideAppearance.isDestructed
import net.maxedgar.coffee.features.misc.HideAppearance.isHidingNow
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.render.clickgui.EnabledModulesHUD
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.markAsError
import net.minecraft.client.gui.screens.DisconnectedScreen
import net.minecraft.client.gui.screens.LevelLoadingScreen

/**
 * Module HUD
 *
 * The client in-game dashboard.
 * Now uses native rendering instead of browser-based overlay.
 */

object ModuleHud : ClientModule("HUD", ModuleCategories.RENDER, state = true, hide = true) {

    override val running
        get() = this.enabled && !isDestructed
    override val baseKey: String
        get() = "${ConfigSystem.KEY_PREFIX}.module.hud"

    private val isVisible: Boolean
        get() = !isHidingNow && inGame

    private val enabledModulesHUD = EnabledModulesHUD()

    @Suppress("unused")
    private val spaceSeperatedNames by boolean("SpaceSeperatedNames", true).onChange { state ->
        EventManager.callEvent(SpaceSeperatedNamesChangeEvent(state))
        state
    }

    val themes = tree(ValueGroup("Themes"))

    val components = tree(ValueGroup("AdditionalComponents"))

    override fun onEnabled() {
        if (isHidingNow) {
            chat(markAsError(message("hidingAppearance")))
        }
    }

    override fun onDisabled() {
        // Handlers auto-unregister when no longer referenced
        // Explicit unregistration not needed for handler delegates
    }

    @Suppress("unused")
    private val screenHandler = handler<ScreenEvent> { event ->
        // HUD is always visible when enabled and in-game
        // No browser overlay to manage
    }

    @Suppress("unused")
    private val disconnectHandler = handler<DisconnectEvent> {
        // Nothing to clean up
    }

}
