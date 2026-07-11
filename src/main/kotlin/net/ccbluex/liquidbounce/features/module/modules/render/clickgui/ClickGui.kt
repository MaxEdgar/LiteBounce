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

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.features.module.ModuleCategories
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.components.ModuleButton
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.ccbluex.liquidbounce.render.drawHorizontalLine
import net.ccbluex.liquidbounce.render.drawVerticalLine
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Main ClickGUI manager that handles windows, popups, and rendering.
 * Adapted from Wurst7's ClickGui class.
 */
@Suppress("TooManyFunctions", "CognitiveComplexMethod", "PrintStackTrace", "LoopWithTooManyJumpStatements", "BracesOnIfStatements", "MaxLineLength")
class ClickGui(private val windowsFile: Path) {

    private val mc: Minecraft get() = Minecraft.getInstance()
    private val windows = mutableListOf<Window>()
    private val popups = mutableListOf<Popup>()

    private var bgColor = floatArrayOf(0.25f, 0.25f, 0.25f)
    private var acColor = floatArrayOf(0.06f, 0.06f, 0.06f)
    private var txtColor = 0xFFF0F0F0.toInt()
    private var opacity = 0.5f
    private var ttOpacity = 0.75f
    private var maxHeight = 200
    private var maxSettingsHeight = 200

    var tooltip: String = ""

    private var leftMouseButtonPressed = false

    fun init() {
        val windowMap = linkedMapOf<ModuleCategory, Window>()
        for (category in ModuleCategories.entries) {
            windowMap[category] = Window(category.tag)
        }

        for (module in ModuleManager.sortedBy { it.name }) {
            val category = module.category
            if (category != null && windowMap.containsKey(category)) {
                windowMap[category]!!.add(ModuleButton(module))
            }
        }

        windows.addAll(windowMap.values)

        // Add UI settings window
        val uiSettings = Window("UI Settings")
        windows.add(uiSettings)

        for (window in windows) {
            window.setMinimized(true)
        }

        var x = 5
        var y = 5
        val scaledWidth = mc.window.guiScaledWidth
        for (window in windows) {
            window.pack()

            if (x + window.getWidth() + 5 > scaledWidth) {
                x = 5
                y += 18
            }

            window.setX(x)
            window.setY(y)
            x += window.getWidth() + 5
        }

        // Load saved window positions
        try {
            BufferedReader(Files.newBufferedReader(windowsFile)).use { reader ->
                val json = JsonParser.parseReader(reader).asJsonObject
                for (window in windows) {
                    val jsonWindow = json.getAsJsonObject(window.getTitle()) ?: continue
                    jsonWindow.get("x")?.asInt?.let { window.setX(it) }
                    jsonWindow.get("y")?.asInt?.let { window.setY(it) }
                    jsonWindow.get("minimized")?.asBoolean?.let { window.setMinimized(it) }
                    jsonWindow.get("pinned")?.asBoolean?.let { window.setPinned(it) }
                }
            }
        } catch (_: NoSuchFileException) {
            saveWindows()
        } catch (e: Exception) {
            System.err.println("Failed to load ${windowsFile.fileName}")
            e.printStackTrace()
            saveWindows()
        }

        saveWindows()
    }

    private fun saveWindows() {
        val json = JsonObject()
        for (window in windows) {
            if (window.isClosable()) continue
            val jw = JsonObject()
            jw.addProperty("x", window.getActualX())
            jw.addProperty("y", window.getActualY())
            jw.addProperty("minimized", window.isMinimized())
            jw.addProperty("pinned", window.isPinned())
            json.add(window.getTitle(), jw)
        }
        try {
            BufferedWriter(Files.newBufferedWriter(windowsFile)).use { writer ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                writer.write(gson.toJson(json))
            }
        } catch (e: Exception) {
            System.err.println("Failed to save ${windowsFile.fileName}")
            e.printStackTrace()
        }
    }

    fun handleMouseClick(context: MouseButtonEvent) {
        val mouseX = context.x().toInt()
        val mouseY = context.y().toInt()
        val mouseButton = context.button()
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            leftMouseButtonPressed = true
        }

        val popupClicked = handlePopupMouseClick(mouseX, mouseY, mouseButton)

        if (!popupClicked) {
            handleWindowMouseClick(mouseX, mouseY, mouseButton, context)
            closeInvalidPopups()
        }

