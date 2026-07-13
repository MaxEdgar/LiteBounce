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
package net.maxedgar.coffee.config.autoconfig

import com.google.gson.JsonObject
import net.maxedgar.coffee.Coffee
import net.maxedgar.coffee.api.models.client.AutoSettings
import net.maxedgar.coffee.api.services.client.ClientApi
import net.maxedgar.coffee.api.types.enums.AutoSettingsStatusType
import net.maxedgar.coffee.api.types.enums.AutoSettingsType
import net.maxedgar.coffee.authlib.utils.obj
import net.maxedgar.coffee.authlib.utils.string
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.ConfigSystem.deserializeValueGroup
import net.maxedgar.coffee.config.gson.publicGson
import net.maxedgar.coffee.config.gson.util.parseTree
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.features.module.ModuleManager
import net.maxedgar.coffee.features.module.modules.render.ModuleClickGui
import net.maxedgar.coffee.features.spoofer.SpooferManager
import net.maxedgar.coffee.utils.client.MessageMetadata
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.text.dropPort
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.utils.client.markAsError
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.text.plus
import net.maxedgar.coffee.utils.client.protocolVersion
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.text.rootDomain
import net.maxedgar.coffee.utils.client.selectProtocolVersion
import net.maxedgar.coffee.utils.client.usesViaFabricPlus
import net.maxedgar.coffee.utils.client.variable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Style
import java.io.Reader
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date

object AutoConfig {

    @Volatile
    var loadingNow = false
        set(value) {
            field = value

            // After completion of loading, sync ClickGUI
            if (!value) {
                ModuleClickGui.sync()
            }
        }

    var includeConfiguration = IncludeConfiguration.DEFAULT

    @Volatile
    var configs: Array<AutoSettings>? = null
        private set

    /**
     * Reloads auto settings list.
     *
     * @return successfully reloaded or not
     */
    suspend fun reloadConfigs(): Boolean = try {
        configs = ClientApi.requestSettingsList()
        true
    } catch (e: Exception) {
        logger.error("Failed to load auto configs", e)
        false
    }

    inline fun withLoading(block: () -> Unit) {
        loadingNow = true
        try {
            block()
        } finally {
            loadingNow = false
        }
    }

    suspend fun loadAutoConfig(autoConfig: AutoSettings) = withLoading {
        ClientApi.requestSettingsScript(autoConfig.settingId).use(::loadAutoConfig)
    }

    /**
     * Deserialize module configurable from a reader
     */
    fun loadAutoConfig(
        reader: Reader,
        modules: Collection<ValueGroup> = emptyList()
    ) {
        publicGson.newJsonReader(reader).use { reader ->
            loadAutoConfig(reader.parseTree().asJsonObject, modules)
        }
    }

    /**
     * Handles the data from a configurable, which might be an auto config and therefore has data which
     * should be displayed to the user.
     *
     * @param jsonObject The JSON object of the configurable
     * @see deserializeValueGroup
     */
    fun loadAutoConfig(
        jsonObject: JsonObject,
        modules: Collection<ValueGroup> = emptyList()
    ) {
        chat(metadata = MessageMetadata(prefix = false))
        chat("Auto Config".asPlainText(Style.EMPTY + ChatFormatting.LIGHT_PURPLE + ChatFormatting.BOLD))

        val name = jsonObject.string("name") ?: throw IllegalArgumentException("Auto Config has no name")
        when (name) {
            "autoconfig" -> {
                // Deserialize Module Configurable
                jsonObject.obj("modules")?.let { moduleObject ->
                    deserializeModuleValueGroup(moduleObject, modules)
                }

                // Deserialize Spoofer Configurable
                jsonObject.obj("spoofers")?.let { spooferObject ->
                    deserializeValueGroup(SpooferManager, spooferObject)
                }
            }
            "modules" -> deserializeModuleValueGroup(jsonObject, modules)
            else -> error("Unknown auto config type: $name")
        }

        // Auto Config
        printOutMetadata(jsonObject)
    }

    /**
     * Print out information from the auto config
     */
    private fun printOutMetadata(jsonObject: JsonObject) {
        val metadata = publicGson.fromJson(jsonObject, AutoConfigMetadata::class.java)

        val serverAddress = metadata.serverAddress
        if (serverAddress != null) {
            chat(
                regular("for server "),
                variable(serverAddress)
            )
        }

        val pName = metadata.protocolName
        val pVersion = metadata.protocolVersion

        if (pName != null && pVersion != null) {
            formatAutoConfigProtocolInfo(pVersion, pName)
        }

        val date = metadata.date
        val time = metadata.time
        val author = metadata.author
        val lbVersion = metadata.clientVersion
        val lbCommit = metadata.clientCommit

        if (date != null || time != null) {
            chat(
                regular("on "),
                variable(if (!date.isNullOrBlank()) "$date " else ""),
                variable(if (!time.isNullOrBlank()) time else "")
            )
        }

        if (author != null) {
            chat(
                regular("by "),
                variable(author)
            )
        }

        if (lbVersion != null) {
            chat(
                regular("with Coffee "),
                variable(lbVersion),
                regular(" "),
                variable(lbCommit ?: "")
            )
        }

        metadata.chat?.let { chatMessages ->
            for (messages in chatMessages) {
                chat(messages)
            }
        }
    }

