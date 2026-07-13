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
package net.maxedgar.coffee.utils.aiming.features.processors.anglesmooth

import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.utils.aiming.RotationTarget
import net.maxedgar.coffee.utils.aiming.data.Rotation

/**
 * This is used by [net.maxedgar.coffee.utils.aiming.features.processors.anglesmooth.impl.HumanizedAngleSmooth]
 * to define an angle smooth mode that does not affect the current rotation.
 *
 * It essentially does nothing.
 */
class NoneAngleSmooth(parent: ModeValueGroup<*>) : AngleSmooth("None", parent) {

    override fun calculateTicks(
        currentRotation: Rotation,
        targetRotation: Rotation
    ): Int = 0

    override fun process(
        rotationTarget: RotationTarget,
        currentRotation: Rotation,
        targetRotation: Rotation
    ): Rotation = currentRotation

}
