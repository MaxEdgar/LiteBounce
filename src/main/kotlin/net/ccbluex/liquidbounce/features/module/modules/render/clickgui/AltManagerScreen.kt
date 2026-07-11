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

import net.ccbluex.liquidbounce.features.account.AccountManager
import net.ccbluex.liquidbounce.features.account.AccountService
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * Alt Manager screen for managing Minecraft accounts.
 * Supports cracked, session token, Microsoft, and TheAltening accounts.
 */
@Suppress("TooManyFunctions", "LongMethod", "CognitiveComplexMethod")
class AltManagerScreen : Screen(Component.literal("")) {

    private var scrollOffset = 0
    private var editingInput: InputField? = null
    private var statusMessage: String? = null
    private var statusTimer = 0

    private enum class InputField {
        CRACKED_USERNAME,
        SESSION_TOKEN,
        ALTENING_TOKEN
    }

    private val inputs = mutableMapOf<InputField, String>()
    private var deleteConfirmId: Int? = null

    private var inputSectionY = 0

    override fun isPauseScreen() = false

    override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        // No background blur
    }

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sw = width
        val sh = height
        val font = mc.font

        context.fill(0, 0, sw, sh, 0xCC0D0D1A.toInt())

        var y = 8

        // Title bar
        context.text(font, "Alt Manager", 8, y, 0xFF66FF66.toInt(), false)
        val restoreX = sw - font.width("Restore Initial") - 8
        val restoreHover = mouseX in restoreX..<restoreX + font.width("Restore Initial") + 4 &&
            mouseY in y..<y + font.lineHeight + 2
        context.fill(restoreX - 2, y, restoreX + font.width("Restore Initial") + 2, y + font.lineHeight + 2,
            if (restoreHover) 0xFF333344.toInt() else 0xFF222233.toInt())
        context.text(font, "Restore Initial", restoreX, y + 1,
            if (restoreHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt(), false)

        y += font.lineHeight + 4

        // Current session
        val user = mc.user
        val nameColor = if (user.name == "Player") 0xFF888888.toInt() else 0xFFF0F0F0.toInt()
        context.text(font, "Current: ${user.name}", 8, y, nameColor, false)
        y += font.lineHeight + 4

        // Separator
        context.fill(8, y, sw - 8, y + 1, 0xFF333344.toInt())
        y += 6

        // Account list
        val listStartY = y
        val inputAreaH = 115
        val listEndY = sh - inputAreaH

        var currentY = listStartY + scrollOffset
        val accounts = AccountManager.accounts.toList()
        val itemH = 16
        val listW = sw / 2 + 80
        val listX = sw / 2 - listW / 2

        if (accounts.isEmpty()) {
            context.text(font, "No accounts saved. Add one below.",
                sw / 2 - font.width("No accounts saved. Add one below.") / 2,
                listStartY + 8, 0xFF666666.toInt(), false)
        }

        for ((index, account) in accounts.withIndex()) {
            val itemY = currentY
            if (itemY + itemH >= listStartY - 2 && itemY < listEndY) {
                val hovering = mouseX >= listX && mouseY >= itemY && mouseX < listX + listW && mouseY < itemY + itemH
                val profile = account.profile
                val username = profile?.username ?: "???"
                val isFavorite = safeIsFavorite(account)
                val service = AccountService.getService(account)
                val isLoggedIn = user.name.equals(username, ignoreCase = true)

                val bgColor = when {
                    isLoggedIn -> 0xFF2A3A2A.toInt()
                    hovering -> 0xFF222233.toInt()
                    else -> 0xFF151520.toInt()
                }
                context.fill(listX, itemY, listX + listW, itemY + itemH, bgColor)

                var cx = listX + 4

                val starText = if (isFavorite) "[*]" else "[ ]"
                context.text(font, starText, cx, itemY + 3,
                    if (isFavorite) 0xFFFFAA00.toInt() else 0xFF555555.toInt(), false)
                cx += 16

                val typeColor = when (service) {
                    AccountService.MICROSOFT -> 0xFF44AA44.toInt()
                    AccountService.SESSION -> 0xFF44AAAA.toInt()
                    AccountService.THEALTENING -> 0xFFAA44AA.toInt()
                    AccountService.CRACKED -> 0xFFAAAA44.toInt()
                }
                context.text(font, service.tag.take(3), cx, itemY + 3, typeColor, false)
                cx += 16

                context.text(font, username, cx, itemY + 3,
                    if (isLoggedIn) 0xFF66FF66.toInt() else 0xFFF0F0F0.toInt(), false)

                val delW = font.width("Del") + 4
                val delX = listX + listW - delW - 2
                context.text(font, if (deleteConfirmId == index) "Confirm" else "Del",
                    delX, itemY + 3, 0xFF888888.toInt(), false)

                val logW = font.width("Login") + 4
                val logX = delX - logW - 4
                val logColor = if (isLoggedIn) 0xFF555555.toInt() else 0xFF888888.toInt()
                context.text(font, "Login", logX, itemY + 3, logColor, false)
            }
            currentY += itemH + 1
        }

        // Scrollbar
        val contentH = accounts.size * (itemH + 1)
        val listH = listEndY - listStartY
        val maxScroll = (contentH - listH).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(-maxScroll, 0)

        if (maxScroll > 0) {
            val scrollBarX = listX + listW + 2
            val scrollBarW = 3
            val thumbH = (listH.toFloat() / contentH.toFloat() * listH).toInt().coerceAtLeast(12)
            val thumbY = listStartY + ((-scrollOffset).toFloat() / maxScroll * (listH - thumbH)).toInt()
            context.fill(scrollBarX, listStartY, scrollBarX + scrollBarW, listEndY, 0xFF1A1A33.toInt())
            context.fill(scrollBarX, thumbY, scrollBarX + scrollBarW, thumbY + thumbH, 0xFF333377.toInt())
        }

        // Bottom input section
        inputSectionY = listEndY.coerceAtLeast(listStartY + 4)
        y = inputSectionY

        context.fill(8, y - 2, sw - 8, y - 1, 0xFF333344.toInt())
        y += 4

        context.text(font, "Cracked", 8, y, 0xFFAAAA44.toInt(), false)
        y += 10
        val crackedVal = inputs[InputField.CRACKED_USERNAME] ?: ""
        val showCrackedPlaceholder = editingInput != InputField.CRACKED_USERNAME && crackedVal.isEmpty()
        val crackedInputY = y
        context.fill(8, crackedInputY, 8 + 130, crackedInputY + 12,
            if (editingInput == InputField.CRACKED_USERNAME) 0xFF333355.toInt() else 0xFF222233.toInt())
        context.text(font, if (showCrackedPlaceholder) "Username..." else crackedVal, 10, crackedInputY + 2,
            if (showCrackedPlaceholder) 0xFF555555.toInt() else 0xFFF0F0F0.toInt(), false)

        val addCrackedX = 8 + 130 + 4
        context.fill(addCrackedX, crackedInputY, addCrackedX + 28, crackedInputY + 12, 0xFF333344.toInt())
        context.text(font, "Add", addCrackedX + 4, crackedInputY + 2, 0xFFAAAAAA.toInt(), false)

        y += 16
        context.text(font, "Session Token", 8, y, 0xFF44AAAA.toInt(), false)
        y += 10
        val sessionVal = inputs[InputField.SESSION_TOKEN] ?: ""
        val showSessionPlaceholder = editingInput != InputField.SESSION_TOKEN && sessionVal.isEmpty()
        val sessionInputY = y
        context.fill(8, sessionInputY, 8 + 240, sessionInputY + 12,
            if (editingInput == InputField.SESSION_TOKEN) 0xFF333355.toInt() else 0xFF222233.toInt())
        context.text(font, if (showSessionPlaceholder) "Paste token here..." else sessionVal, 10, sessionInputY + 2,
            if (showSessionPlaceholder) 0xFF555555.toInt() else 0xFFF0F0F0.toInt(), false)

        val addSessionX = 8 + 240 + 4
        context.fill(addSessionX, sessionInputY, addSessionX + 28, sessionInputY + 12, 0xFF333344.toInt())
        context.text(font, "Add", addSessionX + 4, sessionInputY + 2, 0xFFAAAAAA.toInt(), false)

        y += 16
        context.fill(8, y, 8 + 80, y + 12, 0xFF333344.toInt())
        context.text(font, "Microsoft", 12, y + 2, 0xFFAAAAAA.toInt(), false)

        val altX = 8 + 88
        context.fill(altX, y, altX + 80, y + 12, 0xFF333344.toInt())
        context.text(font, "TheAltening", altX + 4, y + 2, 0xFFAAAAAA.toInt(), false)

        val altToken = inputs[InputField.ALTENING_TOKEN] ?: ""
        if (editingInput == InputField.ALTENING_TOKEN || altToken.isNotEmpty()) {
            val altTokenX = altX + 86
            context.fill(altTokenX, y, altTokenX + 180, y + 12, 0xFF222233.toInt())
            context.text(font, altToken, altTokenX + 2, y + 2, 0xFFF0F0F0.toInt(), false)

            val genX = altTokenX + 186
            context.fill(genX, y, genX + 46, y + 12, 0xFF333344.toInt())
            context.text(font, "Generate", genX + 2, y + 2, 0xFFAAAAAA.toInt(), false)
        }

        val status = statusMessage
        if (status != null && statusTimer > 0) {
            context.text(font, status, sw / 2 - font.width(status) / 2, sh - 16, 0xFF66FF66.toInt(), false)
            statusTimer--
        } else {
            statusMessage = null
        }
    }

    private fun safeIsFavorite(account: Any): Boolean {
        return try {
            val method = account::class.java.getMethod("isFavorite")
            method.invoke(account) as Boolean
        } catch (_: Exception) {
            false
        }
    }

    private fun checkRestoreClick(mx: Int, my: Int): Boolean {
        val font = mc.font
        val restoreX = width - font.width("Restore Initial") - 8
        if (mx in restoreX..<restoreX + font.width("Restore Initial") + 4 &&
            my in 8..<8 + font.lineHeight + 2) {
            AccountManager.restoreInitial()
            statusMessage = "Restored initial session"
            statusTimer = 100
            return true
        }
        return false
    }

    private fun handleAccountAction(index: Int, account: Any, mx: Int, listX: Int, listW: Int): Boolean {
        val font = mc.font
        if (mx < listX + 20) {
            if (safeIsFavorite(account)) {
                AccountManager.unfavoriteAccount(index)
            } else {
                AccountManager.favoriteAccount(index)
            }
            return true
        }
        val delW = font.width("Del") + 4
        val delX = listX + listW - delW - 2
        val logW = font.width("Login") + 4
        val logX = delX - logW - 4
        if (mx >= logX && mx < logX + logW) {
            AccountManager.loginAccount(index)
            statusMessage = "Logging in..."
            statusTimer = 100
            return true
        }
        if (mx >= delX && mx < delX + delW) {
            if (deleteConfirmId == index) {
                AccountManager.removeAccount(index)
                deleteConfirmId = null
                statusMessage = "Removed account"
                statusTimer = 100
            } else {
                deleteConfirmId = index
            }
            return true
        }
        return false
    }

    private fun checkAccountListClick(mx: Int, my: Int): Boolean {
        val accounts = AccountManager.accounts.toList()
        val listW = width / 2 + 80
        val listX = width / 2 - listW / 2
        val listStartY = 8 + mc.font.lineHeight + 4 + 6 + 6

        for ((index, account) in accounts.withIndex()) {
            val itemY = listStartY + scrollOffset + index * 17
            if (my in itemY..<itemY + 16 && mx in listX..<listX + listW) {
                deleteConfirmId = null
                return handleAccountAction(index, account, mx, listX, listW)
            }
        }
        return false
    }

    private fun handleAlteningTokenClick(mx: Int): Boolean {
        val altToken = inputs[InputField.ALTENING_TOKEN] ?: ""
        if (editingInput != InputField.ALTENING_TOKEN && altToken.isEmpty()) return false
        val altX = 8 + 88
        val altTokenX = altX + 86
        if (mx in altTokenX..<altTokenX + 180) {
            editingInput = InputField.ALTENING_TOKEN
            return true
        }
        if (mx in altTokenX + 186..<altTokenX + 186 + 46) {
            generateAltening()
            return true
        }
        return false
    }

    private fun checkInputSectionClick(mx: Int, my: Int): Boolean {
        if (my < inputSectionY) return false
        val yOff = my - inputSectionY
        var handled = false

        if (yOff in 14..<14 + 12) {
            handled = true
            if (mx in 8..<8 + 130) {
                editingInput = InputField.CRACKED_USERNAME
            } else if (mx in 142..<170) {
                addCrackedAccount()
            } else {
                handled = false
            }
        }

        if (!handled && yOff in 40..<40 + 12) {
            handled = true
            if (mx in 8..<8 + 240) {
                editingInput = InputField.SESSION_TOKEN
            } else if (mx in 252..<280) {
                addSessionAccount()
            } else {
                handled = false
            }
        }

        if (!handled && yOff in 66..<66 + 12) {
            handled = true
            if (mx in 8..<8 + 80) {
                addMicrosoftAccount()
            } else if (mx in 96..<176) {
                editingInput = InputField.ALTENING_TOKEN
            } else if (handleAlteningTokenClick(mx)) {
                // handled by helper
            } else {
                handled = false
            }
        }

        return handled
    }

    override fun mouseClicked(context: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mx = context.x().toInt()
        val my = context.y().toInt()
        val button = context.button()
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(context, doubleClick)
        }
        var handled = checkRestoreClick(mx, my)
        if (!handled) {
            handled = checkAccountListClick(mx, my)
        }
        if (!handled) {
            handled = checkInputSectionClick(mx, my)
        }
        if (!handled) {
            editingInput = null
        }
        return handled || super.mouseClicked(context, doubleClick)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val codePoint = event.codepoint()
        if (!Character.isISOControl(codePoint)) {
            val field = editingInput ?: return false
            val current = inputs[field] ?: ""
            if (current.length < 80) {
                inputs[field] = current + codePoint.toChar()
            }
            return true
        }
        return super.charTyped(event)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (input.key == GLFW.GLFW_KEY_ESCAPE) {
            editingInput = null
            deleteConfirmId = null
            onClose()
            return true
        }
        if (input.key == GLFW.GLFW_KEY_BACKSPACE) {
            val field = editingInput ?: return super.keyPressed(input)
            val current = inputs[field] ?: ""
            if (current.isNotEmpty()) {
                inputs[field] = current.dropLast(1)
            }
            return true
        }
        if (input.key == GLFW.GLFW_KEY_ENTER || input.key == GLFW.GLFW_KEY_KP_ENTER) {
            when (editingInput) {
                InputField.CRACKED_USERNAME -> addCrackedAccount()
                InputField.SESSION_TOKEN -> addSessionAccount()
                InputField.ALTENING_TOKEN -> generateAltening()
                null -> {}
            }
            return true
        }
        return super.keyPressed(input)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        scrollOffset += (verticalAmount * 15).toInt()
        scrollOffset = scrollOffset.coerceAtMost(0)
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    private fun addCrackedAccount() {
        val username = inputs[InputField.CRACKED_USERNAME]?.trim() ?: ""
        if (username.isEmpty()) {
            statusMessage = "Username is empty!"
            statusTimer = 100
            return
        }
        AccountManager.newCrackedAccount(username)
        inputs[InputField.CRACKED_USERNAME] = ""
        editingInput = null
        statusMessage = "Added cracked account: $username"
        statusTimer = 100
    }

    private fun addSessionAccount() {
        val token = inputs[InputField.SESSION_TOKEN]?.trim() ?: ""
        if (token.isEmpty()) {
            statusMessage = "Token is empty!"
            statusTimer = 100
            return
        }
        AccountManager.newSessionAccount(token)
        inputs[InputField.SESSION_TOKEN] = ""
        editingInput = null
        statusMessage = "Added session account"
        statusTimer = 100
    }

    private fun addMicrosoftAccount() {
        AccountManager.newMicrosoftAccount { _ -> }
        statusMessage = "Starting Microsoft authentication..."
        statusTimer = 100
    }

    private fun generateAltening() {
        val token = inputs[InputField.ALTENING_TOKEN]?.trim() ?: ""
        if (token.isEmpty()) {
            statusMessage = "Altening token is empty!"
            statusTimer = 100
            return
        }
        AccountManager.generateAlteningAccount(token)
        editingInput = null
        statusMessage = "Generated TheAltening account"
        statusTimer = 100
    }

}
