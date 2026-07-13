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
package net.maxedgar.coffee.utils.client

import net.maxedgar.coffee.Coffee.CLIENT_NAME
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Get a [Logger] with client name prefix
 */
internal fun clientLogger(name: String): Logger = LogManager.getLogger("$CLIENT_NAME/$name")

val logger: Logger = LogManager.getLogger(CLIENT_NAME)

val inGame: Boolean
    get() = Minecraft.getInstance()?.let { mc -> mc.player != null && mc.level != null } == true

inline val clientStartDurationMs: Long
    get() = System.currentTimeMillis() - mc.clientStartTimeMs

/**
 * Open uri in browser
 */
fun browseUrl(url: String) = Util.getPlatform().openUri(url)

/**
 * Get environment variable or system property.
 */
fun env(name: String, property: String) =
    (System.getenv(name) ?: System.getProperty(property))?.takeIf { string -> string.isNotBlank() }
