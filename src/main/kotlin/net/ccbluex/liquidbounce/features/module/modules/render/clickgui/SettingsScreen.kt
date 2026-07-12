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
import org.lwjgl.glfw.GLFW

/**
 * Settings editor screen for a single module.
 * Renders all module settings as an interactive scrollable list.
 */
@Suppress("CognitiveComplexMethod", "LongMethod", "TooManyFunctions")
class SettingsScreen(private val module: ClientModule) : Screen(Component.literal("")) {

    private var scrollOffset = 0
    private var scrollBarGrabbed = false
    private var scrollBarGrabY = 0
    private var scrollBarGrabOffset = 0
    private var listeningForKey: BindValue? = null

    private val settingRows = mutableListOf<SettingRow>()

    private val rowHeight = 16
    private val indent = 10
    private val headerH = 14

    private val backButtonX = 6
    private val backButtonY = 6
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

        // Background
        context.fill(0, 0, sw, sh, 0xCC0D0D1A.toInt())

        // Top bar
        context.fill(0, 0, sw, backButtonH + 10, 0xCC151528.toInt())
        context.fill(0, backButtonH + 10, sw, backButtonH + 11, 0xFF333355.toInt())

        // Back button
        val backHover = mouseX in backButtonX..<backButtonX + backButtonW + 4 &&
            mouseY in backButtonY..<backButtonY + backButtonH
        if (backHover) {
            context.fill(backButtonX - 2, backButtonY, backButtonX + backButtonW + 2, backButtonY + backButtonH,
                0xFF222244.toInt())
        }
        context.text(font, "<- Back", backButtonX, backButtonY + 1,
            if (backHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)

        // Module title
        val titleText = "${module.name} Settings"
        context.text(font, titleText, sw / 2 - font.width(titleText) / 2,
            backButtonY + 1, 0xFF66FF66.toInt(), false)

        // Module description (truncated if too long)
        val descText = module.description.get()
        val showDesc = !descText.isNullOrBlank()
        if (showDesc) {
            val descY = backButtonH + 13
            val maxDescWidth = sw - 40
            val displayDesc = if (font.width(descText) > maxDescWidth) {
                font.plainSubstrByWidth(descText, maxDescWidth - font.width("...")) + "..."
            } else {
                descText
            }
            context.text(font, displayDesc, sw / 2 - font.width(displayDesc) / 2,
                descY, 0xFF777788.toInt(), false)
        }

        val topBarH = backButtonH + 11 + if (showDesc) font.lineHeight + 3 else 8

        // Build setting rows
        settingRows.clear()
        buildRows(module.inner, 0, isTopLevel = true)

        // Scroll calculations
        val listStartY = topBarH
        val listEndY = sh - 4
        val listH = listEndY - listStartY
        val contentH = settingRows.size * (rowHeight + 1)
        val maxScroll = (contentH - listH).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(-maxScroll, 0)

        val listX = sw / 4
        val listW = sw / 2
        val controlAreaX = listX + listW * 3 / 5 + 4
        val controlWidth = (listW * 2 / 5 - 8).coerceAtLeast(40)

        // Render rows
        var currentY = listStartY + scrollOffset

        for ((index, row) in settingRows.withIndex()) {
            row.y = currentY
            val itemH = if (row.isGroup && row.isTopLevel) headerH else rowHeight

            if (currentY + itemH >= listStartY - itemH && currentY < listEndY) {
                val itemY = currentY
                val hovering = mouseX >= listX && mouseY >= itemY &&
                    mouseX < listX + listW && mouseY < itemY + itemH

                if (row.isGroup && row.isTopLevel) {
                    // Group header styling
                    val headerBg = if (hovering) 0xFF1A1A35.toInt() else 0xFF151528.toInt()
                    context.fill(listX, itemY, listX + listW, itemY + headerH, headerBg)
                    context.fill(listX, itemY + headerH, listX + listW, itemY + headerH + 1, 0xFF2A2A55.toInt())
                    context.text(font, row.name, listX + 6, itemY + 2, 0xFF9999CC.toInt(), false)

                } else {
                    // Regular setting row
                    val bgColor = when {
                        hovering -> 0xFF1E1E35.toInt()
                        index % 2 == 0 -> 0xFF131325.toInt()
                        else -> 0xFF111120.toInt()
                    }
                    context.fill(listX, itemY, listX + listW, itemY + itemH, bgColor)

                    val rowX = listX + 6 + row.indent * indent

                    // Name label
                    val namePrefix = if (row.isGroup) "> " else ""
                    val displayName = "$namePrefix${row.name}"
                    val nameColor = when {
                        row.isGroup -> 0xFF88AAFF.toInt()
                        else -> 0xFFDDDDEE.toInt()
                    }
                    context.text(font, displayName, rowX, itemY + 3, nameColor, false)

                    // Separator line between name and control
                    val sepX = rowX + font.width(displayName) + 4
                    if (sepX + 4 < controlAreaX) {
                        context.fill(sepX, itemY + itemH / 2, controlAreaX - 2, itemY + itemH / 2 + 1, 0xFF1A1A33.toInt())
                    }

                    // Render control
                    renderControl(context, row, controlAreaX, itemY, controlWidth, mouseX, mouseY)
                }
            }

            currentY += itemH + 1
        }

        // Scrollbar
        if (maxScroll > 0) {
            val scrollBarX = listX + listW + 2
            val scrollBarW = 4
            val thumbH = (listH.toFloat() / contentH.toFloat() * scrollBarH).toInt().coerceAtLeast(16)
            val thumbY = listStartY + ((-scrollOffset).toFloat() / maxScroll * (scrollBarH - thumbH)).toInt()

            // Track
            context.fill(scrollBarX, listStartY, scrollBarX + scrollBarW, listStartY + scrollBarH, 0xFF1A1A33.toInt())
            // Thumb
            val thumbHover = mouseX in scrollBarX..<scrollBarX + scrollBarW && mouseY in thumbY..<thumbY + thumbH
            context.fill(scrollBarX, thumbY, scrollBarX + scrollBarW, thumbY + thumbH,
                if (thumbHover) 0xFF5555AA.toInt() else 0xFF333377.toInt())
        }
    }

