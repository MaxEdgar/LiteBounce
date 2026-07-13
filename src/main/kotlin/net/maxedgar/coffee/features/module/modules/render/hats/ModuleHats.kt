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

package net.maxedgar.coffee.features.module.modules.render.hats

import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.render.hats.modes.HatsCone
import net.maxedgar.coffee.features.module.modules.render.hats.modes.HatsFlower
import net.maxedgar.coffee.features.module.modules.render.hats.modes.HatsHalo
import net.maxedgar.coffee.features.module.modules.render.hats.modes.HatsImage
import net.maxedgar.coffee.features.module.modules.render.hats.modes.HatsOrbs
import net.maxedgar.coffee.features.module.modules.render.hats.modes.HatsStar
import net.maxedgar.coffee.render.utils.AnimatedValueGroup
import org.joml.Vector2f

/**
 * @author minecrrrr
 */
object ModuleHats : ClientModule("Hats", ModuleCategories.RENDER) {

    object HeightOffset : AnimatedValueGroup("HeightOffset") {
        override val curve = curve("Height") {
            "Progress" x 0f..1f
            "Offset" y 0f..2f
            points(Vector2f(0f, 0.2f), Vector2f(1f, 0.2f))
        }
    }

    init {
        tree(HeightOffset)
    }

    val modes = choices("Mode", 0) {
        arrayOf(
            HatsCone,
            HatsHalo,
            HatsOrbs,
            HatsFlower,
            HatsStar,
            HatsImage,
        )
    }.apply { tagBy(this) }

}
