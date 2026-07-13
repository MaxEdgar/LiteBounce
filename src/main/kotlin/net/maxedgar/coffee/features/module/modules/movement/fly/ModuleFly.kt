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
package net.maxedgar.coffee.features.module.modules.movement.fly

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.PlayerStrideEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.FlyAirWalk
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.FlyCreative
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.FlyEnderpearl
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.FlyExplosion
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.FlyJetpack
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.FlyVanilla
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball.FlyFireball
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.grim.FlyGrim2373Jan15
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.grim.FlyGrim2859V
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.hypixel.FlyHypixel
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.hypixel.FlyHypixelFlat
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.polar.FlyHycraftDamage
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.sentinel.FlySentinel10thMar
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.sentinel.FlySentinel20thApr
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.sentinel.FlySentinel26thDec
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.sentinel.FlySentinel27thJan
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.spartan.FlySpartan524
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.specific.FlyNcpClip
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.verus.FlyVerusB3869Flat
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.verus.FlyVerusB3896Damage
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.vulcan.FlyVulcan277
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.vulcan.FlyVulcan286
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.vulcan.FlyVulcan286MC18
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.vulcan.FlyVulcan286Teleport
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.markAsError
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket

/**
 * Fly module
 *
 * Allows you to fly.
 */

object ModuleFly : ClientModule("Fly", ModuleCategories.MOVEMENT, aliases = listOf("Glide", "Jetpack")) {

    internal val modes = choices(
        "Mode", FlyVanilla, arrayOf(
            // Generic fly modes
            FlyVanilla,
            FlyCreative,
            FlyJetpack,
            FlyEnderpearl,
            FlyAirWalk,
            FlyExplosion,
            FlyFireball,

            // Anti-cheat specific fly modes
            FlyVulcan277,
            FlyVulcan286,
            FlyVulcan286MC18,
            FlyVulcan286Teleport,
            FlyGrim2859V,
            FlyGrim2373Jan15,
            FlySpartan524,

            // Server specific fly modes
            FlySentinel20thApr,
            FlySentinel27thJan,
            FlySentinel10thMar,
            FlySentinel26thDec,

            FlyVerusB3896Damage,
            FlyVerusB3869Flat,
            FlyNcpClip,

            FlyHypixel,
            FlyHypixelFlat,

            FlyHycraftDamage
        )
    ).apply { tagBy(this) }

    private object Visuals : ToggleableValueGroup(this, "Visuals", true) {

        private val stride by boolean("Stride", true)

        @Suppress("unused")
        val strideHandler = handler<PlayerStrideEvent> { event ->
            if (stride) {
                event.strideForce = 0.1.coerceAtMost(player.deltaMovement.horizontalDistance()).toFloat()
            }

        }

    }

    init {
        tree(Visuals)
    }

    private val disableOnSetback by boolean("DisableOnSetback", false)

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        // Setback detection
        if (event.packet is ClientboundPlayerPositionPacket && disableOnSetback) {
            chat(markAsError(message("setbackDetected")))
            enabled = false
        }
    }

}
