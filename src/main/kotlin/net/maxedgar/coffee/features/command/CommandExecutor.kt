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

package net.maxedgar.coffee.features.command

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.ChatSendEvent
import net.maxedgar.coffee.event.events.ClientShutdownEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.lang.translation
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.bold
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.highlight
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.onClick
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.removeMessage
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.maxedgar.coffee.utils.kotlin.MinecraftDispatcher
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import okio.appendingSink
import okio.buffer
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

/**
 * Links minecraft with the command engine
 */
object CommandExecutor : EventListener {

    private val commandHistoryFile = File(ConfigSystem.rootFolder, "command_history.txt")

    @Volatile
    private var isShuttingDown: Boolean = false

    /**
     * Add a wrapped suspend handler to [net.maxedgar.coffee.features.command.builder.CommandBuilder]
     * if you don't want to block the render thread.
     *
     * @param allowParallel allow or prevent duplicated executions
     * @author MukjepScarlet
     */
    fun CommandBuilder.suspendHandler(
        allowParallel: Boolean = false,
        handler: Command.Handler.Suspend,
    ) = if (allowParallel) {
        this.handler {
            commandCoroutineScope.launch(CoroutineName(command.name)) {
                with(handler) { this@handler() }
            }
        }
    } else {
        val running = AtomicBoolean(false)
        this.handler {
            if (!running.compareAndSet(false, true)) {
                chat(
                    markAsError(
                        translation("liquidbounce.commandManager.commandExecuting", command.name)
                    ),
                    command
                )
                return@handler
            }

            // Progress message job
            val progressMessageMetadata = MessageMetadata(id = "C${command.name}#progress", remove = true)
            val progressJob = commandCoroutineScope.launch(CoroutineName("${command.name} Progress")) {
                val startAt = System.currentTimeMillis()
                var n = 0
                val chars = charArrayOf('|', '/', '-', '\\')
                while (isActive) {
                    delay(0.25.seconds)
                    val duration = (System.currentTimeMillis() - startAt) / 1000
                    val char = chars[n % chars.size]
                    chat(
                        regular("<$char> Executing command "),
                        variable(command.name),
                        regular(" ("),
                        variable(duration.toString()),
                        regular("s)"),
                        metadata = progressMessageMetadata
                    )
                    n++
                }
            }

            // Handler job
            commandCoroutineScope.launch(CoroutineName(command.name)) {
                with(handler) { this@handler() }
            }.invokeOnCompletion {
                running.set(false)
                progressJob.cancel()
                mc.gui.hud.chat.removeMessage(progressMessageMetadata.id)
            }
        }
    }

    /**
     * Handling exceptions for suspend handlers
     */
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (isShuttingDown && throwable is CancellationException) {
            // Client shutdown, ignored
        } else {
            handleExceptions(throwable)
        }
    }

    /**
     * Render thread scope
     */
    private val commandCoroutineScope = CoroutineScope(
        MinecraftDispatcher + SupervisorJob() + coroutineExceptionHandler
    )

    internal fun handleExceptions(e: Throwable) {
        when (e) {
            is CommandException -> {
                mc.gui.hud.chat.removeMessage("CommandManager#error")
                val data = MessageMetadata(id = "CommandManager#error", remove = false)
                chat(e.text.withStyle(ChatFormatting.RED), metadata = data)

                if (e.usageInfo.isNotEmpty()) {
                    chat(highlight("Usage: ").bold(true), metadata = data)

                    // Zip the usage info together, e.g.
                    // ⬥ .friend add <name> [<alias>]
                    // ⬥ .friend remove <name>
                    for (usage in e.usageInfo) {
                        val prefix = CommandManager.GlobalSettings.prefix
                        val text = regular("")
                            .append("\u2B25 ".asPlainText(ChatFormatting.BLUE))
                            .append(regular(prefix))
                            .append(usage)
                            .onClick(ClickEvent.SuggestCommand(prefix + usage.string))

                        chat(text, metadata = data)
                    }
                }
            }
            else -> {
                chat(
                    markAsError(
                        translation(
                            "liquidbounce.commandManager.exceptionOccurred",
                            e.javaClass.simpleName ?: "Class name missing", e.message ?: "No message"
                        )
                    ),
                    metadata = MessageMetadata(id = "CommandManager#error")
                )
                logger.error("An exception occurred while executing a command", e)
            }
        }
    }

    @Suppress("unused")
    private val shutdownHandler = handler<ClientShutdownEvent> {
        isShuttingDown = true
        commandCoroutineScope.cancel()
    }

    /**
     * Handles command execution
     */
    @Suppress("unused")
    private val chatEventHandler = handler<ChatSendEvent>(priority = EventPriorityConvention.FIRST_PRIORITY) {
        if (!it.message.startsWith(CommandManager.GlobalSettings.prefix)) {
            return@handler
        }

        val commandBody = it.message.substring(CommandManager.GlobalSettings.prefix.length)
        try {
            CommandManager.execute(commandBody)
        } catch (e: Throwable) {
            handleExceptions(e)
        } finally {
            it.cancelEvent()
        }

        commandHistoryFile.appendingSink().buffer().use { sink ->
            sink.writeUtf8(commandBody)
                .writeByte('\n'.code)
        }
    }
}
