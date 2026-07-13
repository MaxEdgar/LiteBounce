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

package net.maxedgar.coffee.event.events

import net.maxedgar.coffee.annotations.Tag
import net.maxedgar.coffee.event.CancellableEvent
import net.maxedgar.coffee.event.Event
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffectInstance

@Tag("fps")
@Suppress("unused")
class FpsChangeEvent(val fps: Int) : Event()

@Tag("fpsLimit")
@Suppress("unused")
class FpsLimitEvent(var fps: Int) : Event()

@Tag("clientPlayerEffect")
@Suppress("unused")
class ClientPlayerEffectEvent(val effects: List<MobEffectInstance>) : Event()

sealed class TitleEvent : CancellableEvent() {
    sealed class TextContent : TitleEvent() {
        abstract var text: Component?
    }

    @Tag("title")
    class Title(override var text: Component?) : TextContent()

    @Tag("subtitle")
    class Subtitle(override var text: Component?) : TextContent()

    @Tag("titleFade")
    class Fade(var fadeInTicks: Int, var stayTicks: Int, var fadeOutTicks: Int) : TitleEvent()

    @Tag("clearTitle")
    class Clear(var reset: Boolean) : TitleEvent()
}
