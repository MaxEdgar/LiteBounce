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

import kotlinx.atomicfu.atomic
import net.maxedgar.coffee.event.events.DrawOutlinesEvent
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.render.CachedMeshStorage
import net.maxedgar.coffee.render.ClientRenderPipelines
import net.maxedgar.coffee.render.GenericRainbowColorMode
import net.maxedgar.coffee.render.GenericStaticColorMode
import net.maxedgar.coffee.render.MapColorMode
import net.maxedgar.coffee.render.addShapeFaces
import net.maxedgar.coffee.render.addShapeOutlines
import net.maxedgar.coffee.render.buildMesh
import net.maxedgar.coffee.render.drawGenericBlockESP
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.render.getDynamicTransformsUniform
import net.maxedgar.coffee.render.translate
import net.maxedgar.coffee.render.utils.DistanceFadeUniformValueGroup
import net.maxedgar.coffee.render.withPush
import net.maxedgar.coffee.utils.block.AbstractBlockLocationTracker
import net.maxedgar.coffee.utils.block.ChunkScanner
import net.maxedgar.coffee.utils.inventory.findBlocksEndingWith
import net.maxedgar.coffee.utils.math.PositionedVoxelShape
import net.maxedgar.coffee.utils.math.mergeAdjacentVoxelShapes
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Matrix4f
import java.util.concurrent.ConcurrentSkipListSet

/**
 * BlockESP module
 *
 * Allows you to see selected blocks through walls.
 */

object ModuleBlockESP : ClientModule("BlockESP", ModuleCategories.RENDER) {

    private val modes = choices("Mode", 0) {
        arrayOf(
            BoxMode,
            GlowMode,
        )
    }
    private val targets by blocks(
        "Targets",
        ConcurrentSkipListSet(findBlocksEndingWith("_BED", "DRAGON_EGG"))
    ).onChange {
        if (running) {
            onDisabled()
            onEnabled()
        }
        it
    }

    private val colorMode = choices("ColorMode", 0) {
        arrayOf(
            MapColorMode(it),
            GenericStaticColorMode(it, Color4b(255, 179, 72, 50)),
            GenericRainbowColorMode(it)
        )
    }

    private val distanceFade = tree(DistanceFadeUniformValueGroup())
    private val mergeAdjacent by boolean("MergeAdjacent", false).onChanged {
        markDirtyForModes()
    }

    sealed class Mode(name: String) : net.maxedgar.coffee.config.types.group.Mode(name) {
        final override val parent get() = modes

        protected var useColor = false
        protected val dirtyFlag = atomic(true)

        fun markDirty() {
            if (this.running) {
                dirtyFlag.value = true
            }
        }

        final override fun enable() {
            dirtyFlag.value = true
            super.enable()
        }

        protected fun getDynamicTransformsUniform(
            modelView: Matrix4f? = null,
            colorModulatorAlpha: Int = -1,
        ) = getDynamicTransformsUniform(
            modelView = modelView,
            colorModulator = if (useColor) {
                Color4b.WHITE
            } else {
                val color = colorMode.activeMode.getColor(BlockPos.ZERO to Blocks.AIR.defaultBlockState())
                if (colorModulatorAlpha == -1) color else color.alpha(colorModulatorAlpha)
            },
        )
    }

    private object BoxMode : Mode("Box") {
        private val outline by boolean("Outline", true).onChanged {
            if (!it && running) {
                outlinesRenderState.clearStates()
            }
        }
        private val facesRenderState = CachedMeshStorage("${ModuleBlockESP.name} $name Faces")
        private val outlinesRenderState = CachedMeshStorage("${ModuleBlockESP.name} $name Outlines")

        override fun disable() {
            facesRenderState.clearStates()
            facesRenderState.clearBuffers()
            outlinesRenderState.clearStates()
            outlinesRenderState.clearBuffers()
            super.disable()
        }

        @Suppress("unused")
        private val renderHandler = handler<WorldRenderEvent> { event ->
            if (outline) {
                mc.gameRenderer.mainRenderTarget().drawGenericBlockESP(
                    outlinesRenderState,
                    ClientRenderPipelines.relativeLines(useColor),
                    distanceFade,
                ) {
                    getDynamicTransformsUniform(
                        modelView = event.poseStack.last().pose(),
                        colorModulatorAlpha = 150,
                    )
                }
            }

            mc.gameRenderer.mainRenderTarget().drawGenericBlockESP(
                facesRenderState,
                ClientRenderPipelines.relativeQuads(useColor),
                distanceFade,
            ) {
                getDynamicTransformsUniform(
                    modelView = event.poseStack.last().pose(),
                )
            }
        }

