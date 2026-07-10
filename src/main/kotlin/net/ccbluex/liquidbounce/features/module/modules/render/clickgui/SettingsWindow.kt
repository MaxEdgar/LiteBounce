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
package net.ccbluex.liquidbounce.features.module.modules.render.clickgui

import net.ccbluex.liquidbounce.features.module.ClientModule
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth

/**
 * Settings window that appears when clicking the settings area of a module button.
 * Recursively adds all nested settings from the module's value tree.
 * Adapted from Wurst7's SettingsWindow.
 */
class SettingsWindow(module: ClientModule, parent: Window, buttonY: Int) : Window("${module.name} Settings") {

    init {
        // Recursively add all settings, including nested groups
        NativeClickGui.addSettingsToWindow(this, module)

        setClosable(true)
        setMinimizable(false)
        setMaxHeight(200)
        pack()

        setInitialPosition(parent, buttonY)
    }

    private fun setInitialPosition(parent: Window, buttonY: Int) {
        val scroll = if (parent.isScrollingEnabled()) parent.getScrollOffset() else 0
        var x = parent.getX() + parent.getWidth() + 5
        var y = parent.getY() + 12 + buttonY + scroll

        val mcWindow = Minecraft.getInstance().window
        if (x + getWidth() > mcWindow.guiScaledWidth) {
            x = parent.getX() - getWidth() - 5
        }
        if (y + getHeight() > mcWindow.guiScaledHeight) {
            y -= getHeight() - 14
        }

        x = Mth.clamp(x, 0, mcWindow.guiScaledWidth)
        y = Mth.clamp(y, 0, mcWindow.guiScaledHeight)

        setX(x)
        setY(y)
    }
}
