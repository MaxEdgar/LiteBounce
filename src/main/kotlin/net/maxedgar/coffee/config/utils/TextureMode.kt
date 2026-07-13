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

package net.maxedgar.coffee.config.utils

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.config.types.toTextureProperty
import net.minecraft.client.renderer.texture.DynamicTexture

sealed class TextureMode(name: String) : Mode(name) {

    abstract val texture: DynamicTexture?

    class Custom(override val parent: ModeValueGroup<*>) : TextureMode("Custom") {
        override val texture by file("File").toTextureProperty(this)
    }

    class Builtin<T : Builtin.Preset>(
        override val parent: ModeValueGroup<*>,
        default: T,
        choices: Set<T>,
    ) : TextureMode("Builtin") {

        private val mode = enumChoice("Preset", default, choices)

        override val texture get() = mode.get().texture

        interface Preset : Tagged {
            val texture: DynamicTexture?
        }
    }

}
