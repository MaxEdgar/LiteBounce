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
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.maxedgar.coffee.utils.item.setInventoryItemCreative
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import java.util.UUID

/**
 * CommandItemSkull
 *
 * Allows you to create a player skull item with a specified name.
 */
object CommandItemSkull : Command.Factory, MinecraftShortcuts {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("skull")
            .requiresIngame()
            .parameter(
                ParameterBuilder
                    .begin<String>("name")
                    .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                    .required()
                    .build()
            )
            .handler {
                if (!player.hasInfiniteMaterials()) {
                    throw CommandException(command.result("mustBeCreative"))
                }

                val name = args[0] as String

                val itemStack = ItemStack(Items.PLAYER_HEAD)
                    .apply {
                        val profile = runCatching { UUID.fromString(name) }
                            .fold(
                                onSuccess = { ResolvableProfile.createUnresolved(it) },
                                onFailure = { ResolvableProfile.createUnresolved(name) }
                            )
                        DataComponentPatch.builder()
                            .set(DataComponents.PROFILE, profile)
                            .build()
                            .also { applyComponents(it) }
                    }

                val emptySlot = player.inventory.freeSlot
                if (emptySlot == -1) {
                    throw CommandException(command.result("noEmptySlot"))
                }

                player.setInventoryItemCreative(emptySlot, itemStack)
                chat(regular(command.result("skullGiven", variable(name))), command)
            }
            .build()
    }

}
