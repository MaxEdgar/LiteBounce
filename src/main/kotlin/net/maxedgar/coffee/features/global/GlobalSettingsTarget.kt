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

import net.ccbluex.fastutil.enumSetAllOf
import net.ccbluex.fastutil.enumSetOf
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.utils.combat.Targets
import java.util.EnumSet

object GlobalSettingsTarget : ValueGroup(
    name = "Targets",
    aliases = listOf("Enemies")
) {

    val combatChoices = multiEnumChoice("Combat",
        default = enumSetOf(
            Targets.PLAYERS,
            Targets.HOSTILE,
            Targets.ANGERABLE,
            Targets.WATER_CREATURE,
            Targets.INVISIBLE,
        ),
        choices = enumSetAllOf<Targets>().apply { remove(Targets.SELF) },
    )

    val visualChoices = multiEnumChoice("Visual",
        default = enumSetOf(
            Targets.PLAYERS,
            Targets.HOSTILE,
            Targets.ANGERABLE,
            Targets.WATER_CREATURE,
            Targets.INVISIBLE,
        ),
        choices = enumSetAllOf(),
    )

    inline val combat: EnumSet<Targets> get() = combatChoices.get() as EnumSet

    inline val visual: EnumSet<Targets> get() = visualChoices.get() as EnumSet
}