        windows.removeIf { it.isClosing() }
    }

    fun handleMouseRelease(mouseX: Double, mouseY: Double, mouseButton: Int) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            leftMouseButtonPressed = false
        }
    }

    fun handleMouseScroll(mouseX: Double, mouseY: Double, delta: Double) {
        val dWheel = (delta * 4).toInt()
        if (dWheel == 0) return

        for (i in windows.indices.reversed()) {
            val window = windows[i]
            if (!window.isScrollingEnabled() || window.isMinimized() || window.isInvisible()) continue
            if (mouseX < window.getX() || mouseY < window.getY() + 13) continue
            if (mouseX >= window.getX() + window.getWidth() || mouseY >= window.getY() + window.getHeight()) continue

            var scroll = window.getScrollOffset() + dWheel
            scroll = minOf(scroll, 0)
            scroll = maxOf(scroll, -window.getInnerHeight() + window.getHeight() - 13)
            window.setScrollOffset(scroll)
            closeInvalidPopups()
            break
        }
    }

    fun handleNavigatorMouseClick(
        cMouseX: Double, cMouseY: Double, mouseButton: Int,
        window: Window, context: MouseButtonEvent
    ) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            leftMouseButtonPressed = true
        }
        handleComponentMouseClick(window, cMouseX, cMouseY, mouseButton, context)
        closeInvalidPopups()
    }

    fun closePopupsOutsideArea(window: Window, x1: Int, y1: Int, x2: Int, y2: Int) {
        for (popup in popups) {
            val owner = popup.getOwner()
            if (owner.getParent() == window && !isComponentVisibleWithinBounds(owner, x1, y1, x2, y2)) {
                popup.close()
            }
        }
        popups.removeIf { it.isClosing() }
    }

    private fun handlePopupMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        closeInvalidPopups()

        for (i in popups.indices.reversed()) {
            val popup = popups[i]
            val owner = popup.getOwner()
            val parent = owner.getParent() ?: continue

            val x0 = parent.getX() + owner.getX()
            val y0 = parent.getY() + 13 + parent.getScrollOffset() + owner.getY()

            val x1 = x0 + popup.getX()
            val y1 = y0 + popup.getY()
            val x2 = x1 + popup.getWidth()
            val y2 = y1 + popup.getHeight()

            if (mouseX < x1 || mouseY < y1 || mouseX >= x2 || mouseY >= y2) continue

            popup.handleMouseClick(mouseX - x0, mouseY - y0, mouseButton)
            popups.removeAt(i)
            popups.add(popup)
            closeInvalidPopups()
            return true
        }
        return false
    }

    private fun closeInvalidPopups() {
        for (popup in popups) {
            val parent = popup.getOwner().getParent()
            if (parent == null || parent.isClosing() || !isPopupOwnerVisible(popup)) {
                popup.close()
            }
        }
        popups.removeIf { it.isClosing() }
    }

    private fun isPopupOwnerVisible(popup: Popup): Boolean {
        val owner = popup.getOwner()
        val parent = owner.getParent() ?: return false
        if (parent.isInvisible() || parent.isMinimized()) return false
        return isComponentVisibleWithinBounds(owner, parent.getX(), parent.getY() + 13,
            parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight())
    }

    private fun isComponentVisibleWithinBounds(c: Component, x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        val parent = c.getParent() ?: return false
        val cx1 = parent.getX() + c.getX()
        val cy1 = parent.getY() + 13 + parent.getScrollOffset() + c.getY()
        val cx2 = cx1 + c.getWidth()
        val cy2 = cy1 + c.getHeight()
        return cx2 > x1 && cx1 < x2 && cy2 > y1 && cy1 < y2
    }

    private fun handleWindowMouseClick(
        mouseX: Int, mouseY: Int, mouseButton: Int, context: MouseButtonEvent
    ) {
        for (i in windows.indices.reversed()) {
            val window = windows[i]
            if (window.isInvisible()) continue

            val x1 = window.getX(); val y1 = window.getY()
            val x2 = x1 + window.getWidth(); val y2 = y1 + window.getHeight()
            val y3 = y1 + 13

            if (mouseX < x1 || mouseY < y1 || mouseX >= x2 || mouseY >= y2) continue

            if (mouseY < y3) {
                handleTitleBarMouseClick(window, mouseX, mouseY, mouseButton)
            } else if (!window.isMinimized()) {
                window.validate()
                val cMouseX = mouseX - x1
                var cMouseY = mouseY - y3

                if (window.isScrollingEnabled() && mouseX >= x2 - 3) {
                    handleScrollbarMouseClick(window, cMouseX, cMouseY, mouseButton)
                } else {
                    if (window.isScrollingEnabled()) {
                        cMouseY -= window.getScrollOffset()
                    }
                    handleComponentMouseClick(window, cMouseX.toDouble(), cMouseY.toDouble(), mouseButton, context)
                }
            } else continue

            windows.removeAt(i)
            windows.add(window)
            break
        }
    }

    private fun handleTitleBarMouseClick(window: Window, mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return
        if (mouseY < window.getY() + 2 || mouseY >= window.getY() + 11) {
            window.startDragging(mouseX, mouseY)
            return
        }

        var x3 = window.getX() + window.getWidth()

        if (window.isClosable()) {
            x3 -= 11
            if (mouseX in x3 until x3 + 9) {
                window.close()
                return
            }
        }

        if (window.isPinnable()) {
            x3 -= 11
            if (mouseX in x3 until x3 + 9) {
                window.setPinned(!window.isPinned())
                saveWindows()
                return
            }
        }

        if (window.isMinimizable()) {
            x3 -= 11
            if (mouseX in x3 until x3 + 9) {
                window.setMinimized(!window.isMinimized())
                saveWindows()
                return
            }
        }

        window.startDragging(mouseX, mouseY)
    }

    private fun handleScrollbarMouseClick(window: Window, mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) return
        if (mouseX >= window.getWidth() - 1) return

        val outerHeight = window.getHeight() - 13
        val innerHeight = window.getInnerHeight()
        val maxScrollbarHeight = outerHeight - 2
        val scrollbarY = (outerHeight * (-window.getScrollOffset() / innerHeight.toDouble()) + 1).toInt()
        val scrollbarHeight = (maxScrollbarHeight * outerHeight / innerHeight).toInt()

        if (mouseY < scrollbarY || mouseY >= scrollbarY + scrollbarHeight) return
        window.startDraggingScrollbar(window.getY() + 13 + mouseY)
    }

    private fun handleComponentMouseClick(
        window: Window, mouseX: Double, mouseY: Double,
        mouseButton: Int, context: MouseButtonEvent
    ) {
        for (i2 in window.countChildren() - 1 downTo 0) {
            val c = window.getChild(i2)
            if (mouseX < c.getX() || mouseY < c.getY() ||
                mouseX >= c.getX() + c.getWidth() || mouseY >= c.getY() + c.getHeight()
            ) continue

            c.handleMouseClick(mouseX, mouseY, mouseButton, context)
            break
        }
    }

    private fun renderWindow(context: GuiGraphicsExtractor, window: Window, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (window.isInvisible()) return

        val x1 = window.getX(); val y1 = window.getY()
        val x2 = x1 + window.getWidth(); val y2 = y1 + window.getHeight()
        val hovering = mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2

        window.validate()

        // Window background
        val bgColor = toIntColor(bgColor, opacity)
        context.fill(x1, y1, x2, y2, bgColor)

        // Title bar
        val acColorInt = toIntColor(acColor, opacity * 0.8f)
        context.fill(x1, y1, x2, y1 + 13, acColorInt)

        // Window border
        drawBorder2D(context, x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), toIntColor(acColor, 0.5f))

        context.guiRenderState.up()

        // Title text
        val font = mc.font
        val txtColorInt = txtColor
        context.text(font, window.getTitle(), x1 + 2, y1 + 2, txtColorInt, false)

        // Minimize/pin/close buttons
        var bx = x2 - 1

        if (window.isClosable()) {
            bx -= 11
            val btnHover = hovering && mouseX >= bx && mouseX < bx + 9 && mouseY >= y1 + 2 && mouseY < y1 + 11
            ClickGuiIcons.drawCross(context, bx.toFloat(), (y1 + 1).toFloat(), (bx + 9).toFloat(), (y1 + 11).toFloat(), btnHover)
        }

        if (window.isPinnable()) {
            bx -= 11
            val btnHover = hovering && mouseX >= bx && mouseX < bx + 9 && mouseY >= y1 + 2 && mouseY < y1 + 11
            ClickGuiIcons.drawPin(context, bx.toFloat(), (y1 + 1).toFloat(), (bx + 9).toFloat(), (y1 + 11).toFloat(), btnHover, window.isPinned())
        }

        if (window.isMinimizable()) {
            bx -= 11
            val btnHover = hovering && mouseX >= bx && mouseX < bx + 9 && mouseY >= y1 + 2 && mouseY < y1 + 11
            ClickGuiIcons.drawMinimizeArrow(context, bx.toFloat(), (y1 + 1).toFloat(), (bx + 9).toFloat(), (y1 + 11).toFloat(), btnHover, window.isMinimized())
        }

        // Render children if not minimized
        if (!window.isMinimized()) {
            val scrollEnabled = window.isScrollingEnabled()
            val scrollOffset = window.getScrollOffset()

            // Draw children
            for (i in 0 until window.countChildren()) {
                val child = window.getChild(i)

                val cx1 = x1 + child.getX()
                val cy1 = y1 + 13 + child.getY() + (if (scrollEnabled) scrollOffset else 0)

                if (cy1 + child.getHeight() < y1 + 13 || cy1 > y2) continue // Clipped

                context.pose().pushMatrix()
                context.pose().translate(cx1.toFloat(), cy1.toFloat())

                child.render(context,
                    if (hovering) mouseX - cx1 else Int.MIN_VALUE,
                    if (hovering) mouseY - cy1 else Int.MIN_VALUE,
                    partialTicks)

                context.pose().popMatrix()
            }

            // Draw scrollbar
            if (scrollEnabled) {
                val outerHeight = window.getHeight() - 13
                val innerHeight = window.getInnerHeight()
                val maxScrollbarHeight = outerHeight - 2
                val scrollbarHeight = (maxScrollbarHeight * outerHeight / innerHeight)
                    .coerceAtLeast(1)

                val scrollbarY = (outerHeight * (-scrollOffset / innerHeight.toDouble()) + 1).toInt()
                    .coerceAtMost(outerHeight - scrollbarHeight - 1)

                val sx = x2 - 2
                context.fill(sx, y1 + 13 + scrollbarY, sx + 1, y1 + 13 + scrollbarY + scrollbarHeight, 0xFF888888.toInt())
            }
        }
    }

    fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        context.pose().pushMatrix()
        tooltip = ""

        for (window in windows) {
            if (window.isInvisible()) continue

            // dragging
            if (window.isDragging()) {
                if (leftMouseButtonPressed) {
                    window.dragTo(mouseX, mouseY)
                } else {
                    window.stopDragging()
                    saveWindows()
                }
            }

            // scrollbar dragging
            if (window.isDraggingScrollbar()) {
                if (leftMouseButtonPressed) {
                    window.dragScrollbarTo(mouseY)
                } else {
                    window.stopDraggingScrollbar()
                }
            }

            context.guiRenderState.up()
            renderWindow(context, window, mouseX, mouseY, partialTicks)
        }

        renderPopups(context, mouseX, mouseY)
        renderTooltip(context, mouseX, mouseY)

        context.pose().popMatrix()
    }

    fun renderPopups(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        closeInvalidPopups()

        for (popup in popups) {
            val owner = popup.getOwner()
            val parent = owner.getParent() ?: continue

            val x1 = parent.getX() + owner.getX()
            val y1 = parent.getY() + 13 + parent.getScrollOffset() + owner.getY()

            context.pose().pushMatrix()
            context.pose().translate(x1.toFloat(), y1.toFloat())
            context.guiRenderState.up()

            popup.render(context, mouseX - x1, mouseY - y1)

            context.pose().popMatrix()
        }
    }

    fun renderTooltip(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (tooltip.isEmpty()) return

        val lines = tooltip.split("\n")
        val font = mc.font

        var tw = 0
        val th = lines.size * font.lineHeight
        for (line in lines) {
            val lw = font.width(line)
            if (lw > tw) tw = lw
        }
        val sw = mc.window.guiScaledWidth
        val sh = mc.window.guiScaledHeight

        val xt1 = if (mouseX + tw + 11 <= sw) mouseX + 8 else mouseX - tw - 8
        val xt2 = xt1 + tw + 3
        val yt1 = if (mouseY + th - 2 <= sh) mouseY - 4 else mouseY - th - 4
        val yt2 = yt1 + th + 2

        context.guiRenderState.up()

        // background
        context.fill(xt1, yt1, xt2, yt2, toIntColor(bgColor, ttOpacity))

        // outline
        drawBorder2D(context, xt1.toFloat(), yt1.toFloat(), xt2.toFloat(), yt2.toFloat(),
            toIntColor(acColor, 0.5f))

        // text
        context.guiRenderState.up()
        for (i in lines.indices) {
            context.text(font, lines[i], xt1 + 2, yt1 + 2 + i * font.lineHeight, txtColor, false)
        }
    }

    fun renderPinnedWindows(context: GuiGraphicsExtractor, partialTicks: Float) {
        for (window in windows) {
            if (!window.isPinned() || window.isInvisible()) continue
            context.guiRenderState.up()
            renderWindow(context, window, Int.MIN_VALUE, Int.MIN_VALUE, partialTicks)
        }
    }

    fun setBgColor(color: FloatArray) { bgColor = color }
    fun setAcColor(color: FloatArray) { acColor = color }
    fun setTxtColor(color: Int) { txtColor = color }
    fun setOpacity(opacity: Float) { this.opacity = opacity }
    fun setTooltipOpacity(opacity: Float) { ttOpacity = opacity }
    fun setMaxHeight(height: Int) { maxHeight = height }
    fun setMaxSettingsHeight(height: Int) { maxSettingsHeight = height }

    fun getBgColor() = bgColor
    fun getAcColor() = acColor
    fun getTxtColor() = txtColor
    fun getOpacity() = opacity
    fun getTooltipOpacity() = ttOpacity
    fun getMaxHeight() = maxHeight
    fun getMaxSettingsHeight() = maxSettingsHeight
    fun isLeftMouseButtonPressed() = leftMouseButtonPressed

    fun addWindow(window: Window) { windows.add(window) }
    fun addPopup(popup: Popup) { popups.add(popup) }

    /**
     * Closes all closable windows (settings windows).
     * Used when the ModuleSearchScreen is closed to prevent ghost windows.
     */
    fun closeAllWindows() {
        windows.removeIf { it.isClosable() }
    }

    companion object {
        private val scale: Int get() = Minecraft.getInstance().window.guiScale

        fun toIntColor(rgb: FloatArray, opacity: Float): Int {
            val o = (opacity.coerceIn(0f, 1f) * 255).toInt() shl 24
            val r = (rgb[0].coerceIn(0f, 1f) * 255).toInt() shl 16
            val g = (rgb[1].coerceIn(0f, 1f) * 255).toInt() shl 8
            val b = (rgb[2].coerceIn(0f, 1f) * 255).toInt()
            return o or r or g or b
        }

        fun drawBorder2D(context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
            val s = scale
            val x = (x1 * s).toInt(); val y = (y1 * s).toInt()
            val w = ((x2 - x1) * s).toInt(); val h = ((y2 - y1) * s).toInt()

            context.pose().pushMatrix()
            context.pose().scale(1f / s)
            context.drawHorizontalLine(x.toFloat(), (x + w - 1).toFloat(), y.toFloat(), 1f, net.ccbluex.liquidbounce.render.engine.type.Color4b(color))
            context.drawHorizontalLine(x.toFloat(), (x + w - 1).toFloat(), (y + h - 1).toFloat(), 1f, net.ccbluex.liquidbounce.render.engine.type.Color4b(color))
            context.drawVerticalLine(x.toFloat(), (y + 1).toFloat(), (y + h - 2).toFloat(), 1f, net.ccbluex.liquidbounce.render.engine.type.Color4b(color))
            context.drawVerticalLine((x + w - 1).toFloat(), (y + 1).toFloat(), (y + h - 2).toFloat(), 1f, net.ccbluex.liquidbounce.render.engine.type.Color4b(color))
            context.pose().popMatrix()
        }

        fun drawLine2D(context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
            val s = scale
            val x = x1 * s; val y = y1 * s
            val w = (x2 - x1) * s; val h = (y2 - y1) * s
            val length = sqrt((w * w + h * h).toDouble()).toInt()

            context.pose().pushMatrix()
            context.pose().scale(1f / s)
            context.pose().translate(x, y)
            context.drawHorizontalLine(0f, (length - 1).toFloat(), 0f, 1f, net.ccbluex.liquidbounce.render.engine.type.Color4b(color))
            context.pose().popMatrix()
        }

        fun fill2D(context: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
            val s = scale
            context.pose().pushMatrix()
            context.pose().scale(1f / s)
            context.fill((x1 * s).toInt(), (y1 * s).toInt(), (x2 * s).toInt(), (y2 * s).toInt(), color)
            context.pose().popMatrix()
        }
    }
}
