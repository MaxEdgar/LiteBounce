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
package net.maxedgar.coffee.script.bindings.api

import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.script.bindings.api.ScriptRotationUtil.newRotationEntity
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.aiming.features.MovementCorrection
import net.maxedgar.coffee.utils.aiming.utils.raytraceBox
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.kotlin.Priority
import net.minecraft.world.entity.Entity
import kotlin.math.sqrt

/**
 * A collection of useful rotation utilities for the ScriptAPI.
 * This SHOULD not be changed in a way that breaks backwards compatibility.
 *
 * This is a singleton object, so it can be accessed from the script API like this:
 * ```js
 * api.rotationUtil.newRaytracedRotationEntity(entity, 4.2, 0.0)
 * rotationUtil.newRotationEntity(entity)
 * rotationUtil.aimAtRotation(rotation, true)
 * ```
 */
@Suppress("unused")
object ScriptRotationUtil {

    /**
     * Creates a new [net.maxedgar.coffee.utils.aiming.data.Rotation] from [entity]'s bounding box.
     * This uses raytracing, so it's guaranteed to be the best spot.
     *
     * It has a performance impact, so it's recommended to use [newRotationEntity] if you don't need the best spot.
     */
    @JvmName("newRaytracedRotationEntity")
    fun newRaytracedRotationEntity(entity: Entity, range: Double, throughWallsRange: Double): Rotation? {
        val box = entity.boundingBox

        // Finds the best spot (and undefined if no spot was found)
        val (rotation, _) = raytraceBox(
            mc.player!!.eyePosition,
            box,
            range = sqrt(range),
            wallsRange = throughWallsRange
        ) ?: return null

        return rotation
    }

    /**
     * Creates a new [Rotation] from [entity]'s bounding box.
     * This uses no raytracing, so it's not guaranteed to be the best spot.
     * It will aim at the center of the bounding box.
     *
     * It has almost zero performance impact, so it's recommended to use this if you don't need the best spot.
     */
    @JvmName("newRotationEntity")
    fun newRotationEntity(entity: Entity) = Rotation.lookingAt(
        point = entity.boundingBox.center,
        from = mc.player!!.eyePosition
    )

    /**
     * Aims at the given [rotation] using the in-built RotationManager.
     *
     * @param rotation The rotation to aim at.
     * @param fixVelocity Whether to fix the player's velocity.
     *   This means bypassing anti-cheat checks for aim-related movement.
     */
    @JvmName("aimAtRotation")
    fun aimAtRotation(rotation: Rotation, fixVelocity: Boolean) {
        RotationManager.setRotationTarget(
            rotation,
            valueGroup = RotationsValueGroup(
                object : EventListener { },
                movementCorrection = if (fixVelocity) MovementCorrection.SILENT else MovementCorrection.OFF
            ), priority = Priority.NORMAL, provider = ClientModule("ScriptAPI", ModuleCategories.MISC)
        )
    }

}
