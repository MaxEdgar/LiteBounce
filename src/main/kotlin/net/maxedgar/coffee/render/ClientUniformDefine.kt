/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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

package net.maxedgar.coffee.render

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderPass
import net.maxedgar.coffee.Coffee
import net.maxedgar.coffee.utils.client.gpuDevice
import net.maxedgar.coffee.utils.render.std140Size
import net.minecraft.client.renderer.MappableRingBuffer
import java.util.function.Supplier

enum class ClientUniformDefine(val uboName: String, val size: Int) {
    DISTANCE_FADE("u_DistanceFade", std140Size { vec4 }),
    MESH_BASE_BLOCK_POS("u_MeshBaseBlockPos", std140Size { ivec3 }),
    ROUNDED_RECT("u_RoundedRect", std140Size { vec2 + float }),
    HAND_ITEM_LIGHTMAP("ItemChamsData", std140Size { int + float + vec4 + float + vec4 + float + int }),
    BLEND("BlendData", std140Size { vec4 }),
    THEME_BACKGROUND("ThemeBackgroundData", std140Size { float + vec2 + vec2 }),
    ;

    val bindGroupLayout: BindGroupLayout = BindGroupLayout.builder()
        .withUniform(this.uboName, UniformType.UNIFORM_BUFFER)
        .build()

    fun label(): String = "${Coffee.CLIENT_NAME} Uniform ${this.uboName} (${this.size}b)"

    @JvmOverloads
    fun createSingleBuffer(
        labelGetter: Supplier<String> = Supplier(this::label),
    ): GpuBufferSlice {
        return gpuDevice.createBuffer(
            labelGetter,
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            this.size.toLong(),
        ).slice()
    }

    @JvmOverloads
    fun createRingBuffer(
        labelGetter: Supplier<String> = Supplier(this::label),
    ): MappableRingBuffer {
        return MappableRingBuffer(
            labelGetter,
            GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_MAP_WRITE,
            this.size,
        )
    }

    fun setTo(renderPass: RenderPass, slice: GpuBufferSlice) {
        renderPass.setUniform(this.uboName, slice)
    }

}
