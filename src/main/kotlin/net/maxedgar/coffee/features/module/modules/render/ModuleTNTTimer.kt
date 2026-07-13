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

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.ccbluex.fastutil.filterIsInstanceTo
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.computedOn
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.OverlayRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.render.gui.ItemStackListRenderer.drawItemStackList
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.render.WorldToScreen
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.text.plus
import net.maxedgar.coffee.utils.text.textOf
import net.minecraft.network.chat.Style
import net.minecraft.util.Mth
import net.minecraft.world.entity.item.PrimedTnt
import kotlin.math.sin

/**
 * TNTTimer module
 *
 * Highlight the active TNTs.
 */
object ModuleTNTTimer : ClientModule("TNTTimer", ModuleCategories.RENDER) {

    override val baseKey: String
        get() = "${ConfigSystem.KEY_PREFIX}.module.tntTimer"

    // Glow ESP
    val esp by boolean("ESP", true)

    private object ShowTimer : ToggleableValueGroup(this, "ShowTimer", false) {
        val scale by float("Scale", 1.5F, 0.25F..4F)
        val renderY by float("RenderY", 1.0F, -2.0F..2.0F)
        val ownerName by boolean("OwnerName", true)
        val timeUnit by enumChoice("TimeUnit", TimeUnit.TICKS)

        @Suppress("unused")
        private val render2DHandler = handler<OverlayRenderEvent> { event ->
            for (tnt in tntEntities) {
                val pos = tnt.boundingBox.center.add(0.0, renderY.toDouble(), 0.0)

                val screenPos = WorldToScreen.calculateScreenPos(pos) ?: continue

                // Yellow #ffff00 -> Red #ff0000
                val color = Color4b(255, Mth.floor(255F * tnt.fuse / DEFAULT_FUSE).coerceAtMost(255), 0)

                var text = timeUnit.format(tnt.fuse).asPlainText(Style.EMPTY + color)

                if (ownerName) {
                    tnt.owner?.name?.let {
                        text = textOf(
                            text,
                            " (".asPlainText(),
                            it,
                            ")".asPlainText(),
                        )
                    }
                }

                event.context.drawItemStackList(emptyList())
                    .centerX(screenPos.x)
                    .centerY(screenPos.y)
                    .title(text)
                    .scale(scale)
                    .draw()
            }
        }
    }

    init {
        tree(ShowTimer)
    }

    private const val DEFAULT_FUSE = 80

    /**
     * Cycle light periodically according to the remaining time (`fuse`). The less time left, the faster the cycle.
     */
    fun getTntColor(fuse: Int): Color4b {
        val red = Mth.floor(255.0 * (1.0 + 0.5 * sin(2400.0 / (12 + fuse)))).coerceIn(0, 255)
        return Color4b(red, 0, 0)
    }

    private val tntEntities by computedOn<GameTickEvent, MutableSet<PrimedTnt>>(ReferenceOpenHashSet()) { _, set ->
        set.clear()
        world.entitiesForRendering().filterIsInstanceTo(set) { it.fuse > 0 }
        set
    }

    override fun onDisabled() {
        tntEntities.clear()
        super.onDisabled()
    }

}
