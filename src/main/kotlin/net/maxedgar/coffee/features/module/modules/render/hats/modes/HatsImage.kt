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

package net.maxedgar.coffee.features.module.modules.render.hats.modes

import net.maxedgar.coffee.config.types.toTextureProperty
import net.maxedgar.coffee.features.module.modules.render.hats.HatsMode
import net.maxedgar.coffee.render.WorldRenderEnvironment
import net.maxedgar.coffee.render.drawTexQuad
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.render.withPush
import net.minecraft.util.Mth
import org.joml.Quaternionf
import org.joml.Vector2f

internal object HatsImage : HatsMode("Image") {

    private val image by file("Image").toTextureProperty(this, printErrorToChat = true)
    private val colorModulator by color("ColorModulator", Color4b.WHITE)
    private val scale by vec2f("Scale", Vector2f(1f, 1f))
    private val spinSpeed by float("SpinSpeed", 1f, -10f..10f)

    private val ROTATION = Quaternionf()

    override fun WorldRenderEnvironment.drawHat(isHurt: Boolean) {
        val texture = image ?: return

        poseStack.withPush {
            mulPose(
                ROTATION.scaling(1f)
                    .rotateX(Mth.HALF_PI)
                    .rotateZ(getRotationAngle(spinSpeed))
            )
            scale(scale.x(), scale.y(), 1f)

            drawTexQuad(texture, colorModulator.argb)
        }
    }
}
