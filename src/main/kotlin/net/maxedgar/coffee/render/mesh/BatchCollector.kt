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

package net.maxedgar.coffee.render.mesh

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.maxedgar.coffee.render.ClientTesselator
import net.maxedgar.coffee.render.bindDefaultUniforms
import net.maxedgar.coffee.render.bindDynamicTransformsUniform
import net.maxedgar.coffee.render.bindTextures
import net.maxedgar.coffee.render.createRenderPass
import net.maxedgar.coffee.render.engine.RenderDrawKey
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.render.mesh.MeshDraw.DefaultUploader.bindAndDraw
import net.maxedgar.coffee.render.mesh.MeshDraw.DefaultUploader.toMeshDraw
import net.maxedgar.coffee.render.setUniforms
import net.maxedgar.coffee.render.setupRenderTypeScissor

internal class BatchCollector {

    private data class PendingDraw(
        val key: RenderDrawKey,
        val builder: BufferBuilder,
        val order: Int,
        var readyToBuild: Boolean,
    )

    private data class BuiltDraw(
        val key: RenderDrawKey,
        val meshDraw: MeshDraw,
        val order: Int,
    ) : Comparable<BuiltDraw> {
        override fun compareTo(other: BuiltDraw): Int {
            val r1 = key.compareTo(other.key)
            if (r1 != 0) return r1
            return order.compareTo(other.order)
        }
    }

    private val bufferAllocatorInUse = ObjectArrayList<ByteBufferBuilder>()
    private val consolidatedDraws = Object2ObjectOpenHashMap<RenderDrawKey, PendingDraw>()
    private val drawOrder = ObjectArrayList<PendingDraw>()
    private val builtBuffers = ObjectArrayList<BuiltDraw>()

    private val appendNewBuilder = Object2ObjectFunction<RenderDrawKey, PendingDraw> {
        val key = it as RenderDrawKey
        val builder = ClientTesselator.begin(key.pipeline, bufferAllocatorInUse)
        val draw = PendingDraw(key, builder, order = drawOrder.size, readyToBuild = false)
        drawOrder += draw
        draw
    }

    fun start(key: RenderDrawKey): MeshBuildScope {
        if (!key.pipeline.canConsolidateConsecutiveGeometry()) {
            val draw = appendNewBuilder.apply(key)
            return SeparateMeshBuildScope(draw)
        }

        val draw = consolidatedDraws.computeIfAbsent(key, appendNewBuilder)
        draw.readyToBuild = true
        return ConsolidatedMeshBuildScope(draw.builder)
    }

    fun flush(renderTarget: RenderTarget, dynamicTransforms: GpuBufferSlice?) {
        try {
            if (drawOrder.isEmpty) {
                return
            }

            buildAllDraws()

            if (builtBuffers.isEmpty) {
                return
            }

            builtBuffers.sort()

            renderTarget.createRenderPass(
                { "BatchCollector draw" },
                allowOverride = true,
            ).use { pass ->
                pass.setupRenderTypeScissor()
                pass.bindDefaultUniforms()
                dynamicTransforms?.let(pass::bindDynamicTransformsUniform)

                builtBuffers.forEach { draw ->
                    pass.setPipeline(draw.key.pipeline)
                    pass.setUniforms(draw.key.uniforms)
                    pass.bindTextures(draw.key.textures)
                    pass.bindAndDraw(draw.meshDraw)
                }
            }
        } finally {
            builtBuffers.clear()
            clearBuilders()
            ClientTesselator.recycleAll(bufferAllocatorInUse)
            bufferAllocatorInUse.clear()
        }
    }

    private fun buildAllDraws() {
        for (draw in drawOrder) {
            if (draw.readyToBuild) {
                draw.builder.build()?.use { meshData ->
                    try {
                        builtBuffers += BuiltDraw(
                            draw.key,
                            meshData.toMeshDraw(draw.key.pipeline),
                            draw.order,
                        )
                    } catch (e: IllegalStateException) {
                        // Intel GPU workaround: If the ring buffer fence can't be awaited,
                        // skip this draw gracefully instead of crashing the game.
                        logger.warn("Skipping draw for '${draw.key.pipeline.location}' due to GPU fence issue: ${e.message}")
                    }
                }
            }
        }
    }

    private fun clearBuilders() {
        consolidatedDraws.clear()
        drawOrder.clear()
    }

    private class ConsolidatedMeshBuildScope(
        override val consumer: BufferBuilder,
    ) : MeshBuildScope {

        override fun close() {
            // Noop because the flag is already set
        }

    }

    private class SeparateMeshBuildScope(
        private val draw: PendingDraw,
    ) : MeshBuildScope {

        private var closed = false

        override val consumer get() = draw.builder

        override fun close() {
            if (closed) {
                return
            }

            closed = true
            draw.readyToBuild = true
        }

    }
}

/**
 * @see net.minecraft.client.renderer.rendertype.RenderType.canConsolidateConsecutiveGeometry
 */
private fun RenderPipeline.canConsolidateConsecutiveGeometry(): Boolean =
    !primitiveTopology.connectedPrimitives
