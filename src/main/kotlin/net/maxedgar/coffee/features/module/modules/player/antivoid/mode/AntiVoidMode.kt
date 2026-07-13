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
package net.maxedgar.coffee.features.module.modules.player.antivoid.mode

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.player.antivoid.ModuleAntiVoid
import net.minecraft.world.phys.Vec3

abstract class AntiVoidMode(name: String) : Mode(name) {

    override val parent: ModeValueGroup<*>
        get() = ModuleAntiVoid.mode

    // Cases in which the AntiVoid protection should not be active.
    open val isExempt: Boolean
        get() = player.isDeadOrDying || ModuleFly.running

    open fun discoverRescuePosition(): Vec3? {
        if (!ModuleAntiVoid.isLikelyFalling) {
            return player.position()
        }
        return null
    }

    /**
     * Attempt to safely move the player to a safe location.
     */
    abstract fun rescue(): Boolean

}
