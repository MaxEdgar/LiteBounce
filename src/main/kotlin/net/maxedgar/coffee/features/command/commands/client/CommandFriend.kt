/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
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
package net.maxedgar.coffee.features.command.commands.client

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.playerName
import net.maxedgar.coffee.features.misc.FriendManager
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.client.bold
import net.maxedgar.coffee.utils.client.bypassNameProtection
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.copyable
import net.maxedgar.coffee.utils.client.italic
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.onClick
import net.maxedgar.coffee.utils.client.onHover
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.removeMessage
import net.maxedgar.coffee.utils.client.variable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent

private const val MSG_NO_FRIENDS = "noFriends"
private const val MSG_SUCCESS = "success"
private const val MESSAGE_ID = "CFriend#info"

/**
 * Friend Command
 *
 * Provides subcommands related to managing friends, such as adding, removing, aliasing, listing, and clearing friends.
 */
object CommandFriend : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("friend")
            .hub()
            .subcommand(createAddSubcommand())
            .subcommand(createRemoveSubcommand())
            .subcommand(createAliasSubcommand())
            .subcommand(createListSubcommand())
            .subcommand(createClearSubcommand())
            .build()
    }

    private fun createClearSubcommand(): Command {
        return CommandBuilder
            .begin("clear")
            .handler {
                if (FriendManager.friends.isEmpty()) {
                    throw CommandException(command.result(MSG_NO_FRIENDS))
                } else {
                    FriendManager.friends.clear()

                    chat(
                        regular(command.result(MSG_SUCCESS)),
                        metadata = MessageMetadata(id = MESSAGE_ID)
                    )
                }
            }
            .build()
    }

    private fun createListSubcommand(): Command {
        return CommandBuilder
            .begin("list")
            .handler {
                if (FriendManager.friends.isEmpty()) {
                    chat(
                        command.result(MSG_NO_FRIENDS),
                        metadata = MessageMetadata(id = MESSAGE_ID)
                    )
                } else {
                    mc.gui.hud.chat.removeMessage(MESSAGE_ID)
                    val data = MessageMetadata(id = MESSAGE_ID, remove = false)

                    FriendManager.friends.forEachIndexed { index, friend ->
                        val alias = friend.alias ?: friend.getDefaultName(index)

                        val friendTextWithEvent = variable(friend.name)
                            .bypassNameProtection()
                            .copyable(copyContent = friend.name)
                            .italic(true)

                        val removeCommand = ".friend remove ${friend.name}"
                        val removeText = regular("Remove ${friend.name}")

                        val removeButton = regular("[X]")
                            .withStyle(ChatFormatting.RED)
                            .bold(true)
                            .onHover(HoverEvent.ShowText(removeText))
                            .onClick(ClickEvent.SuggestCommand(removeCommand))

                        chat(
                            regular("- "),
                            friendTextWithEvent,
                            regular(" ("),
                            variable(alias),
                            regular(") "),
                            removeButton,
                            metadata = data
                        )
                    }
                }
            }
            .build()
    }

    private fun createAliasSubcommand(): Command {
        return CommandBuilder
            .begin("alias")
            .parameter(
                ParameterBuilder
                    .begin<String>("name")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                    .autocompletedFrom { FriendManager.friends.map { it.name } }
                    .required()
                    .build()
            )
            .parameter(
                ParameterBuilder
                    .begin<String>("alias")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                    .required()
                    .build()
            )
            .handler {
                val name = args[0] as String
                val friend = FriendManager.friends.firstOrNull { it.name == name }

                if (friend != null) {
                    friend.alias = args[1] as String

                    chat(
                        regular(command.result(MSG_SUCCESS, variable(name), variable(args[1] as String))),
                        metadata = MessageMetadata(id = MESSAGE_ID)
                    )
                } else {
                    throw CommandException(command.result("notFriends", variable(name)))
                }
            }
            .build()
    }

    private fun createRemoveSubcommand(): Command {
        return CommandBuilder
            .begin("remove")
            .parameter(
                ParameterBuilder
                    .begin<String>("name")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                    .required()
                    .build()
            )
            .handler {
                val friend = FriendManager.Friend(args[0] as String, null)

                if (FriendManager.friends.remove(friend)) {
                    chat(
                        regular(command.result(MSG_SUCCESS, variable(friend.name))),
                        metadata = MessageMetadata(id = MESSAGE_ID)
                    )
                } else {
                    throw CommandException(command.result("notFriends", variable(friend.name)))
                }
            }
            .build()
    }

    private fun createAddSubcommand(): Command {
        return CommandBuilder
            .begin("add")
            .parameter(
                ParameterBuilder.playerName()
                    .required()
                    .build()
            )
            .parameter(
                ParameterBuilder
                    .begin<String>("alias")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                    .optional()
                    .build()
            )
            .handler {
                val friend = FriendManager.Friend(args[0] as String, args.getOrNull(1) as String?)

                if (FriendManager.friends.add(friend)) {
                    if (friend.alias == null) {
                        chat(
                            regular(command.result(MSG_SUCCESS, variable(friend.name))),
                            metadata = MessageMetadata(id = MESSAGE_ID)
                        )
                    } else {
                        chat(
                            regular(
                                command.result(
                                    "successAlias",
                                    variable(friend.name),
                                    variable(friend.alias!!)
                                )
                            ),
                            metadata = MessageMetadata(id = MESSAGE_ID)
                        )
                    }
                } else {
                    throw CommandException(command.result("alreadyFriends", variable(friend.name)))
                }

            }
            .build()
    }
}
