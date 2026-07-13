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

import net.maxedgar.coffee.render.ClientRenderPipelines
import net.maxedgar.coffee.utils.client.inGame

/**
 * For [net.maxedgar.coffee.features.module.modules.render.ModuleBlockESP] outline mode.
 *
 * @see net.maxedgar.coffee.event.events.DrawOutlinesEvent
 */
@Deprecated("Disabled this feature")
object OutlineShaderRenderer : OverlayShaderRenderer(
    "Outline",
    ClientRenderPipelines.Outline,
    useDepth = false,
) {
    override fun shouldRender(): Boolean {
        return inGame
    }
}
