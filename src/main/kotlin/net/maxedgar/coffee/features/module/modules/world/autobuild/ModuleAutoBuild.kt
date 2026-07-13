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
package net.maxedgar.coffee.features.module.modules.world.autobuild

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.block.placer.BlockPlacer
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.kotlin.Priority

/**
 * AutoBuild module
 *
 * Builds structures.
 */
object ModuleAutoBuild : ClientModule("AutoBuild", ModuleCategories.WORLD, aliases = listOf("Platform", "AutoPortal")) {

    private val mode = choices("Mode", PortalMode, arrayOf(PortalMode, PlatformMode)).apply { tagBy(this) }
    val placer = tree(BlockPlacer("Placing", this, Priority.NORMAL, { mode.activeMode.getSlot() }))

    init {
        mode.onChanged { enabled = false }
    }

    override fun onEnabled() {
        mode.activeMode.enabled()
    }

    override fun onDisabled() {
        placer.disable()
        mode.activeMode.disabled()
    }

    abstract class AutoBuildMode(name: String) : Mode(name) {

        abstract fun getSlot(): HotbarItemSlot?

        open fun enabled() {}

        open fun disabled() {}

        override val parent: ModeValueGroup<*>
            get() = mode

    }

}
