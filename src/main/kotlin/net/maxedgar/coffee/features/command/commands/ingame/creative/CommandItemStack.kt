/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
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
package net.maxedgar.coffee.features.command.commands.ingame.creative

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandException
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.item.setInventoryItemCreative

object CommandItemStack : Command.Factory, MinecraftShortcuts {

    private val amountParameter = ParameterBuilder
        .begin<Int>("amount")
        .verifiedBy(ParameterBuilder.intRange(1, 64))
        .autocompletedFrom { listOf("16", "32", "64") }
        .optional()
        .build()


    @Suppress("detekt:ThrowsCount")
    override fun createCommand(): Command {
        return CommandBuilder
            .begin("stack")
            .requiresIngame()
            .parameter(amountParameter)
            .handler {
                if (!player.hasInfiniteMaterials()) {
                    throw CommandException(command.result("mustBeCreative"))
                }

                val mainHandStack = player.mainHandItem
                if (mainHandStack.isEmpty) {
                    throw CommandException(command.result("noItem"))
                }

                val amount = args.getOrElse(0, defaultValue = { 64 }) as Int

                if (mainHandStack.count == amount) {
                    chat(regular(command.result("hasAlreadyAmount", variable(amount.toString()))), command)
                    return@handler
                }

                mainHandStack.count = amount

                player.setInventoryItemCreative(itemStack = mainHandStack)

                chat(regular(command.result("amountChanged", variable(amount.toString()))), command)
            }
            .build()
    }

}
