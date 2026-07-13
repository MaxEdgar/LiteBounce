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
package net.maxedgar.coffee.features.module.modules.misc

import net.maxedgar.coffee.api.thirdparty.translator.TranslationResult
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.TitleEvent
import net.maxedgar.coffee.event.suspendHandler
import net.maxedgar.coffee.features.global.GlobalSettingsAutoTranslate
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.highlight
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.text.stripMinecraftColorCodes
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Gui
import net.minecraft.network.chat.Component

object ModuleBetterTitle : ClientModule(
    "BetterTitle", ModuleCategories.RENDER, aliases = listOf("BetterSubtitle")
) {
    init {
        tree(AutoTranslate)
    }
}

private object AutoTranslate : ToggleableValueGroup(ModuleBetterTitle, "AutoTranslate", false) {
    private val components by multiEnumChoice("Components", TitleType.entries)
    private val showIn by multiEnumChoice("ShowIn", ShowIn.CHAT)

    private inline fun <reified E : TitleEvent.TextContent> translatorHandler(
        type: TitleType
    ) = suspendHandler<E> { event ->
        if (type !in components) {
            return@suspendHandler
        }

        val string = event.text
            ?.string
            ?.stripMinecraftColorCodes()
            ?.takeUnless(String::isBlank)
            ?: return@suspendHandler

        val result = GlobalSettingsAutoTranslate.translate(text = string)
        if (result.isValid && result is TranslationResult.Success) {
            showIn.forEach { it.show(type, event, result) }
        }
    }

    @Suppress("unused")
    private val titleHandler = translatorHandler<TitleEvent.Title>(TitleType.TITLE)

    @Suppress("unused")
    private val subtitleHandler =
        translatorHandler<TitleEvent.Subtitle>(TitleType.SUBTITLE)
}

@Suppress("unused")
private enum class ShowIn(
    override val tag: String,
    val show: (TitleType, TitleEvent.TextContent, TranslationResult.Success) -> Unit
) : Tagged {
    CHAT("Chat", { type, _, result ->
        chat(
            highlight(type.tag),
            regular(": "),
            result.toResultText(),
        )
    }),
    MESSAGE("Message", { type, event, result ->
        result.translation.asPlainText(ChatFormatting.WHITE).let {
            event.text = it
            type.setText(it)
        }
    })
}


private enum class TitleType(
    override val tag: String,
    /**
     * Doesn't use [Gui.setTitle] and [Gui.setSubtitle] because
     * this will cause reset of the stayIn timer
     */
    val setText: (Component) -> Unit
) : Tagged {
    TITLE("Title", {
        mc.gui.hud.setTitle(it)
    }),
    SUBTITLE("Subtitle", {
        mc.gui.hud.setSubtitle(it)
    })
}
