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
package net.maxedgar.coffee.features.command.commands.ingame.creative

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.text.translateColorCodes
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.item.setInventoryItemCreative
import net.minecraft.core.component.DataComponents
import net.minecraft.world.InteractionHand

/**
 * ItemRename Command
 *
 * Allows you to rename an item held in the player's hand.
 */
object CommandItemRename : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("rename")
            .requiresIngame()
            .parameter(
                ParameterBuilder
                    .begin<String>("name")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                    .optional()
                    .vararg()
                    .build()
            )
            .handler {
                if (!player.hasInfiniteMaterials()) {
                    throw CommandException(command.result("mustBeCreative"))
                }

                val itemStack = player.getItemInHand(InteractionHand.MAIN_HAND)
                if (itemStack.isEmpty) {
                    throw CommandException(command.result("mustHoldItem"))
                }

                val name = (args.getOrElse(0, defaultValue = { arrayOf("") }) as Array<*>)
                    .joinToString(" ") { it as String }
                when (name) {
                    "" -> {
                        itemStack.remove(DataComponents.CUSTOM_NAME)
                        chat(regular(command.result("nameReset")), command)
                    }
                    else -> {
                        itemStack.set(DataComponents.CUSTOM_NAME, name.translateColorCodes().asPlainText())
                        chat(regular(command.result("renamedItem", itemStack.itemName, variable(name))), command)
                    }
                }
                player.setInventoryItemCreative(itemStack = itemStack, animation = false)
            }
            .build()
    }

}
