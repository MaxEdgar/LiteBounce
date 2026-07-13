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
package net.maxedgar.coffee.features.module.modules.combat.crystalaura.post

import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.ModuleCrystalAura
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Counts how many crystals the crystal aura places.
 * "CPS" stands for crystals per second.
 */
object CrystalAuraSpeedDebugger : CrystalPostAttackTracker() {

    private val cps = ConcurrentLinkedQueue<Long>()

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        val currentTime = System.currentTimeMillis()
        val cpsTime = currentTime - 1000L
        cps.removeIf { it < cpsTime }

        ModuleDebug.debugParameter(
            ModuleCrystalAura,
            "CPS",
            cps.size
        )
    }

    override fun confirmed(id: Int) {
        cps.offer(System.currentTimeMillis())
    }

    override fun cleared() {
        cps.clear()
    }

    override val running
        get() = ModuleCrystalAura.running && ModuleDebug.running

}
