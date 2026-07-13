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

import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.misc.FriendManager
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.render.GenericDistanceHSBColorMode
import net.maxedgar.coffee.render.GenericEntityHealthColorMode
import net.maxedgar.coffee.render.GenericRainbowColorMode
import net.maxedgar.coffee.render.GenericStaticColorMode
import net.maxedgar.coffee.render.drawLines
import net.maxedgar.coffee.render.drawLinesWithWidth
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.render.engine.type.Vec3f
import net.maxedgar.coffee.render.renderEnvironment
import net.maxedgar.coffee.utils.combat.EntityTaggingManager
import net.maxedgar.coffee.utils.entity.RenderedEntities
import net.maxedgar.coffee.utils.entity.cameraDistanceSq
import net.maxedgar.coffee.utils.entity.interpolateCurrentPosition
import net.maxedgar.coffee.utils.math.sq
import net.maxedgar.coffee.utils.math.toVec3f

/**
 * Tracers module
 *
 * Draws a line to every entity a certain radius.
 */

object ModuleTracers : ClientModule("Tracers", ModuleCategories.RENDER) {

    private val modes = choices("ColorMode", 0) {
        arrayOf(
            GenericDistanceHSBColorMode.entity(it),
            GenericEntityHealthColorMode(it),
            GenericStaticColorMode(it, Color4b(0, 160, 255, 255)),
            GenericRainbowColorMode(it)
        )
    }

    private val lineWidth by float("LineWidth", 1f, 1f..16f)

    private val maximumDistance by float("MaximumDistance", 128F, 1F..512F)

    override fun onEnabled() {
        RenderedEntities.subscribe(this)
    }

    override fun onDisabled() {
        RenderedEntities.unsubscribe(this)
    }

    val renderHandler = handler<WorldRenderEvent> { event ->
        if (RenderedEntities.isEmpty()) {
            return@handler
        }

        event.renderEnvironment {
            val eyeVector = Vec3f.eyeVector(camera)

            val maxDistanceSq = maximumDistance.sq()
            for (entity in RenderedEntities) {
                val distanceSq = entity.position().cameraDistanceSq().toFloat()
                if (distanceSq > maxDistanceSq) {
                    continue
                }

                val color = if (FriendManager.isFriend(entity)) {
                    Color4b.BLUE
                } else {
                    EntityTaggingManager.getTag(entity).color ?: modes.activeMode.getColor(entity)
                }

                val pos = relativeToCamera(entity.interpolateCurrentPosition(event.partialTicks)).toVec3f()
                val topPos = pos.add(0f, entity.bbHeight, 0f)

                if (lineWidth == 1.0f) {
                    drawLines(color.argb, eyeVector, pos, pos, topPos)
                } else {
                    drawLinesWithWidth(color.argb, lineWidth, eyeVector, pos, pos, topPos)
                }
            }
        }

    }
}
