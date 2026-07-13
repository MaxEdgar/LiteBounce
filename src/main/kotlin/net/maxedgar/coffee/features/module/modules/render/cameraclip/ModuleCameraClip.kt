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

package net.maxedgar.coffee.features.module.modules.render.cameraclip

import com.mojang.blaze3d.platform.InputConstants
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.KeyboardKeyEvent
import net.maxedgar.coffee.event.events.PerspectiveEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.render.ModuleFreeLook
import net.maxedgar.coffee.utils.input.isPressed
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention
import net.maxedgar.coffee.utils.math.Easing
import net.minecraft.client.CameraType
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW

/**
 * CameraClip module
 *
 * Allows you to see through walls in third person view.
 *
 * @author 1zun4, sqlerrorthing
 */
object ModuleCameraClip : ClientModule("CameraClip", ModuleCategories.RENDER) {
    private val cameraDistance = float("CameraDistance", 4f, 1f..48f)

    init {
        tree(Animation)
        tree(ScrollAdjust)
    }

    val distance
        get() = when {
            ScrollAdjust.running -> ScrollAdjust.scrolledDistance
            else -> cameraDistance.get()
        }

    @Suppress("unused")
    private val perspectiveHandler = handler<PerspectiveEvent> { event ->
        event.noClip = true
    }

    private object Animation: ToggleableValueGroup(this, "Animation", true) {
        private val speed by float("Speed", 0.6f, 0.1f..1f)
        private val easing by easing("Easing", Easing.LINEAR)

        @Suppress("unused")
        private val perspectiveHandler = handler<PerspectiveEvent> { event ->
            if (event.lastPerspective != event.perspective) {
                event.lastDistance = 0f
            }
            if (event.perspective != CameraType.FIRST_PERSON) {
                event.distance = Mth.lerp(easing.transform(speed), event.lastDistance, distance)
            }
        }
    }

    private object ScrollAdjust : ScrollAdjustValueGroup(
        ModuleCameraClip,
        "ScrollAdjust",
        true,
        { delta -> ScrollAdjust.scrolledDistance += delta },
        ScrollAdjustOptions(
            modifierKeyDefault = GLFW.GLFW_KEY_LEFT_CONTROL,
            sensitivityDefault = 0.3f,
            sensitivityRange = 0.1f..2f
        )
    ) {
        private val rememberScrolled by boolean("RememberScrolled", false)
        private val requireFreeLook by boolean("RequireFreeLook", false)

        var scrolledDistance = cameraDistance.get()
            private set(value) {
                @Suppress("UNCHECKED_CAST")
                field = value.coerceIn(cameraDistance.range as ClosedFloatingPointRange<Float>)
            }

        override fun canPerformScroll(): Boolean =
            (modifierKey == InputConstants.UNKNOWN || modifierKey.isPressed)
                && (!requireFreeLook || ModuleFreeLook.running)
                && (mc.options.cameraType != CameraType.FIRST_PERSON || ModuleFreeLook.running)

        @Suppress("unused")
        private val resetHandler = handler<PerspectiveEvent>(
            priority = EventPriorityConvention.READ_FINAL_STATE
        ) { event ->
            if (event.perspective == CameraType.FIRST_PERSON) {
                reset()
            }
        }

        @Suppress("unused")
        private val releaseModifierHandler = handler<KeyboardKeyEvent> {
            if (it.key == modifierKey && it.action == GLFW.GLFW_RELEASE) {
                reset()
            }
        }

        fun reset() {
            if (rememberScrolled && scrolledDistance != cameraDistance.get()) {
                cameraDistance.set(scrolledDistance)
            } else {
                scrolledDistance = cameraDistance.get()
            }
        }

        override fun onEnabled() {
            reset()
        }
    }
}
