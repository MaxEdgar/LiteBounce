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

import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * Lightweight search-based module selection screen.
 * Replaces the window-based ClickGUI with a simple search bar and filtered module list.
 * Module settings are opened via the existing SettingsWindow + ClickGui system.
 * Inspired by Wurst's Navigator.
 */
class ModuleSearchScreen : Screen(Component.literal("")) {

    private var searchQuery = ""
    private var cursorBlinkTimer = 0f
    private var showCursor = true
    private var scrollOffset = 0

    private val searchBarY = 10
    private val searchBarHeight = 18
    private val itemHeight = 13
    private val itemPadding = 2

    override fun isPauseScreen() = false

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        // Don't blur the background
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sw = width
        val sh = height

        // Semi-transparent dark background overlay (ARGB: alpha=204, rgb=17,17,17)
        context.fill(0, 0, sw, sh, 0xCC111111.toInt())

        // Cursor blink
        cursorBlinkTimer += partialTicks
        if (cursorBlinkTimer >= 12f) {
            cursorBlinkTimer = 0f
            showCursor = !showCursor
        }

        val searchBarX = sw / 4
        val searchBarWidth = sw / 2

        // Render search bar and module list
        renderSearchBar(context, searchBarX, searchBarWidth, mouseX, mouseY)
        renderModuleList(context, searchBarX, searchBarWidth, mouseX, mouseY)

