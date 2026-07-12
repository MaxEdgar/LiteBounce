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
@file:Suppress("BracesOnIfStatements")

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
@Suppress("CognitiveComplexMethod", "LongMethod", "TooManyFunctions", "LargeClass")
class SettingsScreen(private val module: ClientModule) : Screen(Component.literal("")) {

    private var scrollOffset = 0
    private var scrollBarGrabbed = false
    private var scrollBarGrabY = 0
    private var scrollBarGrabOffset = 0
    private var listeningForKey: BindValue? = null

    private var sliderDraggingRow: SettingRow? = null

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

    @Suppress("CognitiveComplexMethod", "LongMethod", "NestedBlockDepth")
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
        val controlWidth = (listW * 2 / 5 - 8).coerceAtLeast(60)

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
                    // Regular setting row (non-group, or inner group)
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

                    // Separator stub between name and control (only for non-groups)
                    if (!row.isGroup) {
                        val sepX = rowX + font.width(displayName) + 6
                        val sepStubW = 8
                        context.fill(sepX, itemY + itemH / 2, sepX + sepStubW, itemY + itemH / 2 + 1, 0xFF1A1A33.toInt())

                        // Render control
                        renderControl(context, row, controlAreaX, itemY, controlWidth, mouseX, mouseY)
                    }
                }
            }

            currentY += itemH + 1
        }

        // Scrollbar - wider and more visible
        if (maxScroll > 0) {
            val scrollBarX = listX + listW + 3
            val scrollBarW = 6
            // Thumb proportional height
            val thumbH = ((listH.toFloat() / contentH.toFloat()) * listH).toInt().coerceAtLeast(20)
            val scrollTrack = listH - thumbH
            val thumbY = if (scrollTrack > 0) {
                listStartY + ((-scrollOffset.toFloat() / maxScroll) * scrollTrack).toInt()
            } else listStartY

            // Track background
            context.fill(scrollBarX, listStartY, scrollBarX + scrollBarW, listStartY + scrollBarH, 0xFF151528.toInt())
            // Track border
            context.fill(scrollBarX, listStartY, scrollBarX + 1, listStartY + scrollBarH, 0xFF222244.toInt())

            // Thumb
            val thumbHover = mouseX in scrollBarX..<scrollBarX + scrollBarW && mouseY in thumbY..<thumbY + thumbH
            context.fill(scrollBarX, thumbY, scrollBarX + scrollBarW, thumbY + thumbH,
                if (thumbHover) 0xFF6666BB.toInt() else 0xFF3A3A77.toInt())
            // Thumb highlight
            context.fill(scrollBarX + 1, thumbY, scrollBarX + scrollBarW - 1, thumbY + 1, 0xFF5555AA.toInt())
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
            value.valueType == ValueType.INT -> ControlType.INT_SLIDER
            value.valueType == ValueType.FLOAT -> ControlType.FLOAT_SLIDER
            value.valueType == ValueType.INT_RANGE -> ControlType.RANGE_TEXT
            value.valueType == ValueType.FLOAT_RANGE -> ControlType.RANGE_TEXT
            value.valueType == ValueType.TEXT -> ControlType.TEXT
            value.valueType == ValueType.COLOR -> ControlType.COLOR
            value.valueType == ValueType.KEY -> ControlType.BIND
            // Registry types — show as text
            value.valueType == ValueType.BLOCK || value.valueType == ValueType.ITEM ||
                value.valueType == ValueType.ENCHANTMENT || value.valueType == ValueType.SOUND_EVENT ||
                value.valueType == ValueType.MOB_EFFECT || value.valueType == ValueType.MENU ||
                value.valueType == ValueType.ENTITY_TYPE || value.valueType == ValueType.C2S_PACKET ||
                value.valueType == ValueType.S2C_PACKET || value.valueType == ValueType.CLIENT_MODULE ||
                value.valueType == ValueType.FILE -> ControlType.TEXT
            // Vectors / curves — show as text
            value.valueType == ValueType.VECTOR3_I || value.valueType == ValueType.VECTOR3_D ||
                value.valueType == ValueType.VECTOR2_F || value.valueType == ValueType.CURVE -> ControlType.TEXT
            // List types
            value.valueType == ValueType.LIST || value.valueType == ValueType.MUTABLE_LIST ||
                value.valueType == ValueType.NAMED_ITEM_LIST || value.valueType == ValueType.REGISTRY_LIST ||
                value.valueType == ValueType.REGISTRY_MUTABLE_LIST -> ControlType.TEXT
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
            ControlType.INT_SLIDER -> renderSlider(context, row.value as RangedValue<Int>, x, y, w, mouseX, mouseY)
            ControlType.FLOAT_SLIDER -> renderSlider(context, row.value as RangedValue<Float>, x, y, w, mouseX, mouseY)
            ControlType.ENUM -> renderStepper(context, row.value as ChoiceListValue<*>, x, y, w, mouseX, mouseY) { (it as ChoiceListValue<*>).get().toString() }
            ControlType.MULTI_ENUM -> renderMultiEnum(context, row.value as MultiChoiceListValue<*>, x, y, w)
            ControlType.TEXT -> renderText(context, row.value as Value<String>, x, y, w)
            ControlType.COLOR -> renderColor(context, row.value as Value<Color4b>, x, y, w, mouseX, mouseY)
            ControlType.BIND -> renderBind(context, row.value, x, y, w, mouseX, mouseY)
            ControlType.TOGGLE_GROUP -> renderToggle(context, findEnabledValue(row.value as ToggleableValueGroup), x, y, w, mouseX, mouseY)
            ControlType.MODE_GROUP -> renderStepper(context, row.value as ModeValueGroup<*>, x, y, w, mouseX, mouseY) { (it as ModeValueGroup<*>).activeMode.tag }
            ControlType.RANGE_TEXT -> renderRangeText(context, row.value as RangedValue<*>, x, y, w)
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

    private data class SliderGeometry(
        val sliderX: Int,
        val sliderY: Int,
        val sliderW: Int,
        val sliderH: Int,
        val displayText: String
    )

    /** Compute slider layout geometry for a ranged value at the given position */
    private fun computeSliderGeometry(value: RangedValue<*>, x: Int, y: Int, w: Int): SliderGeometry? {
        val range = value.range
        val min = (range.start as Number).toDouble()
        val max = (range.endInclusive as Number).toDouble()
        if (max - min <= 0.0) return null

        val font = mc.font
        val current = (value.get() as Number).toDouble()
        val displayText = if (range.start is Float) String.format("%.1f", current) else current.toInt().toString()
        val textW = font.width(displayText)
        val sliderW = (w - textW - 6).coerceAtLeast(30)
        return SliderGeometry(x, y + 4, sliderW, 8, displayText)
    }

    /** Draggable slider control for int/float ranged values */
    private fun renderSlider(
        ctx: GuiGraphicsExtractor, value: RangedValue<*>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val geo = computeSliderGeometry(value, x, y, w) ?: return
        val font = mc.font
        val range = value.range
        val min = (range.start as Number).toDouble()
        val max = (range.endInclusive as Number).toDouble()
        val current = (value.get() as Number).toDouble()

        val fraction = ((current - min) / (max - min)).toFloat().coerceIn(0f, 1f)
        val fillEnd = geo.sliderX + (geo.sliderW * fraction).toInt()

        // Track background
        ctx.fill(geo.sliderX, geo.sliderY, geo.sliderX + geo.sliderW, geo.sliderY + geo.sliderH, 0xFF222233.toInt())

        // Filled portion (green)
        if (fraction > 0f) {
            ctx.fill(geo.sliderX, geo.sliderY, fillEnd, geo.sliderY + geo.sliderH, 0xFF2D5A27.toInt())
        }

        // Thumb (draggable handle)
        val thumbSize = 6
        val thumbX = (fillEnd - thumbSize / 2).coerceIn(geo.sliderX, geo.sliderX + geo.sliderW - thumbSize)
        val thumbY = geo.sliderY - 1
        val thumbHover = mx in thumbX..<thumbX + thumbSize && my in thumbY..<thumbY + geo.sliderH + 2
        ctx.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + geo.sliderH + 2,
            if (thumbHover || sliderDraggingRow?.value === value) 0xFF66CC66.toInt() else 0xFF44AA44.toInt())

        // Tick marks at quarter positions
        for (t in 1..3) {
            val tickX = geo.sliderX + (geo.sliderW * t / 4).toInt()
            ctx.fill(tickX, geo.sliderY + geo.sliderH - 2, tickX + 1, geo.sliderY + geo.sliderH, 0xFF334433.toInt())
        }

        // Value text (right-aligned after the slider)
        ctx.text(font, geo.displayText, geo.sliderX + geo.sliderW + 4, y + 3, 0xFFDDDDEE.toInt(), false)
    }

    /** Left/right stepper control for enums and mode groups */
    private fun renderStepper(
        ctx: GuiGraphicsExtractor, value: Any,
        x: Int, y: Int, w: Int, mx: Int, my: Int,
        displayFn: (Any) -> String
    ) {
        val font = mc.font
        val text = displayFn(value)
        val btnSize = rowHeight - 4
        val btnY = y + 2
        val decX = x
        val incX = x + w - btnSize
        val decHover = mx in decX..<decX + btnSize && my in btnY..<btnY + btnSize
        val incHover = mx in incX..<incX + btnSize && my in btnY..<btnY + btnSize

        // Left arrow button
        val decBg = if (decHover) 0xFF333366.toInt() else 0xFF1A1A33.toInt()
        ctx.fill(decX, btnY, decX + btnSize, btnY + btnSize, decBg)
        ctx.fill(decX + btnSize, btnY, decX + btnSize + 1, btnY + btnSize, 0xFF2A2A55.toInt())
        ctx.text(font, "<", decX + (btnSize - font.width("<")) / 2, btnY + 2,
            if (decHover) 0xFFFFCC44.toInt() else 0xFF9999BB.toInt(), false)

        // Value text (clickable strip between arrows)
        val textX = decX + btnSize + 2
        val textW = (incX - textX).coerceAtLeast(0)
        val textHover = mx in textX..<textX + textW && my in y..<y + rowHeight
        if (textHover) {
            ctx.fill(textX, btnY, textX + textW, btnY + btnSize, 0xFF222244.toInt())
        }
        val displayText = if (font.width(text) > textW - 4) {
            font.plainSubstrByWidth(text, textW - 8) + ".."
        } else text
        ctx.text(font, displayText, textX + (textW - font.width(displayText)) / 2, y + 3,
            if (textHover) 0xFFFFDD88.toInt() else 0xFFDDDDEE.toInt(), false)

        // Right arrow button
        val incBg = if (incHover) 0xFF333366.toInt() else 0xFF1A1A33.toInt()
        ctx.fill(incX, btnY, incX + btnSize, btnY + btnSize, incBg)
        ctx.fill(incX, btnY, incX + 1, btnY + btnSize, 0xFF2A2A55.toInt())
        ctx.text(font, ">", incX + (btnSize - font.width(">")) / 2, btnY + 2,
            if (incHover) 0xFFFFCC44.toInt() else 0xFF9999BB.toInt(), false)
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
        ctx: GuiGraphicsExtractor, value: Value<*>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val raw = value.get().toString()
        val fullText = if (font.width(raw) > w - 4) {
            font.plainSubstrByWidth(raw, w - 10) + "..."
        } else {
            raw
        }
        ctx.text(font, fullText, x + w - font.width(fullText), y + 3, 0xFFCCCCDD.toInt(), false)
    }

    private var colorPickerEditing: Value<Color4b>? = null

    private fun renderColor(
        ctx: GuiGraphicsExtractor, value: Value<Color4b>,
        x: Int, y: Int, w: Int, mx: Int, my: Int
    ) {
        val font = mc.font
        val color = value.get()
        val hex = String.format("#%02X%02X%02X", color.r, color.g, color.b)
        val boxSize = rowHeight - 4
        val boxX = x + w - font.width(hex) - boxSize - 6
        val boxY = y + 2
        val argb = (color.a.toInt() shl 24) or (color.r.toInt() shl 16) or (color.g.toInt() shl 8) or color.b.toInt()
        val boxHover = mx in boxX..<boxX + boxSize && my in boxY..<boxY + boxSize

        // Color box
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, argb)
        if (boxHover) {
            ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0x44FFFFFF.toInt())
        }
        // Outline
        ctx.fill(boxX, boxY, boxX + boxSize, boxY + 1, 0x88FFFFFF.toInt())
        ctx.fill(boxX, boxY + boxSize - 1, boxX + boxSize, boxY + boxSize, 0x44000000.toInt())
        ctx.fill(boxX, boxY, boxX + 1, boxY + boxSize, 0x88FFFFFF.toInt())
        ctx.fill(boxX + boxSize - 1, boxY, boxX + boxSize, boxY + boxSize, 0x44000000.toInt())

        // Hex text
        ctx.text(font, hex, boxX + boxSize + 4, y + 3,
            if (boxHover) 0xFFFFCC44.toInt() else 0xFFCCCCDD.toInt(), false)

        // Inline color editor (when clicked)
        if (colorPickerEditing === value) {
            renderColorEditor(ctx, value, x, y, w)
        }
    }

    /** Simple inline RGB sliders for editing a color */
    private fun renderColorEditor(
        ctx: GuiGraphicsExtractor, value: Value<Color4b>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val color = value.get()
        val editorX = x
        val editorY = y + rowHeight + 2
        val editorH = 60
        val sliderW = w
        val sliderH = 8

        ctx.fill(editorX, editorY, editorX + w, editorY + editorH, 0xCC1A1A2E.toInt())
        ctx.fill(editorX, editorY, editorX + w, editorY + 1, 0xFF333366.toInt())

        // R slider
        val rLabel = "R"
        val rVal = font.width("255")
        ctx.text(font, rLabel, editorX + 2, editorY + 3, 0xFFFF6666.toInt(), false)
        ctx.text(font, "${color.r}", editorX + w - rVal - 2, editorY + 3, 0xFFDDDDEE.toInt(), false)
        drawMiniSlider(ctx, editorX + 12, editorY + 3, sliderW - 16 - rVal - 4, sliderH,
            color.r.toFloat() / 255f, 0xFF660000.toInt(), 0xFFFF0000.toInt())
        // G slider
        ctx.text(font, "G", editorX + 2, editorY + 17, 0xFF66FF66.toInt(), false)
        ctx.text(font, "${color.g}", editorX + w - rVal - 2, editorY + 17, 0xFFDDDDEE.toInt(), false)
        drawMiniSlider(ctx, editorX + 12, editorY + 17, sliderW - 16 - rVal - 4, sliderH,
            color.g.toFloat() / 255f, 0xFF006600.toInt(), 0xFF00FF00.toInt())
        // B slider
        ctx.text(font, "B", editorX + 2, editorY + 31, 0xFF6666FF.toInt(), false)
        ctx.text(font, "${color.b}", editorX + w - rVal - 2, editorY + 31, 0xFFDDDDEE.toInt(), false)
        drawMiniSlider(ctx, editorX + 12, editorY + 31, sliderW - 16 - rVal - 4, sliderH,
            color.b.toFloat() / 255f, 0xFF000066.toInt(), 0xFF0000FF.toInt())
        // A slider
        ctx.text(font, "A", editorX + 2, editorY + 45, 0xFFAAAAAA.toInt(), false)
        ctx.text(font, "${color.a}", editorX + w - rVal - 2, editorY + 45, 0xFFDDDDEE.toInt(), false)
        drawMiniSlider(ctx, editorX + 12, editorY + 45, sliderW - 16 - rVal - 4, sliderH,
            color.a.toFloat() / 255f, 0xFF444444.toInt(), 0xFFFFFFFF.toInt())
    }

    private fun drawMiniSlider(
        ctx: GuiGraphicsExtractor, x: Int, y: Int, w: Int, h: Int,
        fraction: Float, colorStart: Int, colorEnd: Int
    ) {
        val fillEnd = x + (w * fraction).toInt().coerceIn(0, w)
        ctx.fill(x, y, x + w, y + h, 0xFF222233.toInt())
        ctx.fill(x, y, fillEnd, y + h, colorStart)
        // Gradient overlay
        ctx.fill(fillEnd, y, x + w, y + h, colorEnd and 0x00FFFFFF or 0x33000000)
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

    /** Displays int/float range pairs as text (e.g., "3.0-8.0") */
    private fun renderRangeText(
        ctx: GuiGraphicsExtractor, value: RangedValue<*>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val displayText = when (val v = value.get()) {
            is ClosedRange<*> -> "${v.start}-${v.endInclusive}"
            else -> v.toString()
        }
        ctx.text(font, displayText, x + w - font.width(displayText), y + 3, 0xFF88AAFF.toInt(), false)
    }

    private fun renderNone(
        ctx: GuiGraphicsExtractor, value: Value<*>,
        x: Int, y: Int, w: Int
    ) {
        val font = mc.font
        val raw = value.get().toString()
        // Guard against displaying raw Value.toString() format (e.g. "ChoiceListValue(name=X, type=Y)")
        val display = if ((value::class.simpleName ?: "").let { raw.startsWith("$it(") }) {
            "<${value.valueType}>"
        } else {
            raw
        }
        ctx.text(font, display, x + w - font.width(display), y + 3, 0xFF888899.toInt(), false)
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
                if (row.isGroup) continue // inner groups are just containers
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
            val controlWidth = (listW * 2 / 5 - 8).coerceAtLeast(60)
            handled = checkSettingRowClick(mx, my, controlAreaX, controlWidth) ||
                checkScrollbarClick(mx, my)
        }

        return handled || super.mouseClicked(context, doubleClick)
    }

    override fun mouseReleased(context: MouseButtonEvent): Boolean {
        scrollBarGrabbed = false
        sliderDraggingRow = null
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
            ControlType.INT_SLIDER -> {
                handleSliderPress(row.value as RangedValue<Int>, row, mouseX, mouseY, x, y, w)
                true
            }
            ControlType.FLOAT_SLIDER -> {
                handleSliderPress(row.value as RangedValue<Float>, row, mouseX, mouseY, x, y, w)
                true
            }
            ControlType.ENUM -> {
                val value = row.value as ChoiceListValue<*>
                val btnSize = rowHeight - 4
                val btnY = y + 2
                val decX = x
                val incX = x + w - btnSize
                val options = value.choices.toList()
                if (options.isNotEmpty()) {
                    val currentIdx = options.indexOf(value.get())
                    val onLeftBtn = mouseX in decX..<decX + btnSize && mouseY in btnY..<btnY + btnSize
                    val onRightBtn = mouseX in incX..<incX + btnSize && mouseY in btnY..<btnY + btnSize
                    val onText = mouseX in decX + btnSize..<incX && mouseY in y..<y + rowHeight

                    if (onLeftBtn) {
                        val newIdx = if (currentIdx <= 0) options.size - 1 else currentIdx - 1
                        @Suppress("UNCHECKED_CAST")
                        value.set(options[newIdx] as Nothing)
                    } else if (onRightBtn || onText) {
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
                val btnSize = rowHeight - 4
                val btnY = y + 2
                val decX = x
                val incX = x + w - btnSize
                val modeNames = value.modes.map { it.tag }
                if (modeNames.isNotEmpty()) {
                    val currentIdx = modeNames.indexOf(value.activeMode.tag)
                    val onLeftBtn = mouseX in decX..<decX + btnSize && mouseY in btnY..<btnY + btnSize
                    val onRightBtn = mouseX in incX..<incX + btnSize && mouseY in btnY..<btnY + btnSize
                    val onText = mouseX in decX + btnSize..<incX && mouseY in y..<y + rowHeight

                    if (onLeftBtn) {
                        val newIdx = if (currentIdx <= 0) modeNames.size - 1 else currentIdx - 1
                        value.setByString(modeNames[newIdx])
                    } else if (onRightBtn || onText) {
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
            ControlType.COLOR -> {
                val value = row.value as Value<Color4b>
                colorPickerEditing = if (colorPickerEditing === value) null else value
                true
            }
            else -> false
        }
    }

    /** Handle clicking or starting a drag on a slider */
    private fun handleSliderPress(value: RangedValue<*>, row: SettingRow, mouseX: Int, mouseY: Int, x: Int, y: Int, w: Int) {
        val range = value.range
        val min = (range.start as Number).toDouble()
        val max = (range.endInclusive as Number).toDouble()
        if (max - min <= 0.0) return

        val geo = computeSliderGeometry(value, x, y, w) ?: return

        // Check if click is on the slider track
        if (mouseY in geo.sliderY..<geo.sliderY + geo.sliderH && mouseX >= geo.sliderX && mouseX < geo.sliderX + geo.sliderW) {
            val fraction = ((mouseX - geo.sliderX).toFloat() / geo.sliderW).coerceIn(0f, 1f)
            val newVal = min + fraction * (max - min)
            @Suppress("UNCHECKED_CAST")
            fun setSliderValue(v: RangedValue<*>, nv: Double) {
                when {
                    v.range.start is Int -> v.set(newVal.toInt().coerceIn(min.toInt(), max.toInt()) as Nothing)
                    v.range.start is Float -> v.set(newVal.toFloat().coerceIn(min.toFloat(), max.toFloat()) as Nothing)
                }
            }
            setSliderValue(value, newVal)
            sliderDraggingRow = row
        }
    }

    /** Update a slider value based on mouse position during drag */
    private fun updateSliderDrag(mouseX: Int, mouseY: Int) {
        val row = sliderDraggingRow ?: return
        val value = row.value as? RangedValue<*> ?: return
        val range = value.range
        val min = (range.start as Number).toDouble()
        val max = (range.endInclusive as Number).toDouble()
        if (max - min <= 0.0) return

        val listX = width / 4
        val listW = width / 2
        val controlAreaX = listX + listW * 3 / 5 + 4
        val controlWidth = (listW * 2 / 5 - 8).coerceAtLeast(60)
        val geo = computeSliderGeometry(value, controlAreaX, row.y, controlWidth) ?: return

        if (mouseY in (geo.sliderY - 4)..<(geo.sliderY + geo.sliderH + 4) && mouseX >= geo.sliderX && mouseX < geo.sliderX + geo.sliderW) {
            val fraction = ((mouseX - geo.sliderX).toFloat() / geo.sliderW).coerceIn(0f, 1f)
            val newVal = min + fraction * (max - min)
            @Suppress("UNCHECKED_CAST")
            fun setSliderValue(v: RangedValue<*>, nv: Double) {
                when {
                    v.range.start is Int -> v.set(newVal.toInt().coerceIn(min.toInt(), max.toInt()) as Nothing)
                    v.range.start is Float -> v.set(newVal.toFloat().coerceIn(min.toFloat(), max.toFloat()) as Nothing)
                }
            }
            setSliderValue(value, newVal)
        }
    }

    // Scroll wheel

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (scrollBarGrabbed) return true
        colorPickerEditing = null
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
                val thumbH = ((listH.toFloat() / contentH.toFloat()) * listH).toInt().coerceAtLeast(20)
                val scrollTrack = listH - thumbH
                val scrollPerPixel = if (scrollTrack > 0) maxScroll.toFloat() / scrollTrack else 0f
                scrollOffset = (scrollBarGrabOffset - delta * scrollPerPixel).toInt().coerceIn(-maxScroll, 0)
            }
            return true
        }

        if (sliderDraggingRow != null) {
            updateSliderDrag(event.x().toInt(), event.y().toInt())
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
        INT_SLIDER,
        FLOAT_SLIDER,
        RANGE_TEXT,
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