        @Suppress("unused")
        private val tickHandler = handler<GameTickEvent> {
            if (BlockTracker.isEmpty()) {
                facesRenderState.clearStates()
                outlinesRenderState.clearStates()
                return@handler
            }

            if (!dirtyFlag.compareAndSet(expect = true, update = false)) {
                return@handler
            }

            val colorMode = colorMode.activeMode
            useColor = colorMode.isParamSensitive
            val mergedShapes = collectBlockShapes(colorMode, useColor)

            facesRenderState.buildMesh(
                pipeline = ClientRenderPipelines.relativeQuads(useColor),
                origin = player.blockPosition(),
            ) { pose, origin ->
                for (mergedShape in mergedShapes) {
                    pose.withPush {
                        translate(mergedShape.blockPos, origin)
                        addShapeFaces(last().pose(), mergedShape.shape, mergedShape.key.color)
                    }
                }
            }

            if (outline) {
                outlinesRenderState.buildMesh(
                    pipeline = ClientRenderPipelines.relativeLines(useColor),
                    origin = player.blockPosition(),
                ) { pose, meshOrigin ->
                    for (mergedShape in mergedShapes) {
                        pose.withPush {
                            translate(mergedShape.blockPos, meshOrigin)
                            addShapeOutlines(last().pose(), mergedShape.shape, mergedShape.key.color)
                        }
                    }
                }
            }
        }

    }

    object GlowMode : Mode("Glow") {
        private val renderState = CachedMeshStorage("${ModuleBlockESP.name} $name")

        override fun disable() {
            renderState.clearStates()
            renderState.clearBuffers()
            super.disable()
        }

        @Suppress("unused")
        private val renderHandler = handler<DrawOutlinesEvent> { event ->
            val dirty = event.renderTarget.drawGenericBlockESP(
                renderState,
                ClientRenderPipelines.outlineQuads(useColor),
                distanceFade,
            ) {
                getDynamicTransformsUniform(
                    colorModulatorAlpha = 255,
                )
            }

            if (dirty) {
                event.markDirty()
            }
        }

        @Suppress("unused")
        private val tickHandler = handler<GameTickEvent> {
            if (BlockTracker.isEmpty()) {
                renderState.clearStates()
                return@handler
            }

            if (!dirtyFlag.compareAndSet(expect = true, update = false)) {
                return@handler
            }

            val colorMode = colorMode.activeMode
            useColor = colorMode.isParamSensitive
            val origin = player.blockPosition()

            renderState.buildMesh(
                pipeline = ClientRenderPipelines.outlineQuads(useColor),
                origin = origin,
            ) { pose, meshOrigin ->
                for (mergedShape in collectBlockShapes(colorMode, useColor)) {
                    pose.withPush {
                        translate(mergedShape.blockPos, meshOrigin)
                        addShapeFaces(last().pose(), mergedShape.shape, mergedShape.key.color?.alpha(255))
                    }
                }
            }
        }

    }

    override fun onEnabled() {
        ChunkScanner.subscribe(BlockTracker)
    }

    override fun onDisabled() {
        ChunkScanner.unsubscribe(BlockTracker)
        markDirtyForModes()
    }

    private fun markDirtyForModes() {
        modes.modes.forEach { it.markDirty() }
    }

    private inline fun forEachTrackedBlocks(
        block: (blockPos: BlockPos, blockState: BlockState, outlineShape: VoxelShape) -> Unit,
    ) {
        for ((blockPos, t) in BlockTracker.iterate()) {
            val blockState = t.state
            val outlineShape = t.shape
            block(blockPos, blockState, outlineShape)
        }
    }

    private fun collectBlockShapes(
        colorMode: net.maxedgar.coffee.render.GenericColorMode<Pair<BlockPos, BlockState>>,
        useColor: Boolean,
    ): List<PositionedVoxelShape<BlockMergeKey>> {
        val shapes = buildList {
            forEachTrackedBlocks { blockPos, blockState, outlineShape ->
                val color = if (useColor) colorMode.getColor(blockPos to blockState) else null
                add(
                    PositionedVoxelShape(
                        blockPos = blockPos.asLong(),
                        key = BlockMergeKey(blockState.block, color),
                        shape = outlineShape,
                    )
                )
            }
        }

        return if (mergeAdjacent) shapes.mergeAdjacentVoxelShapes() else shapes
    }

    private data class BlockMergeKey(val block: Block, val color: Color4b?)

    private class TrackedState(@JvmField val state: BlockState, @JvmField val shape: VoxelShape)

    private object BlockTracker : AbstractBlockLocationTracker.BlockPos2State<TrackedState>() {
        override fun getStateFor(pos: BlockPos, state: BlockState): TrackedState? {
            return if (!state.isAir && state.block in targets) {
                TrackedState(state, state.getShape(world, pos))
            } else {
                null
            }
        }

        override fun onUpdated() {
            markDirtyForModes()
        }
    }

}
