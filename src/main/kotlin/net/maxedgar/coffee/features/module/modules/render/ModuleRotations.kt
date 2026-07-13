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
package net.maxedgar.coffee.features.module.modules.render

import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.render.ModuleRotations.smooth
import net.maxedgar.coffee.render.drawBox
import net.maxedgar.coffee.render.drawLine
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.render.engine.type.Vec3f
import net.maxedgar.coffee.render.renderEnvironment
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.entity.lastRotation
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.maxedgar.coffee.utils.math.toVec3f
import net.minecraft.world.phys.AABB

/**
 * Rotations module
 *
 * Allows you to see server-sided rotations.
 */

object ModuleRotations : ClientModule("Rotations", ModuleCategories.RENDER) {

    /**
     * Body part to modify the rotation of.
     */
    private val bodyPart by multiEnumChoice("BodyPart", BodyPart.entries)

    @Suppress("unused")
    enum class BodyPart(
        override val tag: String,
    ) : Tagged {
        HEAD("Head"),
        BODY("Body");
    }

    fun isPartAllowed(part: BodyPart) = part in bodyPart

    /**
     * Smoothes the rotation visually only.
     */
    private val smooth by float("Smooth", 0.0f, 0.0f..0.3f)

    private val vectorLine by color("VectorLine", Color4b.WHITE.with(a = 0)) // alpha 0 means OFF
    private val vectorDot by color("VectorDot", Color4b(0x00, 0x80, 0xFF, 0x00))

    /**
     * The current model rotation, we could be using
     * [RotationManager.currentRotation] and [RotationManager.previousRotation]
     * directly but this is required for [smooth] to work.
     */
    var modelRotation: Rotation? = null
        get() = if (this.running) field else null
    var prevModelRotation: Rotation? = null

    @Suppress("unused")
    private val modelUpdater = handler<GameTickEvent>(priority = EventPriorityConvention.READ_FINAL_STATE) {
        val prev = prevModelRotation ?: player.lastRotation
        val current = RotationManager.currentRotation

        if (current == null) {
            prevModelRotation = modelRotation
            modelRotation = null
            return@handler
        }

        val next = if (smooth > 0f) {
            prev.interpolateTo(current, 1f - smooth)
        } else {
            current
        }

        prevModelRotation = modelRotation
        modelRotation = next
    }

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        val drawVectorLine = vectorLine.a > 0
        val drawVectorDot = vectorDot.a > 0

        if (drawVectorLine || drawVectorDot) {
            val currentRotation = RotationManager.currentRotation ?: return@handler
            val previousRotation = RotationManager.previousRotation ?: currentRotation

            val interpolatedRotationVec = previousRotation.directionVector
                .lerp(currentRotation.directionVector, event.partialTicks.toDouble())
                .toVec3f()

            val eyeVector = Vec3f.eyeVector(event.camera)

            event.renderEnvironment {
                val vector = eyeVector.fma(100f, interpolatedRotationVec)
                if (drawVectorLine) {
                    drawLine(eyeVector, vector, vectorLine.argb)
                }

                if (drawVectorDot) {
                    drawBox(AABB.ofSize(vector.toVec3d(), 2.5, 2.5, 2.5), vectorDot)
                }
            }
        }
    }

    override fun onDisabled() {
        this.modelRotation = null
        this.prevModelRotation = null
        super.onDisabled()
    }
}
