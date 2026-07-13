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
package net.maxedgar.coffee.config.gson.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import java.lang.reflect.Type

class ModeValueGroupSerializer private constructor(
    private val withValueType: Boolean,
) : JsonSerializer<ModeValueGroup<Mode>> {

    override fun serialize(
        src: ModeValueGroup<Mode>, typeOfSrc: Type, context: JsonSerializationContext
    ): JsonElement {
        val obj = JsonObject()

        obj.addProperty("name", src.name)
        obj.addProperty("active", src.activeMode.tag)
        obj.add("value", context.serialize(src.inner))

        val choices = JsonObject()

        for (choice in src.modes) {
            choices.add(choice.name, context.serialize(choice))
        }

        obj.add("choices", choices)
        if (withValueType) {
            obj.add("valueType", context.serialize(src.valueType))
        }

        return obj
    }

    companion object {
        @JvmField
        val FILE_SERIALIZER = ModeValueGroupSerializer(withValueType = false)
    }

}
