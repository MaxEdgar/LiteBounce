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
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.render.esp.ModuleESP
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.render.entity
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.LivingEntity

/**
 * TrueSight module
 *
 * Allows you to see invisible objects and entities.
 */
object ModuleTrueSight : ClientModule("TrueSight", ModuleCategories.RENDER) {
    private val sight by multiEnumChoice("Sight", Sight.entries)

    val barriers get() = Sight.BARRIERS in sight
    val entities get() = Sight.ENTITIES in sight

    val entityColor by color("EntityColor", Color4b(255, 255, 255, 100))
    val entityFeatureLayerColor by color("EntityFeatureLayerColor", Color4b(255, 255, 255, 120))

    @JvmStatic
    @Suppress("ComplexCondition")
    fun canRenderEntities(state: LivingEntityRenderState): Boolean {
        val enabled = this.running && entities

        val entity = state.entity as? LivingEntity ?: return false

        return (enabled || ModuleESP.running && ModuleESP.requiresTrueSight(entity))
            && entity.isInvisible
    }

    private enum class Sight(
        override val tag: String
    ) : Tagged {
        BARRIERS("Barriers"),
        ENTITIES("Entities")
    }
}
