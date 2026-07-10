/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.render.clickgui

import net.ccbluex.liquidbounce.config.ConfigSystem
import net.ccbluex.liquidbounce.config.types.BindValue
import net.ccbluex.liquidbounce.config.types.RangedValue
import net.ccbluex.liquidbounce.config.types.Value
import net.ccbluex.liquidbounce.config.types.ValueType
import net.ccbluex.liquidbounce.config.types.group.ModeValueGroup
import net.ccbluex.liquidbounce.config.types.group.ToggleableValueGroup
import net.ccbluex.liquidbounce.config.types.group.ValueGroup
import net.ccbluex.liquidbounce.config.types.list.ChoiceListValue
import net.ccbluex.liquidbounce.config.types.list.MultiChoiceListValue
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.render.clickgui.components.*
import net.ccbluex.liquidbounce.render.engine.type.Color4b
import net.minecraft.client.Minecraft
import java.io.File

/**
 * Singleton that manages the native ClickGUI instance.
 * This bridges Wurst7's ClickGUI architecture with LiquidBounce's module system.
 */
object NativeClickGui {

    val gui: ClickGui by lazy {
        val windowsFile = File(ConfigSystem.rootFolder, "clickgui_windows.json").toPath()
        ClickGui(windowsFile).also { it.init() }
    }

    @JvmStatic
    fun open() {
        Minecraft.getInstance().gui.setScreen(ClickGuiScreen(gui))
    }

    /**
     * Creates the appropriate ClickGUI component for a given LiquidBounce value.
     * Handles all standard value types that LiquidBounce uses.
     */
    @JvmStatic
    fun createComponent(value: Value<*>): Component? {
        return when {
            // Boolean values
            value.valueType == ValueType.BOOLEAN -> BooleanComponent(value as Value<Boolean>)

            // Float range values
            value is RangedValue<*> && value.valueType == ValueType.FLOAT ->
                FloatComponent(value as RangedValue<Float>)

            // Float range values (float range type)
            value is RangedValue<*> && value.valueType == ValueType.FLOAT_RANGE ->
                FloatRangeComponent(value as RangedValue<ClosedFloatingPointRange<Float>>)

            // Integer range values
            value is RangedValue<*> && value.valueType == ValueType.INT ->
                IntComponent(value as RangedValue<Int>)

            // Integer range pair values
            value is RangedValue<*> && value.valueType == ValueType.INT_RANGE ->
                IntRangeComponent(value as RangedValue<IntRange>)

            // Enum/choice values
            value is ChoiceListValue<*> -> EnumComponent(value)

            // Multi-enum/choice values
            value is MultiChoiceListValue<*> -> MultiEnumComponent(value)

            // String/text values
            value.valueType == ValueType.TEXT -> TextComponent(value as Value<String>)

            // Color values
            value.valueType == ValueType.COLOR -> ColorComponent(value as Value<Color4b>)

            // Keybind values
            value is BindValue -> BindComponent(value)

            // Key values
            value.valueType == ValueType.KEY -> BindComponent(value)

            // Configurable groups - show as group label
            value is ToggleableValueGroup -> ToggleableGroupComponent(value)

            // Mode value groups - show current mode
            value is ModeValueGroup<*> -> ModeGroupComponent(value)

            // Nested value groups - recursively add children
            value is ValueGroup -> null // Skip containers, their children will be added separately

            else -> null
        }
    }

    /**
     * Recursively adds all leaf values from a ValueGroup as components to a Window.
     */
    @JvmStatic
    fun addSettingsToWindow(window: Window, module: ClientModule) {
        for (value in module.inner) {
            when (value) {
                is ToggleableValueGroup -> {
                    // Add a toggleable group component, then recursively add its children
                    val groupComponent = ToggleableGroupComponent(value)
                    window.add(groupComponent)
                    for (childValue in value.inner) {
                        val childComponent = createComponent(childValue)
                        if (childComponent != null) {
                            window.add(childComponent)
                        }
                    }
                }
                is ModeValueGroup<*> -> {
                    val modeComponent = ModeGroupComponent(value)
                    window.add(modeComponent)
                    // Add the active mode's settings
                    val activeMode = value.activeMode
                    for (childValue in activeMode.inner) {
                        val childComponent = createComponent(childValue)
                        if (childComponent != null) {
                            window.add(childComponent)
                        }
                    }
                }
                is ValueGroup -> {
                    // Recursively handle nested groups
                    addSettingsToWindow(window, value)
                }
                else -> {
                    val component = createComponent(value)
                    if (component != null) {
                        window.add(component)
                    }
                }
            }
        }
    }

    @JvmStatic
    private fun addSettingsToWindow(window: Window, group: ValueGroup) {
        for (value in group.inner) {
            when (value) {
                is ToggleableValueGroup -> {
                    window.add(ToggleableGroupComponent(value))
                    for (childValue in value.inner) {
                        val component = createComponent(childValue)
                        if (component != null) window.add(component)
                    }
                }
                is ModeValueGroup<*> -> {
                    window.add(ModeGroupComponent(value))
                }
                is ValueGroup -> addSettingsToWindow(window, value)
                else -> {
                    val component = createComponent(value)
                    if (component != null) window.add(component)
                }
            }
        }
    }
}
