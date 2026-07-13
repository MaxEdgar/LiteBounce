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
package net.maxedgar.coffee.features.module.modules.world

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.ccbluex.fastutil.objectRBTreeSetOf
import net.maxedgar.coffee.config.types.ValueType
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.DeathEvent
import net.maxedgar.coffee.event.events.DisconnectEvent
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.command.commands.module.CommandAutoDisable
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.killaura.ModuleKillAura
import net.maxedgar.coffee.features.module.modules.movement.ModuleNoClip
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed
import net.maxedgar.coffee.utils.client.notification
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

/**
 * AutoDisable module
 *
 * Automatically disables modules, when special event happens.
 *
 * Command: [CommandAutoDisable]
 */
object ModuleAutoDisable : ClientModule("AutoDisable", ModuleCategories.WORLD) {
    val modules: Set<ClientModule>
        field: MutableSet<ClientModule> = ReferenceOpenHashSet()

    private val moduleNames by registryList("Modules", objectRBTreeSetOf<String>(), ValueType.CLIENT_MODULE)
    private val disableOn by multiEnumChoice<DisableOn>("On", DisableOn.entries, canBeNone = false)

    fun clear() {
        modules.clear()
        moduleNames.clear()
    }

    fun add(module: ClientModule): Boolean {
        return if (modules.add(module)) {
            moduleNames.add(module.name)
            true
        } else {
            false
        }
    }

    fun remove(module: ClientModule): Boolean {
        return if (modules.remove(module)) {
            moduleNames.remove(module.name)
            true
        } else {
            false
        }
    }

    init {
        add(ModuleFly)
        add(ModuleSpeed)
        add(ModuleNoClip)
        add(ModuleKillAura)
    }

    @Suppress("unused")
    private val worldChangesHandler = handler<PacketEvent> {
        if (it.packet is ClientboundPlayerPositionPacket && DisableOn.FLAG in disableOn) {
            disableAndNotify("flag")
        }
    }

    @Suppress("unused")
    private val deathHandler = handler<DeathEvent> {
        if (DisableOn.DEATH in disableOn) disableAndNotify("your death")
    }

    @Suppress("unused")
    private val worldChangeHandler = handler<WorldChangeEvent> {
        if (DisableOn.WORLD_CHANGE in disableOn) disableAndNotify("world change")
    }

    @Suppress("unused")
    private val disconnectHandler = handler<DisconnectEvent> {
        if (DisableOn.DISCONNECT in disableOn) disableAndNotify("disconnection")
    }

    private fun disableAndNotify(reason: String) {
        var anyDisabled = false
        for (module in modules) {
            if (module.enabled) {
                module.enabled = false
                anyDisabled = true
            }
        }

        if (anyDisabled) {
            notification("Notifier", "Disabled modules due to $reason", NotificationEvent.Severity.INFO)
        }
    }

    private enum class DisableOn(override val tag: String) : Tagged {
        FLAG("Flag"),
        DEATH("Death"),
        WORLD_CHANGE("WorldChange"),
        DISCONNECT("Disconnect"),
    }
}