    private fun formatAutoConfigProtocolInfo(pVersion: Int, pName: String) {
        // Check if the protocol is identical
        val (protocolName, protocolVersion) = protocolVersion

        // Give user notification about the protocol of the config and his current protocol.
        // If they are not identical, make the message red and bold to make it more visible.
        // If the protocol is identical, make the message green to make it more visible
        val matchesVersion = protocolVersion == pVersion

        chat(
            regular("for protocol "),
            variable("$pName $pVersion")
                .withStyle {
                    if (!matchesVersion) {
                        it.applyFormats(ChatFormatting.RED, ChatFormatting.BOLD)
                    } else {
                        it.applyFormat(ChatFormatting.GREEN)
                    }
                },
            regular(" and your current protocol is "),
            variable("$protocolName $protocolVersion")
        )

        if (!matchesVersion) {
            notification(
                "Auto Config",
                "The auto config was made for protocol $pName, " +
                    "but your current protocol is $protocolName",
                NotificationEvent.Severity.ERROR
            )

            if (usesViaFabricPlus) {
                if (inGame) {
                    chat(markAsError("Please reconnect to the server to apply the correct protocol."))
                } else {
                    selectProtocolVersion(pVersion)
                }
            } else {
                chat(markAsError("Please install ViaFabricPlus to apply the correct protocol."))
            }
        }
    }

    /**
     * Created an auto config, which stores the moduleConfigur
     */
    fun serializeAutoConfig(
        writer: Writer,
        includeConfiguration: IncludeConfiguration = IncludeConfiguration.DEFAULT,
        autoSettingsType: AutoSettingsType = AutoSettingsType.RAGE,
        statusType: AutoSettingsStatusType = AutoSettingsStatusType.BYPASSING
    ) {
        this.includeConfiguration = includeConfiguration

        // Store the config
        val moduleTree = ConfigSystem.serializeValueGroup(ModuleManager.modulesConfig, publicGson)
        val spooferTree = ConfigSystem.serializeValueGroup(SpooferManager, publicGson)

        if (!moduleTree.isJsonObject || !spooferTree.isJsonObject) {
            error("Root element is not a json object")
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("name", "autoconfig")

        jsonObject.add("modules", moduleTree.asJsonObject)
        jsonObject.add("spoofers", spooferTree.asJsonObject)

        val author = mc.user.name

        val now = Date()
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
        val timeFormatter = SimpleDateFormat("HH:mm:ss")
        val date = dateFormatter.format(now)
        val time = timeFormatter.format(now)

        val (protocolName, protocolVersion) = protocolVersion

        jsonObject.addProperty("author", author)
        jsonObject.addProperty("date", date)
        jsonObject.addProperty("time", time)
        jsonObject.addProperty("clientVersion", Coffee.clientVersion)
        jsonObject.addProperty("clientCommit", Coffee.clientCommit)
        mc.currentServer?.let {
            jsonObject.addProperty("serverAddress", it.ip.dropPort().rootDomain())
        }
        jsonObject.addProperty("protocolName", protocolName)
        jsonObject.addProperty("protocolVersion", protocolVersion)

        jsonObject.add("type", publicGson.toJsonTree(autoSettingsType))
        jsonObject.add("status", publicGson.toJsonTree(statusType))

        publicGson.newJsonWriter(writer).use {
            publicGson.toJson(jsonObject, it)
        }

        this.includeConfiguration = IncludeConfiguration.DEFAULT
    }

    /**
     * Deserialize module configurable from a JSON object
     */
    private fun deserializeModuleValueGroup(
        jsonObject: JsonObject,
        modules: Collection<ValueGroup> = emptyList()
    ) {
        // Deserialize full module configurable
        if (modules.isEmpty()) {
            deserializeValueGroup(ModuleManager.modulesConfig, jsonObject)
            return
        }

        modules.forEach { module ->
            val moduleValueGroup = ModuleManager.modulesConfig.inner.find { value ->
                value.name == module.name
            } as? ValueGroup ?: return@forEach

            val moduleElement = jsonObject.asJsonObject["value"].asJsonArray.find { jsonElement ->
                jsonElement.asJsonObject["name"].asString == module.name
            } ?: return@forEach

            deserializeValueGroup(moduleValueGroup, moduleElement)
        }
    }

}
