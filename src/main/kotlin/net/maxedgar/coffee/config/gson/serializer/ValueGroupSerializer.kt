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

import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.maxedgar.coffee.config.types.Value
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.utils.text.toLowerCamelCase
import net.maxedgar.coffee.utils.render.Alignment
import java.lang.reflect.Type

class ValueGroupSerializer(
    private val withValueType: Boolean, private val includePrivate: Boolean, private val includeNotAnOption: Boolean
) : JsonSerializer<ValueGroup> {

    companion object {

        /**
         * This serializer is used to serialize [ValueGroup]s to JSON
         */
        @JvmField
        val FILE_SERIALIZER = ValueGroupSerializer(
            withValueType = false, includePrivate = true, includeNotAnOption = true
        )

        /**
         * This serializer is used to serialize [ValueGroup]s to JSON for public config
         */
        @JvmField
        val PUBLIC_SERIALIZER = ValueGroupSerializer(
            withValueType = false, includePrivate = false, includeNotAnOption = true
        )

        @JvmStatic
        fun serializeReadOnly(
            valueGroup: ValueGroup,
            context: JsonSerializationContext
        ): JsonObject = JsonObject().apply {
            for (v in valueGroup.inner) {
                add(v.name.toLowerCamelCase(), when (v) {
                    is Alignment -> context.serialize(v, Alignment::class.java)
                    is ValueGroup -> serializeReadOnly(v, context)
                    else -> context.serialize(v.inner)
                })
            }
        }

    }

    override fun serialize(
        src: ValueGroup, typeOfSrc: Type, context: JsonSerializationContext
    ) = JsonObject().apply {
        addProperty("name", src.name)
        try {

            add(
                "value",
                context.serialize(
                    src.inner.filter { includeNotAnOption || !it.notAnOption }
                        .filter {
                            includePrivate || checkIfInclude(it)
                        }
                )
            )
        } catch (e: Exception) {
            logger.error("failed to serialize config for ${src.name}")
            throw e
        }
        if (withValueType) {
            add("valueType", context.serialize(src.valueType))
        }
    }

    /**
     * Checks if value should be included in public config
     */
    private fun checkIfInclude(value: Value<*>): Boolean {
        /**
         * Do not include values that are not supposed to be shared
         * with other users
         */
        if (value.doNotInclude.asBoolean) {
            return false
        }

        // Might check if value is module
        if (value is ClientModule) {
            /**
             * Do not include modules that are heavily user-personalised
             */
            if (value.category == ModuleCategories.RENDER || value.category == ModuleCategories.FUN) {
                return false
            }
        }

        // Otherwise include value
        return true
    }

}
