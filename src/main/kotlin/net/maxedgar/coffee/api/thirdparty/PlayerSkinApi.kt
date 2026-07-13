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

package net.maxedgar.coffee.api.thirdparty

import com.google.gson.JsonObject
import net.maxedgar.coffee.api.core.BaseApi
import net.maxedgar.coffee.api.core.HttpClient
import net.maxedgar.coffee.authlib.utils.toRequestBody
import net.maxedgar.coffee.config.gson.serializer.minecraft.accountType
import net.maxedgar.coffee.utils.client.mc
import net.minecraft.world.entity.player.PlayerModelType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PlayerSkinApi(serviceHost: String) : BaseApi(serviceHost) {

    private suspend fun uploadSkin(body: RequestBody) {
        val session = mc.user
        require(session.accountType != "legacy") {
            "Legacy account can't use this API"
        }

        post<Unit>("/minecraft/profile/skins", body) {
            add("Authorization", "Bearer ${session.accessToken}")
        }
    }

    suspend fun changeSkin(url: String, model: PlayerModelType) {
        // https://minecraft.wiki/w/Mojang_API#Change_skin
        val jsonBody = JsonObject().apply {
            addProperty("url", url)
            addProperty("variant", model.variant)
        }.toRequestBody()

        uploadSkin(jsonBody)
    }

    suspend fun uploadSkin(file: File, model: PlayerModelType) {
        // https://minecraft.wiki/w/Mojang_API#Upload_skin
        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("variant", model.variant)
            .addFormDataPart(
                name = "file",
                filename = "skin.png",
                body = file.asRequestBody(HttpClient.MediaTypes.IMAGE_PNG)
            )
            .build()

        uploadSkin(formBody)
    }

    private val PlayerModelType.variant
        get() = when (this) {
            PlayerModelType.WIDE -> "classic"
            PlayerModelType.SLIM -> "slim"
    }

}
