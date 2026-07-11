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

    /** Y-offset where the bottom input section starts (computed during render) */
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

        // ── Title bar ──
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
        val nameColor = if (mc.user.name == "Player") 0xFF888888.toInt() else 0xFFF0F0F0.toInt()
        context.text(font, "Current: $e${user.name}$r ($7${getSessionType()}$r)", 8, y, nameColor, false)
        y += font.lineHeight + 4

        // Separator
        context.fill(8, y, sw - 8, y + 1, 0xFF333344.toInt())
        y += 6

        // ── Account list ──
        val listStartY = y
        val inputAreaH = 115
        val listEndY = sh - inputAreaH
        val listH = listEndY - listStartY

        var currentY = listStartY + scrollOffset
        val accounts = AccountManager.accounts.toList()
        val itemH = 16
        val listW = sw / 2 + 80
        val listX = sw / 2 - listW / 2

        // Clip list area
        context.guiRenderState.enableScissor(0, listStartY, sw, (listEndY - listStartY).coerceAtLeast(0))

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
                val isLoggedIn = user.name.equals(username, ignoreCase = true) &&
                    (profile?.uuid?.let { it == user.profileId } == true)

                // Row background
                val bgColor = when {
                    isLoggedIn -> 0xFF2A3A2A.toInt()
                    hovering -> 0xFF222233.toInt()
                    else -> 0xFF151520.toInt()
                }
                context.fill(listX, itemY, listX + listW, itemY + itemH, bgColor)

                var cx = listX + 4

                // Favorite star
                val starText = if (isFavorite) "[*]" else "[ ]"
                val starHover = mouseX in cx..<cx + 14 && mouseY in itemY..<itemY + itemH
                context.text(font, starText, cx, itemY + 3,
                    if (starHover) 0xFFFFDD00.toInt() else if (isFavorite) 0xFFFFAA00.toInt() else 0xFF555555.toInt(), false)
                cx += 16

                // Account type badge (first 3 chars of type)
                val typeColor = when (service) {
                    AccountService.MICROSOFT -> 0xFF44AA44.toInt()
                    AccountService.SESSION -> 0xFF44AAAA.toInt()
                    AccountService.THEALTENING -> 0xFFAA44AA.toInt()
                    AccountService.CRACKED -> 0xFFAAAA44.toInt()
                }
                context.text(font, service.tag.take(3), cx, itemY + 3, typeColor, false)
                cx += 16

                // Username
                context.text(font, username, cx, itemY + 3,
                    if (isLoggedIn) 0xFF66FF66.toInt() else 0xFFF0F0F0.toInt(), false)

                // Right side: buttons
                // Delete
                val delW = font.width("Del") + 4
                val delX = listX + listW - delW - 2
                val delHover = mouseX in delX..<delX + delW && mouseY in itemY..<itemY + itemH
                val delColor = if (deleteConfirmId == index) 0xFFFF5555.toInt() else 0xFF888888.toInt()
                context.text(font, if (deleteConfirmId == index) "Confirm" else "Del",
                    delX, itemY + 3, if (delHover) 0xFFFF5555.toInt() else delColor, false)

                // Login
                val logW = font.width("Login") + 4
                val logX = delX - logW - 4
                val logHover = mouseX in logX..<logX + logW && mouseY in itemY..<itemY + itemH
                val logColor = if (isLoggedIn) 0xFF555555.toInt() else if (logHover) 0xFFFFAA00.toInt() else 0xFF888888.toInt()
                context.text(font, "Login", logX, itemY + 3, logColor, false)
            }
            currentY += itemH + 1
        }

        context.guiRenderState.disableScissor()

        // Scrollbar
        val contentH = accounts.size * (itemH + 1)
        val maxScroll = (contentH - listH).coerceAtLeast(0)
        scrollOffset = scrollOffset.coerceIn(-maxScroll, 0)

        if (maxScroll > 0) {
            val scrollBarX = listX + listW + 2
            val scrollBarW = 3
            val thumbH = (listH.toFloat() / contentH.toFloat() * listH).toInt().coerceAtLeast(12)
            val thumbY = listStartY + ((-scrollOffset).toFloat() / maxScroll * (listH - thumbH)).toInt()
            context.fill(scrollBarX, listStartY, scrollBarX + scrollBarW, listEndY, 0xFF1A1A33.toInt())
            val thumbHover = mouseX in scrollBarX..<scrollBarX + scrollBarW && mouseY in thumbY..<thumbY + thumbH
            context.fill(scrollBarX, thumbY, scrollBarX + scrollBarW, thumbY + thumbH,
                if (thumbHover) 0xFF5555AA.toInt() else 0xFF333377.toInt())
        }

        // ── Bottom input section ──
        inputSectionY = listEndY.coerceAtLeast(listStartY + 4)
        y = inputSectionY

        context.fill(8, y - 2, sw - 8, y - 1, 0xFF333344.toInt())
        y += 4

        // Cracked
        context.text(font, "Cracked", 8, y, 0xFFAAAA44.toInt(), false)
        y += 10
        val crackedVal = inputs[InputField.CRACKED_USERNAME] ?: ""
        val showCrackedPlaceholder = editingInput != InputField.CRACKED_USERNAME && crackedVal.isEmpty()
        val displayCracked = if (showCrackedPlaceholder) "Username..." else crackedVal
        val crackedInputX = 8
        val crackedInputY = y
        val crackedInputW = 130
        val crackedFocused = editingInput == InputField.CRACKED_USERNAME
        context.fill(crackedInputX, crackedInputY, crackedInputX + crackedInputW, crackedInputY + 12,
            if (crackedFocused) 0xFF333355.toInt() else 0xFF222233.toInt())
        context.text(font, displayCracked, crackedInputX + 2, crackedInputY + 2,
            if (showCrackedPlaceholder) 0xFF555555.toInt() else 0xFFF0F0F0.toInt(), false)

        // Add cracked button
        val addCrackedX = crackedInputX + crackedInputW + 4
        val addCrackedHover = mouseX in addCrackedX..<addCrackedX + 28 && mouseY in crackedInputY..<crackedInputY + 12
        context.fill(addCrackedX, crackedInputY, addCrackedX + 28, crackedInputY + 12,
            if (addCrackedHover) 0xFF444466.toInt() else 0xFF333344.toInt())
        context.text(font, "Add", addCrackedX + 4, crackedInputY + 2,
            if (addCrackedHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)

        y += 16

        // Session / Microsoft token
        context.text(font, "Session Token", 8, y, 0xFF44AAAA.toInt(), false)
        y += 10
        val sessionVal = inputs[InputField.SESSION_TOKEN] ?: ""
        val showSessionPlaceholder = editingInput != InputField.SESSION_TOKEN && sessionVal.isEmpty()
        val displaySession = if (showSessionPlaceholder) "Paste token here..." else sessionVal
        val sessionInputX = 8
        val sessionInputY = y
        val sessionInputW = 240
        val sessionFocused = editingInput == InputField.SESSION_TOKEN
        context.fill(sessionInputX, sessionInputY, sessionInputX + sessionInputW, sessionInputY + 12,
            if (sessionFocused) 0xFF333355.toInt() else 0xFF222233.toInt())
        context.text(font, displaySession, sessionInputX + 2, sessionInputY + 2,
            if (showSessionPlaceholder) 0xFF555555.toInt() else 0xFFF0F0F0.toInt(), false)

        // Add session button
        val addSessionX = sessionInputX + sessionInputW + 4
        val addSessionHover = mouseX in addSessionX..<addSessionX + 28 && mouseY in sessionInputY..<sessionInputY + 12
        context.fill(addSessionX, sessionInputY, addSessionX + 28, sessionInputY + 12,
            if (addSessionHover) 0xFF444466.toInt() else 0xFF333344.toInt())
        context.text(font, "Add", addSessionX + 4, sessionInputY + 2,
            if (addSessionHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)

        y += 16

        // Microsoft button
        val msHover = mouseX in 8..<8 + 80 && mouseY in y..<y + 12
        context.fill(8, y, 8 + 80, y + 12, if (msHover) 0xFF444466.toInt() else 0xFF333344.toInt())
        context.text(font, "Microsoft", 12, y + 2,
            if (msHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)

        // TheAltening button
        val altX = 8 + 88
        val altHover = mouseX in altX..<altX + 80 && mouseY in y..<y + 12
        context.fill(altX, y, altX + 80, y + 12, if (altHover) 0xFF444466.toInt() else 0xFF333344.toInt())
        context.text(font, "TheAltening", altX + 4, y + 2,
            if (altHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)

        // Altening token input (shown when token is being entered or Altening is focused)
        val altToken = inputs[InputField.ALTENING_TOKEN] ?: ""
        if (editingInput == InputField.ALTENING_TOKEN || altToken.isNotEmpty()) {
            val altTokenX = altX + 86
            context.fill(altTokenX, y, altTokenX + 180, y + 12,
                if (editingInput == InputField.ALTENING_TOKEN) 0xFF333355.toInt() else 0xFF222233.toInt())
            val showAltPlaceholder = editingInput != InputField.ALTENING_TOKEN && altToken.isEmpty()
            val displayAlt = if (showAltPlaceholder) "API token..." else altToken
            context.text(font, displayAlt, altTokenX + 2, y + 2, 0xFFF0F0F0.toInt(), false)

            // Generate button
            val genX = altTokenX + 186
            val genHover = mouseX in genX..<genX + 46 && mouseY in y..<y + 12
            context.fill(genX, y, genX + 46, y + 12,
                if (genHover) 0xFF444466.toInt() else 0xFF333344.toInt())
            context.text(font, "Generate", genX + 2, y + 2,
                if (genHover) 0xFFFFAA00.toInt() else 0xFFAAAAAA.toInt(), false)
        }

        // Status message
        val status = statusMessage
        if (status != null && statusTimer > 0) {
            val statusColor = if (status.startsWith("$c") || status.startsWith("$4")) 0xFFFF5555.toInt() else 0xFF66FF66.toInt()
            context.text(font, status.replace("$a", "").replace("$c", "").replace("$e", ""),
                sw / 2 - font.width(status.replace(Regex("$[0-9a-fklmnor]"), "")) / 2,
                sh - 16, statusColor, false)
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

    private fun getSessionType(): String {
        val user = mc.user
        return when (user.type.ordinal) {
            0 -> "Mojang"
            1 -> "MSA"
            else -> "Unknown"
        }
    }

    override fun mouseClicked(context: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mx = context.x().toInt()
        val my = context.y().toInt()
        val button = context.button()
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(context, doubleClick)
        }

        val font = mc.font
        val accounts = AccountManager.accounts.toList()
        val listW = width / 2 + 80
        val listX = width / 2 - listW / 2
        val font2 = mc.font
        val listStartY = 8 + font2.lineHeight + 4 + 6 + 6
        val inputAreaH = 115
        val listEndY = height - inputAreaH

        // ── Check restore button ──
        val restoreX = width - font.width("Restore Initial") - 8
        if (mx in restoreX..<restoreX + font.width("Restore Initial") + 4 &&
            my in 8..<8 + font2.lineHeight + 2) {
            AccountManager.restoreInitial()
            setStatus("$aRestored initial session")
            return true
        }

        // ── Check account list actions ──
        for ((index, account) in accounts.withIndex()) {
            val itemY = listStartY + scrollOffset + index * 17
            if (my in itemY..<itemY + 16 && mx in listX..<listX + listW) {

                // Star (favorite) - first 16px
                if (mx < listX + 20) {
                    if (safeIsFavorite(account)) {
                        AccountManager.unfavoriteAccount(index)
                    } else {
                        AccountManager.favoriteAccount(index)
                    }
                    return true
                }

                // Login button (right side)
                val delW = font.width("Del") + 4
                val delX = listX + listW - delW - 2
                val logW = font.width("Login") + 4
                val logX = delX - logW - 4

                if (mx >= logX && mx < logX + logW) {
                    AccountManager.loginAccount(index)
                    setStatus("$aLogging in as ${account.profile?.username ?: "???"}")
                    return true
                }

                if (mx >= delX && mx < delX + delW) {
                    if (deleteConfirmId == index) {
                        AccountManager.removeAccount(index)
                        deleteConfirmId = null
                        setStatus("$cRemoved account")
                    } else {
                        deleteConfirmId = index
                    }
                    return true
                }

                deleteConfirmId = null
            }
        }

        // ── Check bottom input section using inputSectionY ──
        if (my >= inputSectionY) {
            val yOff = my - inputSectionY

            // Cracked row
            if (yOff in 14..<14 + 12 && mx in 8..<8 + 130) {
                editingInput = InputField.CRACKED_USERNAME
                return true
            }
            if (yOff in 14..<14 + 12 && mx in 8 + 130 + 4..<8 + 130 + 4 + 28) {
                addCrackedAccount()
                return true
            }

            // Session row
            if (yOff in 40..<40 + 12 && mx in 8..<8 + 240) {
                editingInput = InputField.SESSION_TOKEN
                return true
            }
            if (yOff in 40..<40 + 12 && mx in 8 + 240 + 4..<8 + 240 + 4 + 28) {
                addSessionAccount()
                return true
            }

            // Microsoft / Altening row
            if (yOff in 66..<66 + 12) {
                if (mx in 8..<8 + 80) {
                    addMicrosoftAccount()
                    return true
                }
                val altX = 8 + 88
                if (mx in altX..<altX + 80) {
                    editingInput = InputField.ALTENING_TOKEN
                    return true
                }
                // Altening token + generate
                val altToken = inputs[InputField.ALTENING_TOKEN] ?: ""
                if (editingInput == InputField.ALTENING_TOKEN || altToken.isNotEmpty()) {
                    val altTokenX = altX + 86
                    if (mx in altTokenX..<altTokenX + 180) {
                        editingInput = InputField.ALTENING_TOKEN
                        return true
                    }
                    if (mx in altTokenX + 186..<altTokenX + 186 + 46) {
                        generateAltening()
                        return true
                    }
                }
            }
        }

        // Click outside inputs -> deselect
        editingInput = null
        return super.mouseClicked(context, doubleClick)
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
            setStatus("$cUsername is empty!")
            return
        }
        AccountManager.newCrackedAccount(username)
        inputs[InputField.CRACKED_USERNAME] = ""
        editingInput = null
        setStatus("$aAdded cracked account: $username")
    }

    private fun addSessionAccount() {
        val token = inputs[InputField.SESSION_TOKEN]?.trim() ?: ""
        if (token.isEmpty()) {
            setStatus("$cToken is empty!")
            return
        }
        AccountManager.newSessionAccount(token)
        inputs[InputField.SESSION_TOKEN] = ""
        editingInput = null
        setStatus("$aAdded session account")
    }

    private fun addMicrosoftAccount() {
        AccountManager.newMicrosoftAccount { _ -> }
        setStatus("$aStarting Microsoft authentication...")
    }

    private fun generateAltening() {
        val token = inputs[InputField.ALTENING_TOKEN]?.trim() ?: ""
        if (token.isEmpty()) {
            setStatus("$cAltening token is empty!")
            return
        }
        AccountManager.generateAlteningAccount(token)
        editingInput = null
        setStatus("$aGenerated TheAltening account")
    }

    private fun setStatus(msg: String) {
        statusMessage = msg
        statusTimer = 100
    }

}
