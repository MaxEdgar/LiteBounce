/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2025 MaxEdgar
 *
 * Coffee is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coffee is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coffee. If not, see <https://www.gnu.org/licenses/>.
 */

package net.maxedgar.coffee.features.module.modules.misc.betterchat

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.ChatReceiveEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.interfaces.GuiMessageAddition
import net.maxedgar.coffee.interfaces.GuiMessageLineAddition
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.text.asText
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.util.StringDecomposer

object AntiSpam : ToggleableValueGroup(ModuleBetterChat, "AntiSpam", true) {

    private val stack by boolean("StackMessages", false)
    private val regexFilters by regexList("Filters", linkedSetOf())

    @Suppress("unused", "CAST_NEVER_SUCCEEDS" /* succeed with mixins */)
    val chatHandler = handler<ChatReceiveEvent> { event ->
        val string = StringDecomposer.getPlainText(event.textData)

        if (regexFilters.isNotEmpty()) {
            val content = string.subSequence(string.indexOf('>') + 1, string.length).trim()

            val shouldBeRemoved = regexFilters.any {
                it.matches(content)
            }

            if (shouldBeRemoved) {
                event.cancelEvent()
                return@handler
            }
        }

        // stacks messages so that e.g., when a message is sent twice
        // it gets replaces by a new messages that has `[2]` appended
        if (stack && event.type != ChatReceiveEvent.ChatType.DISGUISED_CHAT_MESSAGE) {
            // always cancel so each message gets an ID
            event.cancelEvent()

            // appends "external" to every message id
            // so servers can't troll users with messages that
            // imitate client messages
            val id = "$string-external"

            val chatText = ArrayList<Component>()
            val text = event.applyChatDecoration.apply(event.textData)
            chatText += text

            val other = mc.gui.hud.chat.allMessages.find {
                (it as GuiMessageLineAddition).`liquid_bounce$getId`() == id
            }

            var count = 1
            other?.let {
                count += (other as GuiMessageAddition).`liquid_bounce$getCount`()
                chatText += " [$count]".asPlainText(ChatFormatting.GRAY)
            }

            val data = MessageMetadata(prefix = false, id = id, remove = true, count = count)
            chat(chatText.asText(), data)
        }
    }

}
