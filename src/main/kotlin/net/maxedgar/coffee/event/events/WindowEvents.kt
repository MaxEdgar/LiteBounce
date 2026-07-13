/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
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

import com.mojang.blaze3d.platform.InputConstants
import net.maxedgar.coffee.annotations.Tag
import net.maxedgar.coffee.event.CancellableEvent
import net.maxedgar.coffee.event.Event
import net.minecraft.client.gui.screens.Screen

@Tag("windowResize")
class WindowResizeEvent(val width: Int, val height: Int) : Event()

@Tag("frameBufferResize")
class FramebufferResizeEvent(val width: Int, val height: Int) : Event()

@Tag("mouseButton")
class MouseButtonEvent(
    val key: InputConstants.Key,
    val button: Int,
    val action: Int,
    val mods: Int,
    val screen: Screen? = null
) : Event()

@Tag("mouseScroll")
class MouseScrollEvent(val horizontal: Double, val vertical: Double) : Event()

@Tag("mouseScrollInHotbar")
class MouseScrollInHotbarEvent(val speed: Int) : CancellableEvent()

@Tag("mouseCursor")
class MouseCursorEvent(val x: Double, val y: Double) : Event()

@Tag("keyboardKey")
class KeyboardKeyEvent(
    val key: InputConstants.Key,
    val keyCode: Int,
    val scanCode: Int,
    val action: Int,
    val mods: Int,
    val screen: Screen? = null
) : Event()

@Tag("keyboardChar")
class KeyboardCharEvent(val codePoint: Int) : Event()
