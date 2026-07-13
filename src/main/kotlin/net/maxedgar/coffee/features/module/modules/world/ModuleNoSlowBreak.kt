/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.features.module.modules.world

import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories

/**
 * NoSlowBreak module
 *
 * Automatically adjusts breaking speed when in negatively affecting situations.
 */
object ModuleNoSlowBreak : ClientModule("NoSlowBreak", ModuleCategories.WORLD) {
    @Suppress("ObjectPropertyNaming")
    private val `when` by multiEnumChoice("When",
        When.MINING_FATIGUE,
        When.ON_AIR
    )

    @JvmStatic
    val miningFatigue get() = running && When.MINING_FATIGUE in `when`

    @JvmStatic
    val onAir get() = running && When.ON_AIR in `when`

    @JvmStatic
    val water get() = running && When.UNDERWATER in `when`

    @Suppress("unused")
    private enum class When(override val tag: String) : Tagged {
        MINING_FATIGUE("MiningFatigue"),
        ON_AIR("OnAir"),
        UNDERWATER("Underwater");
    }
}
