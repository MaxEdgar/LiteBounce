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

import net.ccbluex.liquidbounce.config.types.BindValue
import net.ccbluex.liquidbounce.config.types.RangedValue
import net.ccbluex.liquidbounce.config.types.Value
import net.ccbluex.liquidbounce.config.types.ValueType
import net.ccbluex.liquidbounce.config.types.group.ModeValueGroup
import net.ccbluex.liquidbounce.config.types.group.ToggleableValueGroup
import net.ccbluex.liquidbounce.config.types.group.ValueGroup
import net.ccbluex.liquidbounce.config.types.list.ChoiceListValue
import net.ccbluex.liquidbounce.config.types.list.MultiChoiceListValue
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import org.lwjgl.glfw.GLFW

/**
 * Settings editor screen for a single module.
 * Renders all module settings as an interactive scrollable list.
 * Each setting is rendered with its name and an appropriate control.
 */
class SettingsScreen(private val module: ClientModule) : Screen(Component.literal("")) {

    private var scrollOffset = 0
    private var listeningForKey: BindValue? = null

    /** Pre-built list of setting rows for click hit-testing */
    private val settingRows = mutableListOf<SettingRow>()

    /** Height for a single setting row */
    private val rowHeight = 14

    /** Indent per nesting level */
    private val indent = 8

    /** Back button area */
    private val backButtonX = 4
    private val backButtonY = 4
    private val backButtonW: Int get() = mc.font.width("<- Back")
    private val backButtonH = 12

    override fun isPauseScreen() = false

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        // No blur
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sw = width
        val sh = height
        val font = mc.font

        // Dark overlay background
        context.fill(0, 0, sw, sh, 0xCC111111.toInt())

        // --- Back button ---
        val backHover = mouseX in backButtonX..<backButtonX + backButtonW &&
            mouseY in backButtonY..<backButtonY + backButtonH
        context.text(font, "<- Back", backButtonX, backButtonY,
            if (backHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)

        // --- Module title ---
        val titleColor = 0xFF66FF66.toInt()
        context.text(font, "${module.name} Settings",
            sw / 2 - font.width("${module.name} Settings") / 2,
            backButtonY, titleColor, false)

        val startY = backButtonY + backButtonH + 6

        // Clear and rebuild setting rows
        settingRows.clear()

        // Build all setting rows from the module's value tree
        buildRows(module, 0)

        // Apply scroll
        val maxScroll = -(settingRows.size * (rowHeight + 1) - (sh - startY))
            .coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(-maxScroll, 0)

        // Render header separator
        context.fill(sw / 4, startY, 3 * sw / 4, startY + 1, 0xFF444444.toInt())

        // Layout and render each row
        val listStartX = sw / 4 + 4
        val listWidth = sw / 2 - 8
        val controlAreaX = listStartX + listWidth * 3 / 5
        val controlWidth = listWidth * 2 / 5

        var currentY = startY + 4 + scrollOffset

        for (row in settingRows) {
            row.y = currentY

            if (currentY + rowHeight >= startY && currentY < sh) {
                val itemY = currentY
                val itemH = rowHeight
                val hovering = mouseX >= listStartX && mouseY >= itemY &&
                    mouseX < listStartX + listWidth && mouseY < itemY + itemH

                // Background
                val bgColor = if (hovering) 0xFF333333.toInt() else 0xFF1A1A1A.toInt()
                context.fill(listStartX, itemY, listStartX + listWidth, itemY + itemH, bgColor)
                context.guiRenderState.up()

                val rowX = listStartX + row.indent * indent

                // Name label
                val nameColor = if (row.isGroup) 0xFFAAAAAA.toInt() else 0xFFF0F0F0.toInt()
                val nameMaxWidth = controlAreaX - rowX - 4
                val name = if (row.isGroup) "[${row.name}]" else row.name
                context.text(font, name, rowX, itemY + 2, nameColor, false)

                // Render control on the right side
                renderControl(context, row, controlAreaX, itemY, controlWidth, mouseX, mouseY)
            }

            currentY += rowHeight + 1
        }

        // Listening for keybind indicator
        if (listeningForKey != null) {
            context.text(font, "Press a key...", sw / 2 - 40, sh / 2, CommonColors.YELLOW, false)
        }
    }

