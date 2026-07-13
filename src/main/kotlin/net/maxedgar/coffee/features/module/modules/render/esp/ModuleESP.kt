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
package net.maxedgar.coffee.features.module.modules.render.esp

import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.features.misc.FriendManager
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.render.esp.modes.Esp2DMode
import net.maxedgar.coffee.features.module.modules.render.esp.modes.EspBoxMode
import net.maxedgar.coffee.features.module.modules.render.esp.modes.EspGlowMode
import net.maxedgar.coffee.features.module.modules.render.esp.modes.EspLegacy2DMode
import net.maxedgar.coffee.render.GenericDistanceHSBColorMode
import net.maxedgar.coffee.render.GenericEntityHealthColorMode
import net.maxedgar.coffee.render.GenericRainbowColorMode
import net.maxedgar.coffee.render.GenericStaticColorMode
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.combat.EntityTaggingManager
import net.maxedgar.coffee.utils.combat.shouldBeShown
import net.maxedgar.coffee.utils.entity.RenderedEntities
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

/**
 * ESP module
 *
 * Allows you to see targets through walls.
 */
object ModuleESP : ClientModule("ESP", ModuleCategories.RENDER) {

    override val baseKey: String
        get() = "${ConfigSystem.KEY_PREFIX}.module.esp"

    val modes = choices("Mode", EspGlowMode, arrayOf(
        EspBoxMode,
        Esp2DMode,
        EspLegacy2DMode,
//        EspOutlineMode,
        EspGlowMode
    ))

    private val colorModes = choices("ColorMode", 0) {
        arrayOf(
            GenericDistanceHSBColorMode.entity(it),
            GenericEntityHealthColorMode(it),
            GenericStaticColorMode(it, Color4b.WHITE.with(a = 100)),
            GenericRainbowColorMode(it)
        )
    }
    private val invisibleColor by color("Invisible", Color4b.ORANGE)
    private val friendColor by color("Friends", Color4b.GREEN)

    internal val maximumDistance by float("MaximumDistance", 128F, 1F..512F)

    override fun onEnabled() {
        RenderedEntities.subscribe(this)
    }

    override fun onDisabled() {
        RenderedEntities.unsubscribe(this)
    }

    fun getColor(entity: LivingEntity): Color4b {
        if (entity.hurtTime > 0) {
            return Color4b.RED
        }

        if (entity.isInvisible) {
            return invisibleColor
        }

        if (entity is Player) {
            if (FriendManager.isFriend(entity) && friendColor.a > 0) {
                return friendColor
            }

            EntityTaggingManager.getTag(entity).color?.let { return it }
        }

        return colorModes.activeMode.getColor(entity)
    }

    /**
     * Check if the entity requires true sight to be shown with the current ESP mode
     */
    fun requiresTrueSight(entity: LivingEntity) =
        modes.activeMode.requiresTrueSight && entity.shouldBeShown()

}