    private val scrollBarH: Int get() = (height - 4) - (backButtonH + 11 + (if (module.description.get().isNullOrBlank()) 8 else mc.font.lineHeight + 3))

    private fun buildRows(container: Iterable<Value<*>>, depth: Int, isTopLevel: Boolean = false) {
        for (value in container) {
            when (value) {
                is ToggleableValueGroup -> {
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, isTopLevel = isTopLevel || depth == 0, type = ControlType.TOGGLE_GROUP))
                    buildRows(value.inner, depth + 1)
                }
                is ModeValueGroup<*> -> {
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, isTopLevel = isTopLevel || depth == 0, type = ControlType.MODE_GROUP))
                    if (value.activeMode.inner.isNotEmpty()) {
                        buildRows(value.activeMode.inner, depth + 1)
                    }
                }
                is ValueGroup -> {
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = true, isTopLevel = isTopLevel || depth == 0, type = ControlType.NONE))
                    buildRows(value.inner, depth + 1)
                }
                else -> {
                    val controlType = detectControlType(value)
                    settingRows.add(SettingRow(value.name, value, depth, isGroup = false, isTopLevel = false, type = controlType))
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

    // Control renderers

    private fun renderControl(
        context: GuiGraphicsExtractor, row: SettingRow,
        x: Int, y: Int, w: Int, mouseX: Int, mouseY: Int
    ) {
        when (row.type) {
            ControlType.BOOLEAN -> renderToggle(context, row.value as Value<Boolean>, x, y, w, mouseX, mouseY)
            ControlType.INT_RANGE -> renderStepper(context, row.value as RangedValue<Int>, x, y, w, mouseX, mouseY) { it.toString() }
            ControlType.FLOAT_RANGE -> renderStepper(context, row.value as RangedValue<Float>, x, y, w, mouseX, mouseY) { String.format("%.1f", (it as RangedValue<Float>).get()) }
            ControlType.ENUM -> renderStepper(context, row.value as ChoiceListValue<*>, x, y, w, mouseX, mouseY) { it.toString() }
            ControlType.MULTI_ENUM -> renderMultiEnum(context, row.value as MultiChoiceListValue<*>, x, y, w)
            ControlType.TEXT -> renderText(context, row.value as Value<String>, x, y, w)
            ControlType.COLOR -> renderColor(context, row.value as Value<Color4b>, x, y, w)
            ControlType.BIND -> renderBind(context, row.value, x, y, w, mouseX, mouseY)
            ControlType.TOGGLE_GROUP -> renderToggle(context, findEnabledValue(row.value as ToggleableValueGroup), x, y, w, mouseX, mouseY)
            ControlType.MODE_GROUP -> renderStepper(context, row.value as ModeValueGroup<*>, x, y, w, mouseX, mouseY) { (it as ModeValueGroup<*>).activeMode.tag }
            ControlType.NONE -> renderNone(context, row.value, x, y, w)
        }
    }

    private fun findEnabledValue(group: ToggleableValueGroup): Value<Boolean>? {
        return group.inner.find { it.name == "Enabled" } as? Value<Boolean>
    }

    /** Pill-style toggle ON/OFF button */
    private fun renderToggle(
        ctx: GuiGraphicsExtractor, value: Value<Boolean>?,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        if (value == null) return
        val enabled = value.get()
        val btnW = 36
        val btnX = x + w - btnW
        val btnH = rowHeight - 4
        val btnY = y + 2
        val hover = mx in btnX..<btnX + btnW && my in btnY..<btnY + btnH

        ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH,
            if (enabled) 0xFF2D5A27.toInt() else 0xFF333344.toInt())

        val nubW = btnW / 2
        val nubX = if (enabled) btnX + btnW - nubW else btnX
        ctx.fill(nubX, btnY, nubX + nubW, btnY + btnH,
            if (hover) 0xFF66CC66.toInt() else if (enabled) 0xFF44AA44.toInt() else 0xFF666677.toInt())

        val font = mc.font
        val text = if (enabled) "ON" else "OFF"
        ctx.text(font, text, btnX + (btnW - font.width(text)) / 2, btnY + 2,
            if (enabled) 0xFFFFFFFF.toInt() else 0xFF999999.toInt(), false)
    }

    /** Left/right stepper control for ranged values, enums, and mode groups */
    private fun renderStepper(
        ctx: GuiGraphicsExtractor, value: Any,
        x: Int, y: Int, w: Int, mx: Int, my: Int,
        displayFn: (Any) -> String
    ) {
        val font = mc.font
        val text = displayFn(value)
        val btnSize = 10
        val btnY = y + 3
        val decX = x
        val incX = x + w - btnSize
        val decHover = mx in decX..<decX + btnSize && my in btnY..<btnY + btnSize
        val incHover = mx in incX..<incX + btnSize && my in btnY..<btnY + btnSize

        // Left arrow
        ctx.fill(decX, btnY, decX + btnSize, btnY + btnSize,
            if (decHover) 0xFF444466.toInt() else 0xFF222233.toInt())
        ctx.text(font, "<", decX + 2, btnY, if (decHover) 0xFFFFAA00.toInt() else 0xFF666677.toInt(), false)

        // Value text
        ctx.text(font, text, x + w / 2 - font.width(text) / 2, y + 3, 0xFFDDDDEE.toInt(), false)

        // Right arrow
        ctx.fill(incX, btnY, incX + btnSize, btnY + btnSize,
            if (incHover) 0xFF444466.toInt() else 0xFF222233.toInt())
        ctx.text(font, ">", incX + 2, btnY, if (incHover) 0xFFFFAA00.toInt() else 0xFF666677.toInt(), false)
    }

    private fun renderMultiEnum(
        ctx: GuiGraphicsExtractor, value: MultiChoiceListValue<*>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val selected = value.get()
        val all = value.choices
        val text = "${selected.size}/${all.size}"
        ctx.text(font, text, x + w - font.width(text), y + 3, 0xFF888899.toInt(), false)
    }

    private fun renderText(
        ctx: GuiGraphicsExtractor, value: Value<String>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val fullText = "\"${value.get()}\""
        val truncated = if (font.width(fullText) > w - 4) {
            font.plainSubstrByWidth(fullText, w - 10) + "..."
        } else {
            fullText
        }
        ctx.text(font, truncated, x + w - font.width(truncated), y + 3, 0xFFCCCCDD.toInt(), false)
    }

    private fun renderColor(
        ctx: GuiGraphicsExtractor, value: Value<Color4b>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val color = value.get()
        val hex = String.format("#%02X%02X%02X", color.r, color.g, color.b)
        val boxSize = 10
        val boxX = x + w - font.width(hex) - boxSize - 4
        val boxY = y + 3
        val argb = (color.a.toInt() shl 24) or (color.r.toInt() shl 16) or (color.g.toInt() shl 8) or color.b.toInt()
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, argb)
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0x66000000.toInt())
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + 1, 0x44FFFFFF.toInt())
        ctx.text(font, hex, boxX + boxSize + 3, y + 3, 0xFFCCCCDD.toInt(), false)
    }

    private fun renderBind(
        ctx: GuiGraphicsExtractor, value: Value<*>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val isListening = listeningForKey === value
        val displayText = when {
            isListening -> "[...] Press key"
            value is BindValue -> value.get().keyName
            else -> value.get().toString()
        }
        val btnW = (font.width(displayText) + 10).coerceAtMost(w)
        val btnX = x + w - btnW
        val btnH = rowHeight - 4
        val btnY = y + 2
        val hover = mx in btnX..<btnX + btnW && my in btnY..<btnY + btnH

        val bgColor = when {
            isListening -> 0xFF883322.toInt()
            hover -> 0xFF333355.toInt()
            else -> 0xFF222233.toInt()
        }
        ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, bgColor)
        ctx.fill(btnX, btnY + btnH, btnX + btnW, btnY + btnH + 1, 0xFF444466.toInt())
        ctx.text(font, displayText, btnX + 5, btnY + 2,
            if (isListening) 0xFFFFDD44.toInt() else 0xFFDDDDEE.toInt(), false)
    }

    private fun renderNone(
        ctx: GuiGraphicsExtractor, value: Value<*>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val text = value.get().toString()
        ctx.text(font, text, x + w - font.width(text), y + 3, 0xFF888899.toInt(), false)
    }

    // Mouse click handling

    private fun checkBackClick(mx: Int, my: Int): Boolean {
        if (mx in backButtonX..<backButtonX + backButtonW + 4 &&
            my in backButtonY..<backButtonY + backButtonH) {
            mc.gui.setScreen(SearchOverlay())
            return true
        }
        return false
    }

    private fun checkSettingRowClick(mx: Int, my: Int, controlAreaX: Int, controlWidth: Int): Boolean {
        val listX = width / 4
        val listW = width / 2

        for (row in settingRows) {
            val itemH = if (row.isGroup && row.isTopLevel) headerH else rowHeight
            if (my in row.y..<row.y + itemH &&
                mx >= listX && mx < listX + listW) {
                if (row.isGroup && row.isTopLevel) continue
                if (handleControlClick(row, mx, my, controlAreaX, controlWidth)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkScrollbarClick(mx: Int, my: Int): Boolean {
        val sh = height
        val font = mc.font
        val descText = module.description.get()
        val showDesc = !descText.isNullOrBlank()
        val topBarH = backButtonH + 11 + if (showDesc) font.lineHeight + 3 else 8
        val listStartY = topBarH
        val listEndY = sh - 4
        val listH = listEndY - listStartY
        val listX = width / 4
        val listW = width / 2
        val contentH = settingRows.size * (rowHeight + 1)
        val maxScroll = (contentH - listH).coerceAtLeast(0)

        if (maxScroll > 0) {
            val scrollBarX = listX + listW + 2
            val scrollBarW = 4
            if (mx in scrollBarX..<scrollBarX + scrollBarW) {
                scrollBarGrabbed = true
                scrollBarGrabY = my
                scrollBarGrabOffset = scrollOffset
                return true
            }
        }
        return false
    }

    override fun mouseClicked(context: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mx = context.x().toInt()
        val my = context.y().toInt()
        val button = context.button()
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(context, doubleClick)
        }

        var handled = checkBackClick(mx, my)
        if (!handled) {
            val listX = width / 4
            val listW = width / 2
            val controlAreaX = listX + listW * 3 / 5 + 4
            val controlWidth = (listW * 2 / 5 - 8).coerceAtLeast(40)
            handled = checkSettingRowClick(mx, my, controlAreaX, controlWidth) ||
                checkScrollbarClick(mx, my)
        }

        return handled || super.mouseClicked(context, doubleClick)
    }

    override fun mouseReleased(context: MouseButtonEvent): Boolean {
        scrollBarGrabbed = false
        return super.mouseReleased(context)
    }

    @Suppress("CognitiveComplexMethod", "LongMethod")
    private fun handleControlClick(row: SettingRow, mouseX: Int, mouseY: Int, controlAreaX: Int, controlWidth: Int): Boolean {
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
                val btnSize = 10
                val btnY = y + 3
                val decX = x
                val incX = x + w - btnSize
                if (mouseX in decX..<decX + btnSize && mouseY in btnY..<btnY + btnSize) {
                    value.set((value.get() - 1).coerceAtLeast(min))
                } else if (mouseX in incX..<incX + btnSize && mouseY in btnY..<btnY + btnSize) {
                    value.set((value.get() + 1).coerceAtMost(max))
                }
                true
            }
            ControlType.FLOAT_RANGE -> {
                val value = row.value as RangedValue<Float>
                val range = value.range
                val min = (range as ClosedRange<Float>).start
                val max = range.endInclusive
                val btnSize = 10
                val btnY = y + 3
                val decX = x
                val incX = x + w - btnSize
                val step = 0.5f
                if (mouseX in decX..<decX + btnSize && mouseY in btnY..<btnY + btnSize) {
                    value.set((value.get() - step).coerceAtLeast(min))
                } else if (mouseX in incX..<incX + btnSize && mouseY in btnY..<btnY + btnSize) {
                    value.set((value.get() + step).coerceAtMost(max))
                }
                true
            }
            ControlType.ENUM -> {
                val value = row.value as ChoiceListValue<*>
                val btnSize = 10
                val btnY = y + 3
                val decX = x
                val incX = x + w - btnSize
                val options = value.choices.toList()
                if (options.isNotEmpty()) {
                    val currentIdx = options.indexOf(value.get())
                    if (mouseX in decX..<decX + btnSize && mouseY in btnY..<btnY + btnSize) {
                        val newIdx = if (currentIdx <= 0) options.size - 1 else currentIdx - 1
                        @Suppress("UNCHECKED_CAST")
                        value.set(options[newIdx] as Nothing)
                    } else if (mouseX in incX..<incX + btnSize && mouseY in btnY..<btnY + btnSize) {
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
                val btnSize = 10
                val btnY = y + 3
                val decX = x
                val incX = x + w - btnSize
                val modeNames = value.modes.map { it.tag }
                if (modeNames.isNotEmpty()) {
                    val currentIdx = modeNames.indexOf(value.activeMode.tag)
                    if (mouseX in decX..<decX + btnSize && mouseY in btnY..<btnY + btnSize) {
                        val newIdx = if (currentIdx <= 0) modeNames.size - 1 else currentIdx - 1
                        value.setByString(modeNames[newIdx])
                    } else if (mouseX in incX..<incX + btnSize && mouseY in btnY..<btnY + btnSize) {
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

    // Scroll wheel

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (scrollBarGrabbed) return true
        val scrollDelta = (verticalAmount * 20).toInt()
        scrollOffset += scrollDelta
        scrollOffset = scrollOffset.coerceAtMost(0)
        return true
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        if (scrollBarGrabbed) {
            val sh = height
            val font = mc.font
            val descText = module.description.get()
            val showDesc = !descText.isNullOrBlank()
            val topBarH = backButtonH + 11 + if (showDesc) font.lineHeight + 3 else 8
            val listH = sh - 4 - topBarH
            val contentH = settingRows.size * (rowHeight + 1)
            val maxScroll = (contentH - listH).coerceAtLeast(0)
            if (maxScroll > 0) {
                val delta = (event.y().toInt() - scrollBarGrabY).toFloat()
                val thumbH = (listH.toFloat() / contentH.toFloat() * listH).toInt().coerceAtLeast(16)
                val scrollPerPixel = maxScroll.toFloat() / (listH - thumbH).toFloat()
                scrollOffset = (scrollBarGrabOffset - delta * scrollPerPixel).toInt().coerceIn(-maxScroll, 0)
            }
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }

    // Keyboard handling

    override fun keyPressed(input: KeyEvent): Boolean {
        if (input.key == GLFW.GLFW_KEY_ESCAPE) {
            if (listeningForKey != null) {
                listeningForKey = null
                return true
            }
            mc.gui.setScreen(SearchOverlay())
            return true
        }

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
        val isTopLevel: Boolean,
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
