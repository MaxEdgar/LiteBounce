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

package net.maxedgar.coffee.config.types

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.maxedgar.coffee.api.core.ioScope
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.utils.kotlin.Minecraft
import net.maxedgar.coffee.utils.render.asTexture
import net.maxedgar.coffee.utils.render.readNativeImage
import net.minecraft.ChatFormatting
import net.minecraft.client.renderer.texture.DynamicTexture
import kotlin.properties.ReadOnlyProperty
import kotlin.time.Duration.Companion.seconds


/**
 * Convert the [FileValue] to a [ReadOnlyProperty] of [DynamicTexture].
 */
fun <V> FileValue.toTextureProperty(
    owner: V,
    printErrorToChat: Boolean = true,
): ReadOnlyProperty<Any?, DynamicTexture?> where V : EventListener, V : Value<*> {
    var texture: DynamicTexture? = null
    ioScope.launch {
        asStateFlow().filter { it.isFile }.collectLatest { file ->
            while (!inGame || !owner.running) {
                delay(1.seconds)
            }

            try {
                val nativeImage = file.readNativeImage()
                withContext(Dispatchers.Minecraft) {
                    texture = nativeImage.asTexture("(${owner.name}) File texture: ${file.name}")
                }
            } catch (e: Exception) {
                val message = "Failed to load texture from '${file.name}' for ${owner.name}"
                if (owner.running && printErrorToChat) {
                    chat("$message (${e.javaClass.simpleName})".asPlainText(ChatFormatting.RED))
                }
                logger.error(message, e)
            }
        }
    }

    return ReadOnlyProperty { _, _ -> texture }
}
