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

package net.maxedgar.coffee.features.global

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.SuspendHandlerBehavior.CancelPrevious
import net.maxedgar.coffee.event.eventListenerScope
import net.maxedgar.coffee.event.events.ClientChatJwtTokenEvent
import net.maxedgar.coffee.event.events.ClientChatMessageEvent
import net.maxedgar.coffee.event.events.ClientChatStateChange
import net.maxedgar.coffee.event.events.ClientShutdownEvent
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.SessionEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.suspendHandler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.chat.AxochatClient
import net.maxedgar.coffee.features.chat.packet.C2SRequestJWTPacket
import net.maxedgar.coffee.features.command.CommandManager
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.misc.HideAppearance.isDestructed
import net.maxedgar.coffee.lang.translation
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.text.asText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.text.plus
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.text.textOf
import net.maxedgar.coffee.utils.client.withColor
import net.maxedgar.coffee.utils.kotlin.optional
import net.maxedgar.coffee.utils.text.PlainText
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.ObjectContents
import net.minecraft.network.chat.contents.objects.PlayerSprite
import net.minecraft.world.item.component.ResolvableProfile
import kotlin.time.Duration.Companion.seconds

object GlobalSettingsClientChat : ToggleableValueGroup(
    name = "ClientChat",
    enabled = true,
    aliases = listOf("GlobalChat", "IRC")
) {

    private var jwtToken by text("JwtToken", "")

    private val autoTranslate by multiEnumChoice<ClientChatMessageEvent.ChatGroup>("AutoTranslate")

    private val chatClient = AxochatClient()
    private val prefix: Component = "".asText()
        .withStyle(ChatFormatting.RESET).withStyle(ChatFormatting.GRAY)
        .append(this.name.asPlainText(ChatFormatting.BLUE))
        .withStyle(ChatFormatting.BOLD)
        .append(" ▸ ".asText().withStyle(ChatFormatting.RESET).withColor(ChatFormatting.DARK_GRAY))
    private val exceptionData = MessageMetadata(prefix = false, id = "LiquidChat#exception")
    private val messageData = MessageMetadata(prefix = false)

    private fun createChatWriteCommand() = CommandBuilder
        .begin("chat")
        .parameter(
            ParameterBuilder
                .begin<String>("message")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .required()
                .vararg()
                .build()
        )
        .handler {
            if (!chatClient.isConnected) {
                chat(
                    prefix, translation("liquidbounce.liquidchat.notConnected").withStyle(ChatFormatting.GRAY),
                    metadata = exceptionData
                )
                return@handler
            }

            if (!chatClient.isLoggedIn) {
                chat(
                    prefix, translation("liquidbounce.liquidchat.notLoggedIn").withStyle(ChatFormatting.GRAY),
                    metadata = exceptionData
                )
                return@handler
            }

            chatClient.sendMessage((args[0] as Array<*>).joinToString(" ") { it as String })
        }
        .build()

    private fun createChatJwtCommand() = CommandBuilder
        .begin("chatjwt")
        .handler {
            if (!chatClient.isConnected) {
                chat(
                    prefix, translation("liquidbounce.liquidchat.notConnected").withStyle(ChatFormatting.GRAY),
                    metadata = exceptionData
                )
                return@handler
            }

            chatClient.sendPacket(C2SRequestJWTPacket())
            chat(
                prefix, translation("liquidbounce.liquidchat.jwtTokenRequested").withStyle(ChatFormatting.GRAY),
                metadata = exceptionData
            )
        }
        .build()

    init {
        CommandManager.addCommand(createChatWriteCommand())
        CommandManager.addCommand(createChatJwtCommand())
    }

    override fun onEnabled() {
        eventListenerScope.launch {
            chatClient.connect()
        }
    }

    override fun onDisabled() {
        chatClient.disconnect()
    }

    @Suppress("unused")
    private val shutdownHandler = handler<ClientShutdownEvent> {
        chatClient.disconnect()
    }

    @Suppress("unused")
    private val repeatable = tickHandler(Dispatchers.IO) {
        if (!chatClient.isConnected) {
            chatClient.connect()
        } else {
            // Wait 5 seconds before retrying
            delay(5.seconds)
        }
    }

    @Suppress("unused")
    private val sessionChange = suspendHandler<SessionEvent>(behavior = CancelPrevious) {
        chatClient.reconnect()
    }

    @Suppress("unused")
    private val handleChatMessage = suspendHandler<ClientChatMessageEvent> { event ->
        val resolvableProfile = ResolvableProfile.createUnresolved(event.user.uuid)
        withTimeoutOrNull(5.seconds) {
            resolvableProfile.resolveProfile(mc.services().profileResolver).await()
        }

        val playerSpritePart = MutableComponent.create(
            ObjectContents(PlayerSprite(resolvableProfile, false), optional())
        ).copyable(copyContent = event.user.uuid.toString())

        fun namePart(formatting: ChatFormatting) =
            event.user.name.asPlainText(
                Style.EMPTY + formatting +
                    ClickEvent.CopyToClipboard(event.user.name) +
                    HoverEvent.ShowText(event.user.name.asPlainText())
            )

        val prefix = when (event.chatGroup) {
            ClientChatMessageEvent.ChatGroup.PUBLIC_CHAT ->
                textOf(
                    playerSpritePart,
                    PlainText.SPACE,
                    namePart(ChatFormatting.GRAY),
                    " ▸ ".asPlainText(ChatFormatting.DARK_GRAY),
                )
            ClientChatMessageEvent.ChatGroup.PRIVATE_CHAT ->
                textOf(
                    "[".asPlainText(ChatFormatting.DARK_GRAY),
                    playerSpritePart,
                    PlainText.SPACE,
                    namePart(ChatFormatting.BLUE),
                    "] ".asPlainText(ChatFormatting.DARK_GRAY),
                )
        }

        writeChat(prefix, regular(event.message).copyable(copyContent = event.message))

        if (event.chatGroup !in autoTranslate) {
            return@suspendHandler
        }

        val result = GlobalSettingsAutoTranslate.translate(text = event.message)
        if (result.isValid) {
            writeChat(prefix, result.toResultText())
        }
    }

    @Suppress("unused")
    private val handleIncomingJwtToken = suspendHandler<ClientChatJwtTokenEvent>(behavior = CancelPrevious) { event ->
        jwtToken = event.jwt
        chatClient.reconnect()
    }

    @Suppress("unused")
    private val handleStateChange = handler<ClientChatStateChange> {
        when (it.state) {
            ClientChatStateChange.State.CONNECTED -> {
                notification(
                    "LiquidChat",
                    translation("liquidbounce.liquidchat.states.connected"),
                    NotificationEvent.Severity.INFO
                )

                // When the token is not empty, we can try to login via JWT
                if (jwtToken.isNotEmpty()) {
                    logger.info("Logging in via JWT...")
                    chatClient.loginViaJwt(jwtToken)
                } else {
                    logger.info("Requesting to login into Mojang...")
                    chatClient.requestMojangLogin()
                }
            }
            ClientChatStateChange.State.LOGGED_IN -> {
                notification(
                    "LiquidChat",
                    translation("liquidbounce.liquidchat.states.loggedIn"),
                    NotificationEvent.Severity.INFO
                )
            }
            ClientChatStateChange.State.DISCONNECTED -> {
                notification(
                    "LiquidChat",
                    translation("liquidbounce.liquidchat.states.disconnected"),
                    NotificationEvent.Severity.INFO
                )
            }
            ClientChatStateChange.State.AUTHENTICATION_FAILED -> {
                notification(
                    "LiquidChat",
                    translation("liquidbounce.liquidchat.authenticationFailed"),
                    NotificationEvent.Severity.ERROR
                )
                logger.warn("Failed authentication to LiquidChat")
            }

            else -> {} // do not bother
        }
    }

    private fun writeChat(playerPrefix: Component, message: Component) {
        if (!inGame) {
            logger.info("[Chat] ${playerPrefix.string} ${message.string}")
        } else {
            chat(prefix, playerPrefix, message, metadata = messageData)
        }
    }

    /**
     * Overwrites the condition requirement for being in-game
     */
    override val running
        get() = !isDestructed && enabled

}
