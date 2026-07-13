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

package net.maxedgar.coffee.event.events

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.vertex.PoseStack
import net.maxedgar.coffee.annotations.Tag
import net.maxedgar.coffee.event.Event
import net.maxedgar.coffee.render.WorldRenderEnvironment
import net.maxedgar.coffee.render.getDynamicTransformsUniform
import net.maxedgar.coffee.render.mesh.BatchCollector
import net.minecraft.client.Camera
import net.minecraft.client.gui.GuiGraphicsExtractor

@Tag("gameRender")
object GameRenderEvent : Event()

@Tag("screenRender")
class ScreenRenderEvent(val context: GuiGraphicsExtractor, val partialTicks: Float) : Event()

@Tag("worldRender")
class WorldRenderEvent(
    val poseStack: PoseStack,
    val camera: Camera,
    val partialTicks: Float,
    val renderTarget: RenderTarget,
) : Event(), AutoCloseable {

    @Deprecated("For scripts only")
    val matrixStack get() = poseStack

    private val batchCollector = BatchCollector()

    val environment = WorldRenderEnvironment(
        renderTarget = renderTarget,
        poseStack = poseStack,
        camera = camera,
        batchCollector = batchCollector,
    )

    override fun close() {
        batchCollector.flush(renderTarget, getDynamicTransformsUniform())
    }

}

/**
 * Sometimes, modules might want to contribute something to the glow framebuffer. They can hook this event
 * in order to do so.
 *
 * Note: After writing to the outline framebuffer [markDirty] must be called.
 */
@Tag("drawOutlines")
class DrawOutlinesEvent(
    val renderTarget: RenderTarget,
    val pose: PoseStack,
    val partialTicks: Float,
) : Event() {
    var dirtyFlag: Boolean = false
        private set

    /**
     * Called when the framebuffer was edited.
     */
    fun markDirty() {
        this.dirtyFlag = true
    }
}

@Tag("overlayRender")
class OverlayRenderEvent(
    val context: GuiGraphicsExtractor,
    val tickDelta: Float,
) : Event()
