/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.features.misc

import com.mojang.blaze3d.platform.IconSet
import com.terraformersmc.modmenu.util.mod.Mod
import kotlinx.coroutines.cancel
import net.maxedgar.coffee.api.core.ioScope
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.EventManager
import net.maxedgar.coffee.event.EventManager.callEvent
import net.maxedgar.coffee.event.events.ClientShutdownEvent
import net.maxedgar.coffee.event.events.KeyboardKeyEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.command.CommandManager
import net.maxedgar.coffee.features.misc.HideAppearance.isHidingNow
import net.maxedgar.coffee.features.module.ModuleManager
import net.maxedgar.coffee.utils.client.Chronometer
import net.maxedgar.coffee.utils.client.env
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.modmenu.ModMenuCompatibility
import net.fabricmc.loader.impl.FabricLoaderImpl
import net.minecraft.SharedConstants
import org.lwjgl.glfw.GLFW
import java.lang.Thread.sleep
import kotlin.concurrent.thread

private val modMenuPresent = runCatching {
    Class.forName("com.terraformersmc.modmenu.ModMenu")
    true
}.getOrDefault(false)

/**
 * Hides client appearance
 *
 * using 2x CRTL + SHIFT to hide and unhide the client
 */
object HideAppearance : EventListener {

    /**
     * These mods will be removed from ModMenu.
     * When [isHidingNow] is true
     * Or added, if [isHidingNow] is false
     *
     * Because we don't know about the [Mod] container on each mod in this list
     * We set the default value is null.
     * And we'll provide the value after first removing the mod
     */
    private val modContainersToHide: MutableMap<String, Mod?> = arrayOf(
        "liquidbounce"
    ).associateWith { null }.toMutableMap()

    private val shiftChronometer = Chronometer()

    var isHidingNow = env("LB_UI_HIDE", "net.maxedgar.coffee.ui.hide")?.toBoolean() ?: false
        set(value) {
            field = value
            mc.schedule(::updateClient)

            if (modMenuPresent) {
                if (value) {
                    for (id in modContainersToHide.keys) {
                        modContainersToHide[id] = ModMenuCompatibility.INSTANCE.removeModUnchecked(id)
                    }
                } else {
                    for ((id, container) in modContainersToHide) {
                        container?.let {
                            ModMenuCompatibility.INSTANCE.addModUnchecked(id, it)
                        }
                    }
                }
            }
        }

    var isDestructed = false

    private fun updateClient() {
        mc.updateTitle()
        mc.window.setIcon(
            mc.vanillaPackResources,
            if (SharedConstants.getCurrentVersion().stable()) IconSet.RELEASE else IconSet.SNAPSHOT
        )
    }

    @Suppress("unused")
    private val keyHandler = handler<KeyboardKeyEvent> { event ->
        val keyCode = event.keyCode
        val modifier = event.mods

        if (inGame) {
            return@handler
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT && modifier == GLFW.GLFW_MOD_CONTROL) {
            if (!shiftChronometer.hasElapsed(400L)) {
                isHidingNow = !isHidingNow
            }

            shiftChronometer.reset()
        }
    }

    /**
     * Attempt to destruct the client
     */
    fun destructClient() {
        isHidingNow = true
        isDestructed = true

        mc.gui.hud.chat.recentChat.removeIf {
            it.startsWith(CommandManager.GlobalSettings.prefix)
        }

        // Cancel all async tasks
        ioScope.cancel()

        callEvent(ClientShutdownEvent)
        EventManager.unregisterAll()

        // Disable all modules
        // Be careful to not trigger ConfigManager saving, but this should be prevented by [isDestructed]
        // and unregistering all events
        for (module in ModuleManager) {
            module.enabled = false
        }
        ModuleManager.clear()
    }

    fun wipeClient() = thread(name = "wipe-client") {
        // Wait for the client to be destructed
        sleep(1000L)

        // Clear log folder
        mc.gameDirectory.resolve("logs").listFiles()?.forEach {
            runCatching {
                it.delete()
            }
        }

        // Delete Coffee folder and its content
        runCatching {
            ConfigSystem.rootFolder.deleteRecursively()
        }

        FabricLoaderImpl.INSTANCE.allMods.find {
            it.metadata.id == "liquidbounce"
        }?.let { mod ->
            // Delete JAR file
            runCatching {
                val origin = mod.origin

                for (path in origin.paths) {
                    runCatching {
                        path.toFile().delete()
                    }
                }
            }

            // Remove from Fabric Loader Impl
            runCatching {
                FabricLoaderImpl.INSTANCE.modsInternal.remove(mod)
            }
        }

        // History clear
        mc.gui.hud.chat.clearMessages(true)
    }

}
