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

package net.maxedgar.coffee.render.engine

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.textures.GpuSampler
import net.maxedgar.coffee.features.module.MinecraftShortcuts
import net.maxedgar.coffee.render.createRenderPass

/**
 * @param blitPipeline should use `core/screenquad` for drawing
 */
abstract class OverlayShaderRenderer(
    val name: String,
    private val blitPipeline: RenderPipeline,
    private val useDepth: Boolean = false,
    private val needDefaultUniforms: Boolean = false,
) : MinecraftShortcuts {

    private val renderTargetHolder = LazyRenderTargetHolder("Custom shader FBO $name", useDepth)
    private val sampler: GpuSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
    var dirty: Boolean = false

    open fun shouldRender(): Boolean = true

    fun prepareRenderTarget(): RenderTarget {
        require(!dirty) { "OverlayShaderRenderer $name is dirty, draw it before starting another render pass" }

        return renderTargetHolder.initAndGet()
    }

    protected open fun preRender() {
        // Nothing to do
    }

    protected open fun onRender(pass: RenderPass) {
        // Nothing to do
    }

    protected open fun postRender() {
        // Nothing to do
    }

    fun drawBlitIfDirty(target: RenderTarget) {
        if (!dirty) {
            return
        }
        dirty = false

        preRender()

        val colorTexture = this.renderTargetHolder.raw?.colorTextureView
        requireNotNull(colorTexture) { "Overlay shader $name FBO color texture view is null" }

        target.createRenderPass({ "Overlay Shader $name blit pass" }).use { pass ->
            pass.setPipeline(blitPipeline)
            if (needDefaultUniforms) {
                RenderSystem.bindDefaultUniforms(pass)
            }
            pass.bindTexture("InSampler", colorTexture, sampler)
            onRender(pass)
            pass.draw(3, 1, 0, 0)
        }

        postRender()
    }

}
