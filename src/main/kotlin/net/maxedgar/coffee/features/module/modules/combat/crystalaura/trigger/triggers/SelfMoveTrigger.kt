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
package net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.triggers

import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.destroy.SubmoduleCrystalDestroyer
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.SubmoduleCrystalPlacer
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.mc
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.runDestroy
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.runPlace
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.Trigger
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket

/**
 * Runs placing when you move.
 */
object SelfMoveTrigger : Trigger("SelfMove", false) {

    @Suppress("unused")
    private val packetListener = handler<PacketEvent>(-1) { event ->
        val packet = event.packet
        if (packet !is ServerboundMovePlayerPacket) {
            return@handler
        }

        mc.execute {
            runDestroy { SubmoduleCrystalDestroyer.tick() }
            runPlace { SubmoduleCrystalPlacer.tick() }
        }
    }

}
