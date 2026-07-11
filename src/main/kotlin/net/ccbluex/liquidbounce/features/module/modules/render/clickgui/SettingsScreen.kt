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

import com.mojang.blaze3d.platform.InputConstants
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
import net.ccbluex.liquidbounce.utils.input.InputBind
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
@Suppress("CognitiveComplexMethod", "LongMethod", "TooManyFunctions")
class SettingsScreen(private val module: ClientModule) : Screen(Component.literal("")) {

    private var scrollOffset = 0
    private var listeningForKey: BindValue? = null

    private val settingRows = mutableListOf<SettingRow>()

    private val rowHeight = 14
    private val indent = 8

    private val backButtonX = 4
    private val backButtonY = 4
    private val backButtonW: Int get() = mc.font.width("<- Back")
    private val backButtonH = 12

    override fun isPauseScreen() = false

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        // No blur
    }

    @Suppress("CognitiveComplexMethod", "LongMethod")
    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sw = width
        val sh = height
        val font = mc.font

        context.fill(0, 0, sw, sh, 0xCC111111.toInt())

        // Back button
        val backHover = mouseX in backButtonX..<backButtonX + backButtonW &&
            mouseY in backButtonY..<backButtonY + backButtonH
        context.text(font, "<- Back", backButtonX, backButtonY,
            if (backHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)

        // Module title
        val titleText = "${module.name} Settings"
        context.text(font, titleText, sw / 2 - font.width(titleText) / 2,
            backButtonY, 0xFF66FF66.toInt(), false)

        // Module description (truncated if too long)
        val descText = module.description.get()
        val showDesc = !descText.isNullOrBlank()
        if (showDesc) {
            val descY = backButtonY + backButtonH + 2
            val maxDescWidth = sw - 40
            val displayDesc = if (font.width(descText) > maxDescWidth) {
                font.plainSubstrByWidth(descText, maxDescWidth) + "..."
            } else {
                descText
            }
            context.text(font, displayDesc, sw / 2 - font.width(displayDesc) / 2,
                descY, 0xFF888888.toInt(), false)
        }

        val startY = backButtonY + backButtonH + 2 + if (showDesc) font.lineHeight + 2 else 4

        // Build setting rows
        settingRows.clear()
        buildRows(module.inner, 0)

        // Clamp scroll
        val maxScroll = (settingRows.size * (rowHeight + 1) - (sh - startY)).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(-maxScroll, 0)

        // Header separator
        context.fill(sw / 4, startY, 3 * sw / 4, startY + 1, 0xFF444444.toInt())

        val listStartX = sw / 4 + 4
        val listWidth = sw / 2 - 8
        val controlAreaX = listStartX + listWidth * 3 / 5
        val controlWidth = listWidth * 2 / 5

        var currentY = startY + 4 + scrollOffset

        for (row in settingRows) {
            row.y = currentY

            if (currentY + rowHeight >= startY && currentY < sh) {
                val itemY = currentY
                val hovering = mouseX >= listStartX && mouseY >= itemY &&
                    mouseX < listStartX + listWidth && mouseY < itemY + rowHeight

                val bgColor = if (hovering) 0xFF333333.toInt() else 0xFF1A1A1A.toInt()
                context.fill(listStartX, itemY, listStartX + listWidth, itemY + rowHeight, bgColor)
                context.guiRenderState.up()

                val rowX = listStartX + row.indent * indent

                // Name label
                val nameColor = if (row.isGroup) 0xFFAAAAAA.toInt() else 0xFFF0F0F0.toInt()
                val namePrefix = if (row.isGroup) "[" else ""
                val nameSuffix = if (row.isGroup) "]" else ""
                val displayName = "$namePrefix${row.name}$nameSuffix"
                context.text(font, displayName, rowX, itemY + 2, nameColor, false)

                renderControl(context, row, controlAreaX, itemY, controlWidth, mouseX, mouseY)
            }

            currentY += rowHeight + 1
        }

        // Keybind listening indicator
        if (listeningForKey != null) {
            context.text(font, "Press a key...", sw / 2 - 40, sh / 2, CommonColors.YELLOW, false)
        }
    }

    private fun buildRows(container: Iterable<Value<*>>, depth: Int) {
        for (value in container) {
            when (value) {
                is ToggleableValueGroup -> {
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, type = ControlType.TOGGLE_GROUP))
                    buildRows(value.inner, depth + 1)
                }
                is ModeValueGroup<*> -> {
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, type = ControlType.MODE_GROUP))
                    if (value.activeMode.inner.isNotEmpty()) {
                        buildRows(value.activeMode.inner, depth + 1)
                    }
                }
                is ValueGroup -> {
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

    // --- Control renderers ---

    private fun renderControl(
        context: GuiGraphicsExtractor, row: SettingRow,
        x: Int, y: Int, w: Int, mouseX: Int, mouseY: Int
    ) {
        when (row.type) {
            ControlType.BOOLEAN -> renderBoolean(context, row.value as Value<Boolean>, x, y, w, mouseX, mouseY)
            ControlType.INT_RANGE -> renderIntRange(context, row.value as RangedValue<Int>, x, y, w, mouseX, mouseY)
            ControlType.FLOAT_RANGE -> renderFloatRange(context, row.value as RangedValue<Float>, x, y, w, mouseX, mouseY)
            ControlType.ENUM -> renderEnum(context, row.value as ChoiceListValue<*>, x, y, w, mouseX, mouseY)
            ControlType.MULTI_ENUM -> renderMultiEnum(context, row.value as MultiChoiceListValue<*>, x, y, w)
            ControlType.TEXT -> renderText(context, row.value as Value<String>, x, y, w)
            ControlType.COLOR -> renderColor(context, row.value as Value<Color4b>, x, y, w)
            ControlType.BIND -> renderBind(context, row.value, x, y, w, mouseX, mouseY)
            ControlType.TOGGLE_GROUP -> renderToggleGroup(context, row.value as ToggleableValueGroup, x, y, w, mouseX, mouseY)
            ControlType.MODE_GROUP -> renderModeGroup(context, row.value as ModeValueGroup<*>, x, y, w, mouseX, mouseY)
            ControlType.NONE -> renderNone(context, row.value, x, y, w)
        }
    }

    private fun renderBoolean(
        ctx: GuiGraphicsExtractor, value: Value<Boolean>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val enabled = value.get()
        val btnW = 30
        val btnX = x + w - btnW
        val hover = mx in btnX..<btnX + btnW && my in y..<y + rowHeight
        val bgColor = when {
            enabled && hover -> 0xFF4A8A37.toInt()
            enabled -> 0xFF3A7A27.toInt()
            hover -> 0xFF555555.toInt()
            else -> 0xFF444444.toInt()
        }
        ctx.fill(btnX, y, btnX + btnW, y + rowHeight, bgColor)
        val text = if (enabled) "ON" else "OFF"
        val font = mc.font
        ctx.text(font, text, btnX + (btnW - font.width(text)) / 2, y + 2,
            if (enabled) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt(), false)
    }

    private fun renderIntRange(
        ctx: GuiGraphicsExtractor, value: RangedValue<Int>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val text = value.get().toString()
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, text, x + w / 2 - font.width(text) / 2, y + 2, 0xFFF0F0F0.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderFloatRange(
        ctx: GuiGraphicsExtractor, value: RangedValue<Float>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val text = String.format("%.1f", value.get())
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, text, x + w / 2 - font.width(text) / 2, y + 2, 0xFFF0F0F0.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderEnum(
        ctx: GuiGraphicsExtractor, value: ChoiceListValue<*>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val text = value.get().toString()
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, text, x + w / 2 - font.width(text) / 2, y + 2, 0xFFFFDD66.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderMultiEnum(
        ctx: GuiGraphicsExtractor, value: MultiChoiceListValue<*>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val selected = value.get()
        val all = value.choices
        val text = "${selected.size}/${all.size} selected"
        ctx.text(font, text, x + w - font.width(text), y + 2, 0xFF888888.toInt(), false)
    }

    private fun renderText(
        ctx: GuiGraphicsExtractor, value: Value<String>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val fullText = "\"${value.get()}\""
        val truncated = if (font.width(fullText) > w) {
            val approxChars = w / font.width("A")
            fullText.take(approxChars.coerceAtLeast(3)) + "..."
        } else {
            fullText
        }
        ctx.text(font, truncated, x + w - font.width(truncated), y + 2, 0xFFCCCCCC.toInt(), false)
    }

    private fun renderColor(
        ctx: GuiGraphicsExtractor, value: Value<Color4b>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val color = value.get()
        val hex = String.format("#%02X%02X%02X", color.r, color.g, color.b)
        val boxSize = rowHeight - 4
        val boxX = x + w - font.width(hex) - boxSize - 4
        val boxY = y + 2
        val argb = (color.a.toInt() shl 24) or (color.r.toInt() shl 16) or (color.g.toInt() shl 8) or color.b.toInt()
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, argb)
        ctx.text(font, hex, boxX + boxSize + 3, y + 2, 0xFFCCCCCC.toInt(), false)
    }

    private fun renderBind(
        ctx: GuiGraphicsExtractor, value: Value<*>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val isListening = listeningForKey === value
        val text = when {
            isListening -> "Press a key..."
            value is BindValue -> value.get().keyName
            else -> value.get().toString()
        }
        val btnW = font.width(text) + 8
        val btnX = x + w - btnW
        val hover = mx in btnX..<btnX + btnW && my in y..<y + rowHeight
        val bgColor = when {
            isListening -> 0xFFAA5500.toInt()
            hover -> 0xFF444444.toInt()
            else -> 0xFF333333.toInt()
        }
        ctx.fill(btnX, y, btnX + btnW, y + rowHeight, bgColor)
        ctx.text(font, text, btnX + 4, y + 2,
            if (isListening) CommonColors.YELLOW else 0xFFF0F0F0.toInt(), false)
    }

    private fun renderToggleGroup(
        ctx: GuiGraphicsExtractor, value: ToggleableValueGroup,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val enabledValue = value.inner.find { it.name == "Enabled" } as? Value<Boolean>
        val enabled = enabledValue?.get() ?: true
        val btnW = 30
        val btnX = x + w - btnW
        val hover = mx in btnX..<btnX + btnW && my in y..<y + rowHeight
        val bgColor = when {
            enabled && hover -> 0xFF4A8A37.toInt()
            enabled -> 0xFF3A7A27.toInt()
            hover -> 0xFF555555.toInt()
            else -> 0xFF444444.toInt()
        }
        ctx.fill(btnX, y, btnX + btnW, y + rowHeight, bgColor)
        val text = if (enabled) "ON" else "OFF"
        ctx.text(font, text, btnX + (btnW - font.width(text)) / 2, y + 2,
            if (enabled) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt(), false)
    }

    private fun renderModeGroup(
        ctx: GuiGraphicsExtractor, value: ModeValueGroup<*>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val modeName = value.activeMode.tag
        val decX = x
        val incX = x + w - 8
        val decHover = mx in decX..<decX + 8 && my in y..<y + rowHeight
        val incHover = mx in incX..<incX + 8 && my in y..<y + rowHeight
        ctx.text(font, "<", decX, y + 2, if (decHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
        ctx.text(font, modeName, x + w / 2 - font.width(modeName) / 2, y + 2, 0xFFFFDD66.toInt(), false)
        ctx.text(font, ">", incX, y + 2, if (incHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)
    }

    private fun renderNone(
        ctx: GuiGraphicsExtractor, value: Value<*>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val text = value.get().toString()
        ctx.text(font, text, x + w - font.width(text), y + 2, 0xFF888888.toInt(), false)
    }

    // --- Mouse click handling ---

    override fun mouseClicked(context: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = context.x().toInt()
        val mouseY = context.y().toInt()
        val button = context.button()
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(context, doubleClick)
        }

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

    @Suppress("CognitiveComplexMethod", "LongMethod")
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
                val range = value.range
                val min = (range as ClosedRange<Int>).start
                val max = range.endInclusive
                val incX = x + w - 8
                val decX = x
                if (mouseX >= decX && mouseX < decX + 8) {
                    value.set((value.get() - 1).coerceAtLeast(min))
                } else if (mouseX >= incX && mouseX < incX + 8) {
                    value.set((value.get() + 1).coerceAtMost(max))
                }
                true
            }
            ControlType.FLOAT_RANGE -> {
                val value = row.value as RangedValue<Float>
                val range = value.range
                val min = (range as ClosedRange<Float>).start
                val max = range.endInclusive
                val incX = x + w - 8
                val decX = x
                val step = 0.5f
                if (mouseX >= decX && mouseX < decX + 8) {
                    value.set((value.get() - step).coerceAtLeast(min))
                } else if (mouseX >= incX && mouseX < incX + 8) {
                    value.set((value.get() + step).coerceAtMost(max))
                }
                true
            }
            ControlType.ENUM -> {
                val value = row.value as ChoiceListValue<*>
                val incX = x + w - 8
                val decX = x
                val options = value.choices.toList()
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
                val all = value.choices.toList()
                val selected = value.get()
                // Find next unselected value and toggle it
                val toToggle = all.find { it !in selected } ?: all.firstOrNull()
                if (toToggle != null) {
                    value.toggle(toToggle)
                }
                true
            }
            ControlType.TOGGLE_GROUP -> {
                val value = row.value as ToggleableValueGroup
                val enabledValue = value.inner.find { it.name == "Enabled" } as? Value<Boolean>
                enabledValue?.let { it.set(!it.get()) }
                true
            }
            ControlType.MODE_GROUP -> {
                val value = row.value as ModeValueGroup<*>
                val incX = x + w - 8
                val decX = x
                val modeNames = value.modes.map { it.tag }
                if (modeNames.isNotEmpty()) {
                    val currentIdx = modeNames.indexOf(value.activeMode.tag)
                    if (mouseX >= decX && mouseX < decX + 8) {
                        val newIdx = if (currentIdx <= 0) modeNames.size - 1 else currentIdx - 1
                        value.setByString(modeNames[newIdx])
                    } else if (mouseX >= incX && mouseX < incX + 8) {
                        val newIdx = if (currentIdx >= modeNames.size - 1) 0 else currentIdx + 1
                        value.setByString(modeNames[newIdx])
                    }
                }
                true
            }
            ControlType.BIND -> {
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
                listeningForKey = null
                return true
            }
            mc.gui.setScreen(SearchOverlay())
            return true
        }

        // Capture keybind
        if (listeningForKey != null && input.key != GLFW.GLFW_KEY_ESCAPE) {
            val newBind = InputBind(InputConstants.Type.KEYSYM, input.key, InputBind.BindAction.TOGGLE)
            listeningForKey!!.set(newBind)
            listeningForKey = null
            return true
        }

        return super.keyPressed(input)
    }

    private data class SettingRow(
        val name: String,
        val value: Value<*>,
        val indent: Int,
        val isGroup: Boolean,
        val type: ControlType,
        var y: Int = 0
    )

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