        // Always render ClickGui settings windows on top (if any are open)
        // Uses the existing window system for module settings.
        NativeClickGui.gui.render(context, mouseX, mouseY, partialTicks)
    }

    private fun renderSearchBar(context: GuiGraphicsExtractor, x: Int, width: Int, mouseX: Int, mouseY: Int) {
        val font = mc.font
        val y = searchBarY

        // Search bar background
        context.fill(x, y, x + width, y + searchBarHeight, 0xFF333333.toInt())

        if (searchQuery.isNotEmpty()) {
            val textX = x + 4
            val textY = y + (searchBarHeight - font.lineHeight) / 2
            context.text(font, searchQuery, textX, textY, 0xFFF0F0F0.toInt(), false)

            // Cursor blink
            if (showCursor) {
                val cursorX = textX + font.width(searchQuery)
                context.fill(cursorX, textY, cursorX + 1, textY + font.lineHeight, 0xFFF0F0F0.toInt())
            }
        } else {
            context.text(font, "Search modules...", x + 4, y + (searchBarHeight - font.lineHeight) / 2,
                0xFF888888.toInt(), false)
        }

        // Bottom border
        context.fill(x, y + searchBarHeight, x + width, y + searchBarHeight + 1, 0xFF555555.toInt())
    }

    private fun renderModuleList(context: GuiGraphicsExtractor, x: Int, width: Int, mouseX: Int, mouseY: Int) {
        val font = mc.font
        val startY = searchBarY + searchBarHeight + 4

        val filteredModules = ModuleManager.sortedBy { it.name }
            .filter { it.name.contains(searchQuery, ignoreCase = true) || searchQuery.isEmpty() }
            .filter { !it.disableActivation }

        var currentY = startY + scrollOffset

        for (module in filteredModules) {
            val itemY = currentY
            val itemH = itemHeight

            if (itemY + itemH >= 0 && itemY < height) {
                val hovering = mouseX >= x && mouseY >= itemY && mouseX < x + width && mouseY < itemY + itemH
                val settingsHover = hovering && mouseX >= x + width - 10
                val hasSettings = module.inner.isNotEmpty()

                // Background
                val bgColor = when {
                    module.enabled && settingsHover -> 0xFF3A6A27.toInt()
                    module.enabled -> 0xFF2D5A27.toInt()
                    hovering -> 0xFF3A3A3A.toInt()
                    else -> 0xFF222222.toInt()
                }
                context.fill(x, itemY, x + width, itemY + itemH, bgColor)

                // Module name - use muted green for enabled modules
                val nameColor = if (module.enabled) 0xFF66FF66.toInt() else 0xFFF0F0F0.toInt()
                context.text(font, module.name, x + 3, itemY + 2, nameColor, false)

                // Settings arrow (>) if module has settings
                if (hasSettings) {
                    val arrowColor = if (settingsHover) 0xFFFF8800.toInt() else 0xFF888888.toInt()
                    context.text(font, ">", x + width - 10, itemY + 2, arrowColor, false)
                }
            }

            currentY += itemH + itemPadding
        }
    }

    override fun mouseClicked(context: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = context.x().toInt()
        val mouseY = context.y().toInt()
        val button = context.button()

        // Let ClickGui handle clicks on its settings windows first
        NativeClickGui.gui.handleMouseClick(context)

        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(context, doubleClick)
        }

        val searchBarX = width / 4
        val searchBarWidth = width / 2

        val startY = searchBarY + searchBarHeight + 4

        val filteredModules = ModuleManager.sortedBy { it.name }
            .filter { it.name.contains(searchQuery, ignoreCase = true) || searchQuery.isEmpty() }
            .filter { !it.disableActivation }

        var currentY = startY + scrollOffset
        for (module in filteredModules) {
            val itemY = currentY
            val itemH = itemHeight

            if (mouseX >= searchBarX && mouseY >= itemY &&
                mouseX < searchBarX + searchBarWidth && mouseY < itemY + itemH) {

                val hasSettings = module.inner.isNotEmpty()
                val settingsAreaX = searchBarX + searchBarWidth - 10

                if (hasSettings && mouseX >= settingsAreaX) {
                    // Open module settings via existing ClickGui/SettingsWindow system
                    val parentWindow = Window(module.name)
                    parentWindow.setX((searchBarX + searchBarWidth) / 2)
                    parentWindow.setY(itemY.coerceAtMost(height / 2))
                    parentWindow.setMinimizable(false)
                    parentWindow.setPinnable(false)
                    parentWindow.pack()

                    SettingsWindow(module, parentWindow, 20).also {
                        NativeClickGui.gui.addWindow(it)
                    }
                } else {
                    module.enabled = !module.enabled
                }
                return true
            }

            currentY += itemH + itemPadding
        }

        return super.mouseClicked(context, doubleClick)
    }

    override fun mouseReleased(context: MouseButtonEvent): Boolean {
        NativeClickGui.gui.handleMouseRelease(context.x(), context.y(), context.button())
        return super.mouseReleased(context)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        // Let ClickGui handle scroll on its windows first
        NativeClickGui.gui.handleMouseScroll(mouseX, mouseY, verticalAmount)

        // Also scroll the module list
        val scrollDelta = (verticalAmount * 20).toInt()
        scrollOffset += scrollDelta
        scrollOffset = scrollOffset.coerceAtMost(0)
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val codePoint = event.codepoint()
        if (!Character.isISOControl(codePoint)) {
            searchQuery += codePoint
            scrollOffset = 0
            return true
        }
        return super.charTyped(event)
    }

    override fun onClose() {
        // Close any lingering settings windows when the search screen closes
        NativeClickGui.gui.closeAllWindows()
        super.onClose()
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (input.key == GLFW.GLFW_KEY_ESCAPE) {
            onClose()
            return true
        }

        if (input.key == GLFW.GLFW_KEY_BACKSPACE) {
            if (searchQuery.isNotEmpty()) {
                searchQuery = searchQuery.dropLast(1)
                scrollOffset = 0
            }
            return true
        }

        if (input.key == GLFW.GLFW_KEY_ENTER) {
            val filtered = ModuleManager.sortedBy { it.name }
                .filter { it.name.contains(searchQuery, ignoreCase = true) || searchQuery.isEmpty() }
                .filter { !it.disableActivation }
            if (filtered.isNotEmpty()) {
                filtered.first().enabled = !filtered.first().enabled
            }
            return true
        }

        return super.keyPressed(input)
    }

}
