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
package net.maxedgar.coffee.utils.aiming.preference

import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.math.fma
import net.maxedgar.coffee.utils.math.geometry.Ray
import net.maxedgar.coffee.utils.math.minus
import net.maxedgar.coffee.utils.math.sq
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class LeastDifferencePreference(
    private val baseRotation: Rotation,
    private val basePoint: Vec3? = null
) : RotationPreference {

    override fun getPreferredSpot(eyesPos: Vec3, range: Double): Vec3 {
        if (basePoint != null) {
            return basePoint
        }

        return eyesPos.fma(range, baseRotation.directionVector)
    }

    override fun getPreferredSpotOnBox(box: AABB, eyesPos: Vec3, range: Double): Vec3 {
        if (basePoint != null) {
            return basePoint
        }

        val preferredSpot = getPreferredSpot(eyesPos, range)
        if (box.contains(preferredSpot)) {
            return preferredSpot
        }

        val look = Ray(eyesPos, preferredSpot - eyesPos)
        return look.firstIntersectionWith(box)
            ?.takeIf { it.distanceToSqr(eyesPos) <= range.sq() }
            ?: preferredSpot
    }

    override fun compare(o1: Rotation, o2: Rotation): Int {
        val rotationDifferenceO1 = baseRotation.angleTo(o1)
        val rotationDifferenceO2 = baseRotation.angleTo(o2)

        return rotationDifferenceO1.compareTo(rotationDifferenceO2)
    }

    companion object {

        @JvmStatic
        fun leastDifferenceToCurrentRotation() =
            LeastDifferencePreference(RotationManager.currentRotation ?: player.rotation)

        @JvmStatic
        fun leastDifferenceToLastPoint(eyes: Vec3, point: Vec3): LeastDifferencePreference {
            return LeastDifferencePreference(Rotation.lookingAt(point, from = eyes), point)
        }

    }

}
