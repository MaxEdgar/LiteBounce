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
 * Search-based module selection screen, inspired by Wurst7's Navigator.
 * Replaces the old window-based ClickGUI with a clean search overlay.
 * Press a module to toggle it, click > to open its settings.
 */
class SearchOverlay : Screen(Component.literal("")) {

    private var searchQuery = ""
    private var scrollOffset = 0

    private val searchBarHeight = 20
    private val itemHeight = 14

    override fun isPauseScreen() = false

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        // No background blur
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sw = width
        val sh = height

        // Semi-transparent dark overlay background
        context.fill(0, 0, sw, sh, 0xCC111111.toInt())

        val searchBarX = sw / 4
        val searchBarW = sw / 2
        val searchBarY = 8

        renderSearchBar(context, searchBarX, searchBarY, searchBarW, mouseX, mouseY)
        renderModuleList(context, searchBarX, searchBarY + searchBarHeight + 4, searchBarW, mouseX, mouseY)
    }

    private fun renderSearchBar(context: GuiGraphicsExtractor, x: Int, y: Int, w: Int, mouseX: Int, mouseY: Int) {
        val font = mc.font

        // Search bar background
        context.fill(x, y, x + w, y + searchBarHeight, 0xFF222222.toInt())

        // Bottom border accent
        context.fill(x, y + searchBarHeight, x + w, y + searchBarHeight + 1, 0xFF444444.toInt())

        val textY = y + (searchBarHeight - font.lineHeight) / 2

        if (searchQuery.isNotEmpty()) {
            context.text(font, searchQuery, x + 4, textY, 0xFFF0F0F0.toInt(), false)
        } else {
            context.text(font, "Search modules...", x + 4, textY, 0xFF666666.toInt(), false)
        }
    }

    private fun renderModuleList(context: GuiGraphicsExtractor, x: Int, startY: Int, w: Int, mouseX: Int, mouseY: Int) {
        val font = mc.font

        val filteredModules = ModuleManager.sortedBy { it.name }
            .filter { it.name.contains(searchQuery, ignoreCase = true) || searchQuery.isEmpty() }

        var currentY = startY + scrollOffset

        for (module in filteredModules) {
            val itemY = currentY
            val itemH = itemHeight

            if (itemY + itemH >= 0 && itemY < height) {
                val hovering = mouseX >= x && mouseY >= itemY && mouseX < x + w && mouseY < itemY + itemH
                val hasSettings = module.inner.isNotEmpty()
                val arrowX = x + w - 10
                val settingsHover = hovering && mouseX >= arrowX

                // Module item background
                val bgColor = when {
                    module.enabled && settingsHover -> 0xFF4A7A37.toInt()
                    module.enabled && hovering -> 0xFF3A6A27.toInt()
                    module.enabled -> 0xFF2D5A27.toInt()
                    hovering -> 0xFF333333.toInt()
                    else -> 0xFF1A1A1A.toInt()
                }
                context.fill(x, itemY, x + w, itemY + itemH, bgColor)

                // Module name text color
                val nameColor = if (module.enabled) 0xFF66FF66.toInt() else 0xFFF0F0F0.toInt()
                context.text(font, module.name, x + 3, itemY + 2, nameColor, false)

                // Settings arrow (>)
                if (hasSettings) {
                    val arrowColor = if (settingsHover) 0xFFFFAA00.toInt() else 0xFF555555.toInt()
                    context.text(font, ">", arrowX, itemY + 2, arrowColor, false)
                }
            }

            currentY += itemH + 1
        }
    }

    override fun mouseClicked(context: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = context.x().toInt()
        val mouseY = context.y().toInt()
        val button = context.button()

        val searchBarX = width / 4
        val searchBarW = width / 2
        val searchBarY = 8
        val listStartY = searchBarY + searchBarHeight + 4

        val filteredModules = ModuleManager.sortedBy { it.name }
            .filter { it.name.contains(searchQuery, ignoreCase = true) || searchQuery.isEmpty() }

        var currentY = listStartY + scrollOffset

        for (module in filteredModules) {
            val itemY = currentY
            val itemH = itemHeight

            if (mouseY >= itemY && mouseY < itemY + itemH &&
                mouseX >= searchBarX && mouseX < searchBarX + searchBarW) {

                val hasSettings = module.inner.isNotEmpty()

                // Right click -> open settings screen
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    if (hasSettings) {
                        mc.gui.setScreen(SettingsScreen(module))
                        return true
                    }
                    return super.mouseClicked(context, doubleClick)
                }

                // Left click -> toggle or open settings via > arrow
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    val arrowX = searchBarX + searchBarW - 10

                    if (hasSettings && mouseX >= arrowX) {
                        mc.gui.setScreen(SettingsScreen(module))
                    } else {
                        module.enabled = !module.enabled
                    }
                    return true
                }

                return super.mouseClicked(context, doubleClick)
            }

            currentY += itemH + 1
        }

        return super.mouseClicked(context, doubleClick)
    }

    override fun mouseReleased(context: MouseButtonEvent): Boolean {
        return super.mouseReleased(context)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val scrollDelta = (verticalAmount * 15).toInt()
        scrollOffset += scrollDelta
        scrollOffset = scrollOffset.coerceAtMost(0)
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val codePoint = event.codepoint()
        if (!Character.isISOControl(codePoint)) {
            searchQuery += codePoint.toChar()
            scrollOffset = 0
            return true
        }
        return super.charTyped(event)
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
            if (filtered.isNotEmpty()) {
                filtered.first().enabled = !filtered.first().enabled
            }
            return true
        }
        return super.keyPressed(input)
    }

    override fun onClose() {
        super.onClose()
    }

}
