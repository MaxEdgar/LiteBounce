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
package net.maxedgar.coffee.features.command.commands.module.teleport

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.module.modules.movement.ModuleTeleport
import net.maxedgar.coffee.utils.client.player

/**
 * Teleport Command
 *
 * Allows you to teleport.
 *
 * Module: [ModuleTeleport]
 */
object CommandTeleport : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder
            .begin("teleport")
            .alias("tp")
            .requiresIngame()
            .parameter(
                ParameterBuilder
                    .begin<Float>("x")
                    .required()
                    .verifiedBy(ParameterBuilder.FLOAT_VALIDATOR)
                    .build(),
            )
            .parameter(
                ParameterBuilder
                    .begin<Float>("y|z")
                    .required()
                    .verifiedBy(ParameterBuilder.FLOAT_VALIDATOR)
                    .build()
            )
            .parameter(
                ParameterBuilder
                    .begin<Float>("z")
                    .optional()
                    .verifiedBy(ParameterBuilder.FLOAT_VALIDATOR)
                    .build()
            )
            .handler {
                val x = args[0] as Float
                val yOrZ = args[1] as Float
                val z = if (args.size > 2) args[2] as Float else yOrZ
                val y = if (args.size > 2) {
                    yOrZ.toDouble()
                } else if (ModuleTeleport.highTp) {
                    ModuleTeleport.highTpAmount.toDouble()
                } else {
                    player.y
                }

                ModuleTeleport.indicateTeleport(x.toDouble(), y, z.toDouble())
            }
            .build()
    }
}
