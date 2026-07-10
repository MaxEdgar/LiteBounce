/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.config.types.group.ToggleableValueGroup
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.events.ClickGuiScaleChangeEvent
import net.ccbluex.liquidbounce.event.events.ClickGuiValueChangeEvent
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.ModuleCategories
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.ClickGuiScreen
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.NativeClickGui
import net.ccbluex.liquidbounce.utils.client.inGame
import org.lwjgl.glfw.GLFW

/**
 * ClickGUI module
 *
 * Shows you an easy-to-use menu to toggle and configure modules.
 * Now uses the native ClickGUI instead of the browser-based one.
 */

object ModuleClickGui :
    ClientModule("ClickGUI", ModuleCategories.RENDER, bind = GLFW.GLFW_KEY_RIGHT_SHIFT, disableActivation = true) {

    override val running get() = true

    @Suppress("UnusedPrivateProperty")
    private val scale by float("Scale", 1f, 0.5f..2f).onChanged {
        EventManager.callEvent(ClickGuiScaleChangeEvent(it))
        EventManager.callEvent(ClickGuiValueChangeEvent(this))
    }

    object Snapping : ToggleableValueGroup(this, "Snapping", true) {

        @Suppress("UnusedPrivateProperty", "unused")
        private val gridSize by int("GridSize", 10, 1..100, "px").onChanged {
            EventManager.callEvent(ClickGuiValueChangeEvent(ModuleClickGui))
        }

        init {
            inner.find { it.name == "Enabled" }?.onChanged {
                EventManager.callEvent(ClickGuiValueChangeEvent(ModuleClickGui))
            }
        }
    }

    /**
     * Syncs the ClickGUI state with the module system.
     * Used by commands and auto-config to refresh window state after changes.
     */
    @JvmStatic
    fun sync() {
        // Future: sync window positions, pinned state, etc.
    }

    /**
     * Whether the ClickGUI search bar is currently active.
     * Used by InventoryMove to determine if inputs should be blocked.
     */
    @JvmField
    val isInSearchBar = false

    init {
        tree(Snapping)
    }

    override fun onEnabled() {
        if (!LiquidBounce.isInitialized || !inGame) {
            return
        }

        mc.execute {
            NativeClickGui.open()
        }
        super.onEnabled()
    }

}
