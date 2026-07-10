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

import net.ccbluex.liquidbounce.render.drawHorizontalLine
import net.ccbluex.liquidbounce.render.drawQuad
import net.ccbluex.liquidbounce.render.drawTriangle
import net.ccbluex.liquidbounce.render.drawVerticalLine
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * Drawing utilities for ClickGUI icons (arrows, checkmarks, crosses, pins).
 * Adapted from Wurst7's ClickGuiIcons.
 */
object ClickGuiIcons {

    fun drawMinimizeArrow(
        context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float,
        hovering: Boolean, minimized: Boolean
    ) {
        val xa1 = x1 + 1
        val xa2 = (x1 + x2) / 2
        val xa3 = x2 - 1
        val ya1: Float
        val ya2: Float

        if (minimized) {
            ya1 = y1 + 3
            ya2 = y2 - 2.5f
            val arrowColor = if (hovering) Color4b(0x00, 0xFF, 0x00) else Color4b(0x00, 0xD9, 0x00)
            context.drawTriangle(xa1, ya1, xa2, ya2, xa3, ya1,
                fillColor = arrowColor, outlineColor = Color4b(0x80, 0x10, 0x10, 0x80))
        } else {
            ya1 = y2 - 3
            ya2 = y1 + 2.5f
            val arrowColor = if (hovering) Color4b(0xFF, 0x00, 0x00) else Color4b(0xD9, 0x00, 0x00)
            context.drawTriangle(xa1, ya1, xa3, ya1, xa2, ya2,
                fillColor = arrowColor, outlineColor = Color4b(0x80, 0x10, 0x10, 0x80))
        }
    }

    fun drawCheck(
        context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float,
        hovering: Boolean, grayedOut: Boolean
    ) {
        val xc1 = x1 + 2.5f
        val xc2 = x1 + 3.5f
        val xc3 = (x1 + x2) / 2 - 1
        val xc4 = x2 - 3.5f
        val xc5 = x2 - 2.5f
        val yc1 = y1 + 2.5f
        val yc2 = y1 + 3.5f
        val yc3 = (y1 + y2) / 2
        val yc4 = yc3 + 1
        val yc5 = y2 - 4.5f
        val yc6 = y2 - 2.5f

        val checkColor = when {
            grayedOut -> Color4b(0x80, 0x80, 0x80, 0xC0)
            hovering -> Color4b(0x00, 0xFF, 0x00)
            else -> Color4b(0x00, 0xD9, 0x00)
        }

        // Draw the check mark as small quads along the check path
        // First leg: from (xc2,yc3) to (xc3,yc5) to (xc3,yc6) 
        // Second leg: from (xc4,yc1) to (xc5,yc2) to (xc3,yc6)
        drawCheckQuad(context, xc2, yc3, xc3, yc5, xc3, yc6, checkColor)
        drawCheckQuad(context, xc4, yc1, xc5, yc2, xc3, yc6, checkColor)

        // Outline
        val outlineColor = Color4b(0x80, 0x10, 0x10, 0x80)
        drawCheckOutline(context, xc2, yc3, xc3, yc5, xc4, yc1, xc5, yc2, xc3, yc6, xc1, yc4, outlineColor)
    }

    private fun drawCheckQuad(context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, color: Color4b) {
        val xMin = minOf(x1, x2, x3)
        val yMin = minOf(y1, y2, y3)
        val xMax = maxOf(x1, x2, x3)
        val yMax = maxOf(y1, y2, y3)
        context.drawQuad(xMin, yMin, xMax, yMax, fillColor = color)
    }

    private fun drawCheckOutline(context: GuiGraphicsExtractor, vararg points: Float, color: Color4b) {
        // Draw horizontal/vertical lines for the check outline - simplified
        val argb = color.argb
        for (i in 0 until points.size - 2 step 2) {
            val x1 = points[i]; val y1 = points[i + 1]
            val x2 = points[i + 2]; val y2 = points[i + 3]
            val xMin = minOf(x1, x2); val yMin = minOf(y1, y2)
            val xMax = maxOf(x1, x2); val yMax = maxOf(y1, y2)
            val thickness = 0.5f
            if (kotlin.math.abs(xMax - xMin) > kotlin.math.abs(yMax - yMin)) {
                context.drawHorizontalLine(xMin, xMax, yMin, thickness, color)
            } else {
                context.drawVerticalLine(xMin, yMin, yMax, thickness, color)
            }
        }
    }

    fun drawCross(
        context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float,
        hovering: Boolean
    ) {
        val crossColor = if (hovering) Color4b(0xFF, 0x00, 0x00) else Color4b(0xD9, 0x00, 0x00)
        val centerX = (x1 + x2) / 2
        val centerY = (y1 + y2) / 2
        val halfSize = ((x2 - x1) / 3).coerceAtLeast(1f)

        // Draw two crossed lines
        context.drawHorizontalLine(centerX - halfSize, centerX + halfSize, centerY - halfSize, 1f, crossColor)
        context.drawHorizontalLine(centerX - halfSize, centerX + halfSize, centerY + halfSize, 1f, crossColor)
        context.drawVerticalLine(centerX - halfSize, centerY - halfSize, centerY + halfSize, 1f, crossColor)
        context.drawVerticalLine(centerX + halfSize, centerY - halfSize, centerY + halfSize, 1f, crossColor)
    }

    fun drawPin(
        context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float,
        hovering: Boolean, pinned: Boolean
    ) {
        if (pinned) {
            val knobColor = if (hovering) Color4b(0xFF, 0x00, 0x00) else Color4b(0xD9, 0x00, 0x00)
            val needleColor = if (hovering) Color4b(0xFF, 0xFF, 0xFF) else Color4b(0xD9, 0xD9, 0xD9)
            val outlineColor = Color4b(0x80, 0x10, 0x10, 0x80)

            // Pin knob - a filled circle/rect at the top
            context.drawQuad(x1 + 2, y1 + 2, x2 - 2, y2 - 2, fillColor = knobColor)
            // Needle - a narrow rect below
            context.drawQuad(x1 + 3.5f, y2 - 2, x2 - 3.5f, y2 + 0.5f, fillColor = needleColor)
            // Outline
            context.drawQuad(x1 + 2, y1 + 2, x2 - 2, y2 - 2, outlineColor = outlineColor)
            context.drawQuad(x1 + 3.5f, y2 - 2, x2 - 3.5f, y2 + 0.5f, outlineColor = outlineColor)
        } else {
            val knobColor = if (hovering) Color4b(0x00, 0xFF, 0x00) else Color4b(0x00, 0xD9, 0x00)
            val needleColor = if (hovering) Color4b(0xFF, 0xFF, 0xFF) else Color4b(0xD9, 0xD9, 0xD9)
            val outlineColor = Color4b(0x80, 0x10, 0x10, 0x80)

            // Pin knob - draw as a small rotated square
            context.drawQuad(x2 - 3.5f, y1 + 0.5f, x2 - 0.5f, y2 - 0.5f, fillColor = knobColor)
            // Needle - a small triangle below
            context.drawQuad(x1 + 1, y2 - 1, x1 + 4, y2 - 4, fillColor = needleColor)
            // Outlines
            context.drawQuad(x2 - 3.5f, y1 + 0.5f, x2 - 0.5f, y2 - 0.5f, outlineColor = outlineColor)
            context.drawQuad(x1 + 1, y2 - 1, x1 + 4, y2 - 4, outlineColor = outlineColor)
        }
    }

}
