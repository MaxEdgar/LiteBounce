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

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.render.clearColor
import net.maxedgar.coffee.utils.render.clearColorAndDepth

/**
 * A holder for a RenderTarget that initializes it lazily and handles resizing.
 */
class LazyRenderTargetHolder(
    val name: String,
    @JvmField val useDepth: Boolean
) : AutoCloseable {
    var raw: RenderTarget? = null
        private set

    /**
     * Destroys the buffers and releases the RenderTarget.
     */
    override fun close() {
        this.raw?.destroyBuffers()
        this.raw = null
    }

    /**
     * Initializes the RenderTarget if needed, or resizes/clears it if it already exists, then returns it.
     */
    fun initAndGet(): RenderTarget {
        val width = mc.window.width
        val height = mc.window.height

        val current = this.raw

        if (current == null) {
            val new = TextureTarget(name, width, height, useDepth, GpuFormat.RGBA8_UNORM)
            this.raw = new
            return new
        } else {
            if (width != current.width || height != current.height) {
                current.resize(width, height) // Resizing includes clearing the framebuffer
            } else if (useDepth) {
                current.clearColorAndDepth()
            } else {
                current.colorTexture!!.clearColor()
            }
            return current
        }
    }
}
