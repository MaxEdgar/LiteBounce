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

package net.maxedgar.coffee.features.command.commands.client

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.maxedgar.coffee.Coffee
import net.maxedgar.coffee.api.core.HttpClient
import net.maxedgar.coffee.api.core.HttpMethod
import net.maxedgar.coffee.api.core.asForm
import net.maxedgar.coffee.api.core.parse
import net.maxedgar.coffee.config.autoconfig.AutoConfig.serializeAutoConfig
import net.maxedgar.coffee.config.gson.publicGson
import net.maxedgar.coffee.config.gson.serializer.minecraft.accountType
import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.CommandExecutor.suspendHandler
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.global.GlobalSettingsTarget
import net.maxedgar.coffee.features.module.ModuleManager
import net.maxedgar.coffee.lang.LanguageManager
import net.maxedgar.coffee.script.ScriptManager
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.text.plus
import net.maxedgar.coffee.utils.client.usesViaFabricPlus
import net.minecraft.ChatFormatting
import net.minecraft.SharedConstants
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Style
import java.net.URI
import java.util.EnumSet

/**
 * Debug Command to collect information about the client
 * in order to help developers to fix bugs or help users
 * with their issues.
 *
 * This command will create a JSON file with all the information
 * and send it to the CCBlueX Paste API.
 */
object CommandDebug : Command.Factory {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    override fun createCommand() = CommandBuilder.begin("debug")
        .suspendHandler {
            chat("§7Collecting debug information...")

            val buffer = okio.Buffer()

            serializeAutoConfig(buffer.outputStream().writer())
            val autoConfig = buffer.readUtf8()
            val autoConfigPaste = uploadToPaste(autoConfig)
            buffer.clear()

            val debugJson = createDebugJson(autoConfigPaste)
            gson.toJson(debugJson, buffer.outputStream().writer())
            val content = buffer.readUtf8()
            val paste = uploadToPaste(content)
            buffer.clear()

            chat(
                "Debug information has been uploaded to: ".asPlainText(ChatFormatting.GREEN),
                paste.asPlainText(Style.EMPTY + ChatFormatting.YELLOW + ClickEvent.OpenUrl(URI(paste))),
            )
        }
        .build()

    @Suppress("LongMethod")
    private fun createDebugJson(
        autoConfigPaste: String
    ) = JsonObject().apply {
        add("client", JsonObject().apply {
            addProperty("name", Coffee.CLIENT_NAME)
            addProperty("version", Coffee.clientVersion)
            addProperty("commit", Coffee.clientCommit)
            addProperty("branch", Coffee.clientBranch)
            addProperty("development", Coffee.IN_DEVELOPMENT)
            addProperty("usesViaFabricPlus", usesViaFabricPlus)
        })

        add("minecraft", JsonObject().apply {
            addProperty("version", SharedConstants.getCurrentVersion().name())
            addProperty("protocol", SharedConstants.getProtocolVersion())
        })

        add("java", JsonObject().apply {
            addProperty("version", System.getProperty("java.version"))
            addProperty("vendor", System.getProperty("java.vendor"))
        })

        add("os", JsonObject().apply {
            addProperty("name", System.getProperty("os.name"))
            addProperty("version", System.getProperty("os.version"))
            addProperty("architecture", System.getProperty("os.arch"))
        })

        add("user", JsonObject().apply {
            addProperty("language", System.getProperty("user.language"))
            addProperty("country", System.getProperty("user.country"))
            addProperty("timezone", System.getProperty("user.timezone"))
        })

        add("profile", JsonObject().apply {
            addProperty("name", mc.user.name)
            addProperty("uuid", mc.user.profileId.toString())
            addProperty("type", mc.user.accountType)
        })

        add("language", JsonObject().apply {
            addProperty("language", mc.languageManager.selected)
            addProperty("clientLanguage", LanguageManager.clientLanguage.tag)
        })

        add("server", JsonObject().apply {
            mc.currentServer?.let {
                addProperty("name", it.name)
                addProperty("address", it.ip)
                addProperty("protocol", it.protocol)
            }
        })

        addProperty("config", autoConfigPaste)

        add("activeModules", JsonArray().apply {
            ModuleManager.filter { it.running }.forEach { module ->
                add(JsonPrimitive(module.name))
            }
        })

        add("scripts", JsonArray().apply {
            ScriptManager.scripts.forEach { script ->
                add(JsonObject().apply {
                    addProperty("name", script.scriptName)
                    addProperty("version", script.scriptVersion)
                    addProperty("author", script.scriptAuthors.joinToString(", "))
                    addProperty("path", script.file.path)
                })
            }
        })

        add("enemies", publicGson.toJsonTree(GlobalSettingsTarget.combat, EnumSet::class.javaObjectType))
    }

    /**
     * Uploads the given content to the CCBlueX Paste API
     * and returns the URL of the paste.
     */
    private suspend fun uploadToPaste(content: String): String {
        val form = "content=$content"
        return HttpClient.request("https://paste.ccbluex.net/api.php", HttpMethod.POST, body = form.asForm())
                .parse<String>()
    }

}
