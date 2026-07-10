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
            val arrowColor = if (hovering) 0xFF00FF00.toInt() else 0xFF00D900.toInt()
            val arrowVertices = arrayOf(floatArrayOf(xa1, ya1), floatArrayOf(xa2, ya2), floatArrayOf(xa3, ya1))
            fillTriangle2D(context, arrowVertices, arrowColor)
            drawLineStrip2D(context, arrowVertices, 0x80101010)
        } else {
            ya1 = y2 - 3
            ya2 = y1 + 2.5f
            val arrowColor = if (hovering) 0xFFFF0000.toInt() else 0xFFD90000.toInt()
            val arrowVertices = arrayOf(floatArrayOf(xa1, ya1), floatArrayOf(xa3, ya1), floatArrayOf(xa2, ya2))
            fillTriangle2D(context, arrowVertices, arrowColor)
            drawLineStrip2D(context, arrowVertices, 0x80101010)
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
            grayedOut -> 0xC0808080
            hovering -> 0xFF00FF00.toInt()
            else -> 0xFF00D900.toInt()
        }
        val checkVertices = arrayOf(
            floatArrayOf(xc2, yc3), floatArrayOf(xc1, yc4), floatArrayOf(xc3, yc6),
            floatArrayOf(xc3, yc5), floatArrayOf(xc3, yc5), floatArrayOf(xc3, yc6),
            floatArrayOf(xc5, yc2), floatArrayOf(xc4, yc1)
        )
        fillQuads2D(context, checkVertices, checkColor)

        val outlineVertices = arrayOf(
            floatArrayOf(xc2, yc3), floatArrayOf(xc3, yc5), floatArrayOf(xc4, yc1),
            floatArrayOf(xc5, yc2), floatArrayOf(xc3, yc6), floatArrayOf(xc1, yc4),
            floatArrayOf(xc2, yc3)
        )
        drawLineStrip2D(context, outlineVertices, 0x80101010)
    }

    fun drawCross(
        context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float,
        hovering: Boolean
    ) {
        val xc1 = x1 + 2; val xc2 = x1 + 3; val xc3 = x2 - 2; val xc4 = x2 - 3
        val xc5 = x1 + 3.5f; val xc6 = (x1 + x2) / 2; val xc7 = x2 - 3.5f
        val yc1 = y1 + 3; val yc2 = y1 + 2; val yc3 = y2 - 3; val yc4 = y2 - 2
        val yc5 = y1 + 3.5f; val yc6 = (y1 + y2) / 2; val yc7 = y2 - 3.5f

        val crossColor = if (hovering) 0xFFFF0000.toInt() else 0xFFD90000.toInt()
        val crossVertices = arrayOf(
            floatArrayOf(xc2, yc2), floatArrayOf(xc1, yc1), floatArrayOf(xc4, yc4),
            floatArrayOf(xc3, yc3), floatArrayOf(xc3, yc1), floatArrayOf(xc4, yc2),
            floatArrayOf(xc6, yc5), floatArrayOf(xc7, yc6), floatArrayOf(xc6, yc7),
            floatArrayOf(xc5, yc6), floatArrayOf(xc1, yc3), floatArrayOf(xc2, yc4)
        )
        fillQuads2D(context, crossVertices, crossColor)

        val outlineVertices = arrayOf(
            floatArrayOf(xc1, yc1), floatArrayOf(xc2, yc2), floatArrayOf(xc6, yc5),
            floatArrayOf(xc4, yc2), floatArrayOf(xc3, yc1), floatArrayOf(xc7, yc6),
            floatArrayOf(xc3, yc3), floatArrayOf(xc4, yc4), floatArrayOf(xc6, yc7),
            floatArrayOf(xc2, yc4), floatArrayOf(xc1, yc3), floatArrayOf(xc5, yc6)
        )
        drawLineStrip2D(context, outlineVertices, 0x80101010)
    }

    fun drawPin(
        context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float,
        hovering: Boolean, pinned: Boolean
    ) {
        val needleColor = if (hovering) 0xFFFFFFFF.toInt() else 0xFFD9D9D9.toInt()
        val outlineColor = 0x80101010

        if (pinned) {
            val xk1 = x1 + 2; val xk2 = x2 - 2; val xk3 = x1 + 1; val xk4 = x2 - 1
            val yk1 = y1 + 2; val yk2 = y2 - 2; val yk3 = y2 - 0.5f

            val knobColor = if (hovering) 0xFFFF0000.toInt() else 0xFFD90000.toInt()
            fill2D(context, xk1, yk1, xk2, yk2, knobColor)
            fill2D(context, xk3, yk2, xk4, yk3, knobColor)

            val xn1 = x1 + 3.5f; val xn2 = x2 - 3.5f; val yn1 = y2 - 0.5f; val yn2 = y2
            fill2D(context, xn1, yn1, xn2, yn2, needleColor)

            drawBorder2D(context, xk1, yk1, xk2, yk2, outlineColor)
            drawBorder2D(context, xk3, yk2, xk4, yk3, outlineColor)
            drawBorder2D(context, xn1, yn1, xn2, yn2, outlineColor)
        } else {
            val knobColor = if (hovering) 0xFF00FF00.toInt() else 0xFF00D900.toInt()
            val xk4 = x1 + 3; val xk3 = x2 - 3; val xk2 = x2 - 0.5f; val xk1 = x2 - 3.5f
            val yk4 = y1 + 3; val yk3 = y2 - 3; val yk2 = y1 + 3.5f; val yk1 = y1 + 0.5f
            val knobVertices = arrayOf(
                floatArrayOf(xk4, yk4), floatArrayOf(xk3, yk3), floatArrayOf(xk2, yk2), floatArrayOf(xk1, yk1)
            )
            fillQuads2D(context, knobVertices, knobColor)
            drawLineStrip2D(context, knobVertices, outlineColor)

            val xn3 = x1 + 1; val xn2 = x1 + 4; val xn1 = x1 + 3; val yn3 = y2 - 1; val yn2 = y2 - 3; val yn1 = y2 - 4
            val needleVertices = arrayOf(
                floatArrayOf(xn3, yn3), floatArrayOf(xn2, yn2), floatArrayOf(xn1, yn1)
            )
            fillTriangle2D(context, needleVertices, needleColor)
            drawLineStrip2D(context, needleVertices, outlineColor)
        }
    }

    private val scale: Int get() = Minecraft.getInstance().window.guiScale

    private fun fill2D(context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        val s = scale
        context.pose().pushMatrix()
        context.pose().scale(1f / s)
        context.fill((x1 * s).toInt(), (y1 * s).toInt(), (x2 * s).toInt(), (y2 * s).toInt(), color)
        context.pose().popMatrix()
    }

    private fun fillTriangle2D(context: GuiGraphicsExtractor, vertices: Array<FloatArray>, color: Int) {
        if (vertices.size < 3) return
        val s = scale
        context.pose().pushMatrix()
        context.pose().scale(1f / s)
        for (i in 0 until vertices.size - 2) {
            val x1 = (vertices[i][0] * s).toInt()
            val y1 = (vertices[i][1] * s).toInt()
            val x2 = (vertices[i + 1][0] * s).toInt()
            val y2 = (vertices[i + 1][1] * s).toInt()
            val x3 = (vertices[i + 2][0] * s).toInt()
            val y3 = (vertices[i + 2][1] * s).toInt()
            context.fillTriangle(x1, y1, x2, y2, x3, y3, color)
        }
        context.pose().popMatrix()
    }

    private fun fillQuads2D(context: GuiGraphicsExtractor, vertices: Array<FloatArray>, color: Int) {
        if (vertices.size < 4) return
        val s = scale
        context.pose().pushMatrix()
        context.pose().scale(1f / s)
        for (i in 0 until vertices.size - 3) {
            val x1 = (vertices[i][0] * s).toInt()
            val y1 = (vertices[i][1] * s).toInt()
            val x2 = (vertices[i + 1][0] * s).toInt()
            val y2 = (vertices[i + 1][1] * s).toInt()
            val x3 = (vertices[i + 2][0] * s).toInt()
            val y3 = (vertices[i + 2][1] * s).toInt()
            val x4 = (vertices[i + 3][0] * s).toInt()
            val y4 = (vertices[i + 3][1] * s).toInt()
            context.fillTriangle(x1, y1, x2, y2, x3, y3, color)
            context.fillTriangle(x1, y1, x3, y3, x4, y4, color)
        }
        context.pose().popMatrix()
    }

    private fun drawLine2D(context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        val s = scale
        val x = x1 * s; val y = y1 * s
        val w = (x2 - x1) * s; val h = (y2 - y1) * s
        val angle = kotlin.math.atan2(h.toDouble(), w.toDouble()).toFloat()
        val length = kotlin.math.sqrt((w * w + h * h).toDouble()).toInt()

        context.pose().pushMatrix()
        context.pose().scale(1f / s)
        context.pose().translate(x, y)
        context.pose().rotate(angle, 0f, 0f, 1f)
        context.drawHorizontalLine(0, length - 1, 0, color)
        context.pose().popMatrix()
    }

    private fun drawBorder2D(context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        val s = scale
        val x = (x1 * s).toInt(); val y = (y1 * s).toInt()
        val w = ((x2 - x1) * s).toInt(); val h = ((y2 - y1) * s).toInt()

        context.pose().pushMatrix()
        context.pose().scale(1f / s)
        context.drawHorizontalLine(x, x + w - 1, y, color)
        context.drawHorizontalLine(x, x + w - 1, y + h - 1, color)
        context.drawVerticalLine(x, y + 1, y + h - 2, color)
        context.drawVerticalLine(x + w - 1, y + 1, y + h - 2, color)
        context.pose().popMatrix()
    }

    private fun drawLineStrip2D(context: GuiGraphicsExtractor, vertices: Array<FloatArray>, color: Int) {
        if (vertices.size < 2) return
        for (i in 1 until vertices.size) {
            drawLine2D(context, vertices[i - 1][0], vertices[i - 1][1], vertices[i][0], vertices[i][1], color)
        }
        drawLine2D(context, vertices[vertices.size - 1][0], vertices[vertices.size - 1][1], vertices[0][0], vertices[0][1], color)
    }
}
