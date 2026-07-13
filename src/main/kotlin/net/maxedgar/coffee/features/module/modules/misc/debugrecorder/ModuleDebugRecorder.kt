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
package net.maxedgar.coffee.features.module.modules.misc.debugrecorder

import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.gson.adapter.toUnderlinedString
import net.maxedgar.coffee.config.gson.publicGson
import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.misc.debugrecorder.modes.AimDebugRecorder
import net.maxedgar.coffee.features.module.modules.misc.debugrecorder.modes.BoxDebugRecorder
import net.maxedgar.coffee.features.module.modules.misc.debugrecorder.modes.DebugCPSRecorder
import net.maxedgar.coffee.features.module.modules.misc.debugrecorder.modes.GenericDebugRecorder
import net.maxedgar.coffee.utils.text.asText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.onClick
import net.maxedgar.coffee.utils.client.onHover
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.underline
import net.maxedgar.coffee.utils.client.variable
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import java.time.LocalDateTime

object ModuleDebugRecorder : ClientModule("DebugRecorder", ModuleCategories.MISC, disableOnQuit = true) {

    init {
        // [Debug Recorder] is usually used by developers and testers and is not needed in the auto config.
        doNotIncludeAlways()
    }

    val modes = choices("Mode", GenericDebugRecorder, arrayOf(
        GenericDebugRecorder,
        DebugCPSRecorder,
        AimDebugRecorder,
        BoxDebugRecorder
    ))

    abstract class DebugRecorderMode<T>(name: String) : Mode(name) {
        override val parent: ModeValueGroup<*>
            get() = modes

        val folder = ConfigSystem.rootFolder.resolve("debug-recorder/$name").apply {
            mkdirs()
        }
        internal val packets = mutableListOf<T>()

        protected fun recordPacket(packet: T) {
            if (!this.isSelected) {
                return
            }

            packets.add(packet)
        }

        override fun enable() {
            this.packets.clear()
            chat(regular("Recording "), variable(name), regular("..."))
        }

        override fun disable() {
            if (this.packets.isEmpty()) {
                chat(regular("No packets recorded."))
                return
            }

            runCatching {
                // Create parent folder
                folder.mkdirs()

                val baseName = LocalDateTime.now().toUnderlinedString()
                var file = folder.resolve("${baseName}.json")

                var idx = 0
                while (file.exists()) {
                    file = folder.resolve("${baseName}_${idx++}.json")
                }

                file.bufferedWriter().use { writer ->
                    publicGson.toJson(this.packets, writer)
                }
                file.absolutePath
            }.onFailure {
                chat(markAsError("Failed to write log to file $it"))
            }.onSuccess { path ->
                val text = path.asText()
                    .underline(true)
                    .onHover(HoverEvent.ShowText(regular("Browse...")))
                    .onClick(ClickEvent.OpenFile(path))

                chat(regular("Log was written to "), text, regular("."))
            }

            this.packets.clear()
        }
    }
}