    /** Recursively builds setting rows from a value container */
    private fun buildRows(container: Iterable<Value<*>>, depth: Int) {
        for (value in container) {
            when (value) {
                is ToggleableValueGroup -> {
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, type = ControlType.TOGGLE_GROUP))
                    buildRows(value.inner, depth + 1)
                }
                is ModeValueGroup<*> -> {
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, type = ControlType.MODE_GROUP))
                    val activeMode = value.activeMode
                    if (activeMode.inner.isNotEmpty()) {
                        buildRows(activeMode.inner, depth + 1)
                    }
                }
                is ValueGroup -> {
                    // Section label
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, type = ControlType.NONE))
                    buildRows(value.inner, depth + 1)
                }
                else -> {
                    val controlType = detectControlType(value)
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = false, type = controlType))
                }
            }
        }
    }

    /** Determines the appropriate control type for a value */
    private fun detectControlType(value: Value<*>): ControlType {
        return when {
            value.valueType == ValueType.BOOLEAN -> ControlType.BOOLEAN
            value.valueType == ValueType.INT -> ControlType.INT_RANGE
            value.valueType == ValueType.FLOAT -> ControlType.FLOAT_RANGE
            value.valueType == ValueType.INT_RANGE -> ControlType.NONE
            value.valueType == ValueType.FLOAT_RANGE -> ControlType.NONE
            value.valueType == ValueType.TEXT -> ControlType.TEXT
            value.valueType == ValueType.COLOR -> ControlType.COLOR
            value.valueType == ValueType.KEY -> ControlType.BIND
            value is ChoiceListValue<*> -> ControlType.ENUM
            value is MultiChoiceListValue<*> -> ControlType.MULTI_ENUM
            value is BindValue -> ControlType.BIND
            else -> ControlType.NONE
        }
    }

    /** Renders the control widget for a single setting row */
    private fun renderControl(
        context: GuiGraphicsExtractor,
        row: SettingRow,
        x: Int,
        y: Int,
        w: Int,
        mouseX: Int,
        mouseY: Int
    ) {
        val font = mc.font
        val value = row.value

        when (row.type) {
            ControlType.BOOLEAN -> renderBoolean(context, value as Value<Boolean>, x, y, w, mouseX, mouseY)
            ControlType.INT_RANGE -> renderIntRange(context, value as RangedValue<Int>, x, y, w, mouseX, mouseY)
            ControlType.FLOAT_RANGE -> renderFloatRange(context, value as RangedValue<Float>, x, y, w, mouseX, mouseY)
            ControlType.ENUM -> renderEnum(context, value as ChoiceListValue<*>, x, y, w, mouseX, mouseY)
            ControlType.MULTI_ENUM -> renderMultiEnum(context, value as MultiChoiceListValue<*>, x, y, w)
            ControlType.TEXT -> renderText(context, value as Value<String>, x, y, w)
            ControlType.COLOR -> renderColor(context, value as Value<Color4b>, x, y, w)
            ControlType.BIND -> renderBind(context, value, x, y, w, mouseX, mouseY)
            ControlType.TOGGLE_GROUP -> renderToggleGroup(context, value as ToggleableValueGroup, x, y, w, mouseX, mouseY)
            ControlType.MODE_GROUP -> renderModeGroup(context, value as ModeValueGroup<*>, x, y, w, mouseX, mouseY)
            ControlType.NONE -> renderNone(context, value, x, y, w)
        }
    }

    // --- Control renderers ---

    private fun renderBoolean(ctx: GuiGraphicsExtractor, value: Value<Boolean>, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val enabled = value.get()
        val btnW = 30
        val btnX = x + w - btnW
        val hover = mx in btnX..<btnX + btnW && my in y..<y + rowHeight
        val bgColor = if (enabled) (if (hover) 0xFF4A8A37.toInt() else 0xFF3A7A27.toInt())
            else (if (hover) 0xFF555555.toInt() else 0xFF444444.toInt())
        ctx.fill(btnX, y, btnX + btnW, y + rowHeight, bgColor)
        val text = if (enabled) "ON" else "OFF"
        val font = mc.font
        ctx.text(font, text, btnX + (btnW - font.width(text)) / 2, y + 2,
            if (enabled) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt(), false)
    }

    private fun renderIntRange(ctx: GuiGraphicsExtractor, value: RangedValue<Int>, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val font = mc.font
        val current = value.get()
        val text = current.toString()
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, text, x + w / 2 - font.width(text) / 2, y + 2, 0xFFF0F0F0.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderFloatRange(ctx: GuiGraphicsExtractor, value: RangedValue<Float>, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val font = mc.font
        val current = value.get()
        val text = String.format("%.1f", current)
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, text, x + w / 2 - font.width(text) / 2, y + 2, 0xFFF0F0F0.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderEnum(ctx: GuiGraphicsExtractor, value: ChoiceListValue<*>, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val font = mc.font
        val current = value.get().toString()
        val text = current
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, text, x + w / 2 - font.width(text) / 2, y + 2, 0xFFFFDD66.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderMultiEnum(ctx: GuiGraphicsExtractor, value: MultiChoiceListValue<*>, x: Int, y: Int, w: Int) {
        val font = mc.font
        val selected = value.get()
        val all = value.values
        val text = "${selected.size}/${all.size} selected"
        ctx.text(font, text, x + w - font.width(text), y + 2, 0xFF888888.toInt(), false)
    }

    private fun renderText(ctx: GuiGraphicsExtractor, value: Value<String>, x: Int, y: Int, w: Int) {
        val font = mc.font
        val text = "\"${value.get()}\""
        val truncated = if (font.width(text) > w) text.take((w / font.width("A")).coerceAtMost(text.length - 3)) + "..." else text
        ctx.text(font, truncated, x + w - font.width(truncated), y + 2, 0xFFCCCCCC.toInt(), false)
    }

    private fun renderColor(ctx: GuiGraphicsExtractor, value: Value<Color4b>, x: Int, y: Int, w: Int) {
        val font = mc.font
        val color = value.get()
        val hex = String.format("#%02X%02X%02X", color.r, color.g, color.b)
        val boxSize = rowHeight - 4
        val boxX = x + w - font.width(hex) - boxSize - 4
        val boxY = y + 2
        val argb = color.r shl 16 or (color.g shl 8) or color.b or (0xFF shl 24)
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, argb)
        ctx.text(font, hex, boxX + boxSize + 3, y + 2, 0xFFCCCCCC.toInt(), false)
    }

    private fun renderBind(ctx: GuiGraphicsExtractor, value: Value<*>, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val font = mc.font
        val isListening = listeningForKey === value
        val text = if (isListening) "Press a key..." else {
            when (value) {
                is BindValue -> value.get().displayName ?: value.get().name
                else -> value.get().toString()
            }
        }
        val btnW = font.width(text) + 8
        val btnX = x + w - btnW
        val hover = mx in btnX..<btnX + btnW && my in y..<y + rowHeight
        val bgColor = if (isListening) 0xFFAA5500.toInt() else if (hover) 0xFF444444.toInt() else 0xFF333333.toInt()
        ctx.fill(btnX, y, btnX + btnW, y + rowHeight, bgColor)
        ctx.text(font, text, btnX + 4, y + 2, if (isListening) CommonColors.YELLOW else 0xFFF0F0F0.toInt(), false)
    }

    private fun renderToggleGroup(ctx: GuiGraphicsExtractor, value: ToggleableValueGroup, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val font = mc.font
        val enabledValue = value.inner.find { it.name == "Enabled" } as? Value<Boolean>
        val enabled = enabledValue?.get() ?: true
        val btnW = 30
        val btnX = x + w - btnW
        val hover = mx in btnX..<btnX + btnW && my in y..<y + rowHeight
        val bgColor = if (enabled) (if (hover) 0xFF4A8A37.toInt() else 0xFF3A7A27.toInt())
            else (if (hover) 0xFF555555.toInt() else 0xFF444444.toInt())
        ctx.fill(btnX, y, btnX + btnW, y + rowHeight, bgColor)
        val text = if (enabled) "ON" else "OFF"
        ctx.text(font, text, btnX + (btnW - font.width(text)) / 2, y + 2,
            if (enabled) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt(), false)
    }

    private fun renderModeGroup(ctx: GuiGraphicsExtractor, value: ModeValueGroup<*>, x: Int, y: Int, w: Int, mx: Int, my: Int) {
        val font = mc.font
        val modeName = value.activeMode.name
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, modeName, x + w / 2 - font.width(modeName) / 2, y + 2, 0xFFFFDD66.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderNone(ctx: GuiGraphicsExtractor, value: Value<*>, x: Int, y: Int, w: Int) {
        val font = mc.font
        val text = value.get().toString()
        ctx.text(font, text, x + w - font.width(text), y + 2, 0xFF888888.toInt(), false)
    }

    // --- Mouse click handling ---

    override fun mouseClicked(context: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = context.x().toInt()
        val mouseY = context.y().toInt()
        val button = context.button()
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(context, doubleClick)

        // Check back button
        if (mouseX in backButtonX..<backButtonX + backButtonW &&
            mouseY in backButtonY..<backButtonY + backButtonH) {
            mc.gui.setScreen(SearchOverlay())
            return true
        }

        // Check setting rows
        val listStartX = width / 4 + 4
        val listWidth = width / 2 - 8

        for (row in settingRows) {
            if (mouseY in row.y..<row.y + rowHeight &&
                mouseX >= listStartX && mouseX < listStartX + listWidth) {
                if (handleControlClick(row, mouseX, mouseY)) {
                    return true
                }
            }
        }

        return super.mouseClicked(context, doubleClick)
    }

    /** Handles clicks on the control area for a given row */
    private fun handleControlClick(row: SettingRow, mouseX: Int, mouseY: Int): Boolean {
        val listStartX = width / 4 + 4
        val listWidth = width / 2 - 8
        val controlAreaX = listStartX + listWidth * 3 / 5
        val controlWidth = listWidth * 2 / 5
        val x = controlAreaX
        val y = row.y
        val w = controlWidth

        return when (row.type) {
            ControlType.BOOLEAN -> {
                val value = row.value as Value<Boolean>
                value.set(!value.get())
                true
            }
            ControlType.INT_RANGE -> {
                val value = row.value as RangedValue<Int>
                val incX = x + w - 8
                val decX = x
                if (mouseX >= decX && mouseX < decX + 8) {
                    value.set((value.get() - 1).coerceAtLeast(value.minValue.toInt()))
                } else if (mouseX >= incX && mouseX < incX + 8) {
                    value.set((value.get() + 1).coerceAtMost(value.maxValue.toInt()))
                }
                true
            }
            ControlType.FLOAT_RANGE -> {
                val value = row.value as RangedValue<Float>
                val incX = x + w - 8
                val decX = x
                val step = 0.5f
                if (mouseX >= decX && mouseX < decX + 8) {
                    value.set((value.get() - step).coerceAtLeast(value.minValue.toFloat()))
                } else if (mouseX >= incX && mouseX < incX + 8) {
                    value.set((value.get() + step).coerceAtMost(value.maxValue.toFloat()))
                }
                true
            }
            ControlType.ENUM -> {
                val value = row.value as ChoiceListValue<*>
                val incX = x + w - 8
                val decX = x
                val options = value.values
                if (options.isNotEmpty()) {
                    val currentIdx = options.indexOf(value.get())
                    if (mouseX >= decX && mouseX < decX + 8) {
                        val newIdx = if (currentIdx <= 0) options.size - 1 else currentIdx - 1
                        @Suppress("UNCHECKED_CAST")
                        value.set(options[newIdx] as Nothing)
                    } else if (mouseX >= incX && mouseX < incX + 8) {
                        val newIdx = if (currentIdx >= options.size - 1) 0 else currentIdx + 1
                        @Suppress("UNCHECKED_CAST")
                        value.set(options[newIdx] as Nothing)
                    }
                }
                true
            }
            ControlType.MULTI_ENUM -> {
                @Suppress("UNCHECKED_CAST")
                val value = row.value as MultiChoiceListValue<Any>
                val selected = value.get().toMutableSet()
                val all = value.values.toList()
                // Find the next unselected value and toggle it
                val currentSelected = selected.toSet()
                val toToggle = all.find { it !in currentSelected } ?: all.firstOrNull()
                if (toToggle != null) {
                    if (toToggle in selected) {
                        if (selected.size > 1) selected.remove(toToggle)
                    } else {
                        selected.add(toToggle)
                    }
                    value.set(selected)
                }
                true
            }
            ControlType.TOGGLE_GROUP -> {
                val value = row.value as ToggleableValueGroup
                val enabledValue = value.inner.find { it.name == "Enabled" } as? Value<Boolean>
                enabledValue?.set(!enabledValue.get())
                true
            }
            ControlType.MODE_GROUP -> {
                val value = row.value as ModeValueGroup<*>
                val incX = x + w - 8
                val decX = x
                val modes = value.inner
                if (modes.isNotEmpty()) {
                    val currentIdx = modes.indexOf(value.activeMode)
                    if (mouseX >= decX && mouseX < decX + 8) {
                        val newIdx = if (currentIdx <= 0) modes.size - 1 else currentIdx - 1
                        @Suppress("UNCHECKED_CAST")
                        value.activeMode = modes[newIdx] as ValueGroup
                    } else if (mouseX >= incX && mouseX < incX + 8) {
                        val newIdx = if (currentIdx >= modes.size - 1) 0 else currentIdx + 1
                        @Suppress("UNCHECKED_CAST")
                        value.activeMode = modes[newIdx] as ValueGroup
                    }
                }
                true
            }
            ControlType.BIND -> {
                // Start/stop listening for a keybind
                if (listeningForKey === row.value) {
                    listeningForKey = null
                } else {
                    listeningForKey = row.value as? BindValue
                }
                true
            }
            else -> false
        }
    }

    // --- Keyboard handling ---

    override fun keyPressed(input: KeyEvent): Boolean {
        if (input.key == GLFW.GLFW_KEY_ESCAPE) {
            if (listeningForKey != null) {
                // Cancel keybind listening
                listeningForKey = null
                return true
            }
            // Go back to search overlay
            mc.gui.setScreen(SearchOverlay())
            return true
        }

        // If listening for a keybind, capture the key
        if (listeningForKey != null && input.key != GLFW.GLFW_KEY_ESCAPE) {
            @Suppress("UNCHECKED_CAST")
            (listeningForKey!! as Value<Int>).set(input.key)
            listeningForKey = null
            return true
        }

        return super.keyPressed(input)
    }

    /** Data class for a single setting row */
    private data class SettingRow(
        val name: String,
        val value: Value<*>,
        val indent: Int,
        val isGroup: Boolean,
        val type: ControlType,
        var y: Int = 0
    )

    /** Enum of supported control types */
    private enum class ControlType {
        BOOLEAN,
        INT_RANGE,
        FLOAT_RANGE,
        ENUM,
        MULTI_ENUM,
        TEXT,
        COLOR,
        BIND,
        TOGGLE_GROUP,
        MODE_GROUP,
        NONE
    }

}
