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
import net.ccbluex.liquidbounce.features.module.ModuleCategories
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
 * Search-based module selection screen with expandable category tree.
 * Click > on a category to expand/collapse. Click a module to toggle it.
 * Right-click or left-click > on a module to open its settings.
 */
class SearchOverlay : Screen(Component.literal("")) {

    private var searchQuery = ""
    private var scrollOffset = 0

    private val searchBarHeight = 20
    private val itemHeight = 14
    private val categoryHeaderH = 16

    /** Persistent expanded categories (maps category name -> expanded) */
    private val expandedCategories = hashMapOf<String, Boolean>()

    /** Whether we're in flat-search mode (when user types) */
    private val isSearching get() = searchQuery.isNotEmpty()

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
            context.text(font, "Search or click > to expand categories...", x + 4, textY, 0xFF666666.toInt(), false)
        }
    }

    /** Get visible items: either categories+modules (browse) or flat list (search) */
    private fun getVisibleItems(): List<VisibleItem> {
        if (isSearching) {
            // Flat list of matching modules
            return ModuleManager.sortedBy { it.name }
                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                .map { VisibleItem.ModuleItem(it) }
        }
        // Group by category, only show expanded ones
        val items = mutableListOf<VisibleItem>()
        val categoriesWithModules = ModuleManager.groupBy { it.category }
            .toSortedMap(compareBy { it.displayName })

        for ((category, modules) in categoriesWithModules) {
            val sorted = modules.sortedBy { it.name }
            val expanded = expandedCategories.getOrPut(category.name) { false }
            items.add(VisibleItem.CategoryHeader(category, expanded))

            if (expanded) {
                for (module in sorted) {
                    items.add(VisibleItem.ModuleItem(module))
                }
            }
        }
        return items
    }

    private fun renderModuleList(context: GuiGraphicsExtractor, x: Int, startY: Int, w: Int, mouseX: Int, mouseY: Int) {
        val font = mc.font
        val items = getVisibleItems()

        var currentY = startY + scrollOffset

        for (item in items) {
            val itemH = when (item) {
                is VisibleItem.CategoryHeader -> categoryHeaderH
                is VisibleItem.ModuleItem -> itemHeight
            }

            if (currentY + itemH >= 0 && currentY < height) {
                when (item) {
                    is VisibleItem.CategoryHeader -> renderCategoryHeader(context, item, x, currentY, w, mouseX, mouseY)
                    is VisibleItem.ModuleItem -> renderModuleItem(context, item, x, currentY, w, mouseX, mouseY)
                }
            }

            currentY += itemH + 1
        }
    }

    private fun renderCategoryHeader(
        ctx: GuiGraphicsExtractor, item: VisibleItem.CategoryHeader,
        x: Int, y: Int, w: Int, mouseX: Int, mouseY: Int
    ) {
        val font = mc.font
        val hovering = mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + categoryHeaderH
        val arrowX = x + w - 14
        val arrowHover = hovering && mouseX >= arrowX

        val bgColor = if (hovering) 0xFF222244.toInt() else 0xFF1A1A2E.toInt()
        ctx.fill(x, y, x + w, y + categoryHeaderH, bgColor)
        ctx.fill(x, y + categoryHeaderH, x + w, y + categoryHeaderH + 1, 0xFF2A2A55.toInt())

        // Category icon (small colored square)
        ctx.fill(x + 3, y + 3, x + 3 + 8, y + 3 + 8,
            ModuleCategories.entries.indexOf(item.category).let { 0xFF222244 + (it * 0x222222) and 0xFFFFFF or 0xFF000000.toInt() })

        // Category name
        ctx.text(font, item.category.displayName, x + 15, y + 3,
            if (hovering) 0xFFAABBFF.toInt() else 0xFF8888BB.toInt(), false)

        // Module count
        val count = ModuleManager.modules.count { it.category == item.category }
        val countText = "$count modules"
        ctx.text(font, countText, x + 15, y + 4 + font.lineHeight,
            0xFF555577.toInt(), false)

        // Expand/collapse arrow
        val arrowText = if (item.expanded) "v" else ">"
        val arrowColor = if (arrowHover) 0xFFFFAA00.toInt() else 0xFF666688.toInt()
        ctx.text(font, arrowText, arrowX, y + 2, arrowColor, false)
    }

    private fun renderModuleItem(
        ctx: GuiGraphicsExtractor, item: VisibleItem.ModuleItem,
        x: Int, y: Int, w: Int, mouseX: Int, mouseY: Int
    ) {
        val font = mc.font
        val module = item.module
        val hovering = mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + itemHeight
        val hasSettings = module.inner.isNotEmpty()
        val arrowX = x + w - 12
        val arrowHover = hovering && mouseX >= arrowX

        // Module item background
        val bgColor = when {
            module.enabled && arrowHover && hasSettings -> 0xFF4A7A37.toInt()
            module.enabled && hovering -> 0xFF3A6A27.toInt()
            module.enabled -> 0xFF2D5A27.toInt()
            hovering -> 0xFF333333.toInt()
            else -> 0xFF1A1A1A.toInt()
        }
        ctx.fill(x, y, x + w, y + itemHeight, bgColor)

        // Module name text color
        val nameColor = if (module.enabled) 0xFF66FF66.toInt() else 0xFFF0F0F0.toInt()
        ctx.text(font, module.name, x + 8, y + 2, nameColor, false)

        // Settings arrow (>) if module has settings
        if (hasSettings) {
            val arrowColor = if (arrowHover) 0xFFFFAA00.toInt() else 0xFF555555.toInt()
            ctx.text(font, ">", arrowX, y + 2, arrowColor, false)
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

        val items = getVisibleItems()
        var currentY = listStartY + scrollOffset

        for (item in items) {
            val itemH = when (item) {
                is VisibleItem.CategoryHeader -> categoryHeaderH
                is VisibleItem.ModuleItem -> itemHeight
            }

            if (mouseY >= currentY && mouseY < currentY + itemH &&
                mouseX >= searchBarX && mouseX < searchBarX + searchBarW) {
                when (item) {
                    is VisibleItem.CategoryHeader -> {
                        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                            // Toggle expansion: click anywhere on the header
                            expandedCategories[item.category.name] = !item.expanded
                            return true
                        }
                    }
                    is VisibleItem.ModuleItem -> {
                        val module = item.module
                        val hasSettings = module.inner.isNotEmpty()
                        val arrowX = searchBarX + searchBarW - 12

                        // Left click on > arrow or right click anywhere -> open settings
                        if ((button == GLFW.GLFW_MOUSE_BUTTON_LEFT && hasSettings && mouseX >= arrowX) ||
                            (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && hasSettings)) {
                            mc.gui.setScreen(SettingsScreen(module))
                            return true
                        }

                        // Left click on module body -> toggle
                        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                            module.enabled = !module.enabled
                            return true
                        }
                    }
                }
            }

            currentY += itemH + 1
        }

        return super.mouseClicked(context, doubleClick)
    }

    override fun mouseReleased(context: MouseButtonEvent): Boolean {
        return super.mouseReleased(context)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val scrollDelta = (verticalAmount * 20).toInt()
        scrollOffset += scrollDelta
        scrollOffset = scrollOffset.coerceAtMost(0)
        return true
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
            if (isSearching) {
                val filtered = ModuleManager.sortedBy { it.name }
                    .filter { it.name.contains(searchQuery, ignoreCase = true) }
                if (filtered.isNotEmpty()) {
                    filtered.first().enabled = !filtered.first().enabled
                }
            }
            return true
        }
        return super.keyPressed(input)
    }

    override fun onClose() {
        super.onClose()
    }

    /** Represents an item visible in the module list */
    private sealed class VisibleItem {
        data class CategoryHeader(val category: ModuleCategories, val expanded: Boolean) : VisibleItem()
        data class ModuleItem(val module: ClientModule) : VisibleItem()
    }

}
