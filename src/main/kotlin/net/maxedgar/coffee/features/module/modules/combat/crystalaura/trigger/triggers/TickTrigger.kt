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
package net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.triggers

import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.SubmoduleIdPredict
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.destroy.SubmoduleCrystalDestroyer
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.SubmoduleCrystalPlacer
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.offThread
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.runDestroy
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer.runPlace
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.Trigger

/**
 * Runs placing and destroying every tick.
 */
object TickTrigger : Trigger("Tick", true) {

    @Suppress("unused")
    private val simulatedTickHandler = handler<RotationUpdateEvent> {
        if (offThread) {
            runDestroy { SubmoduleCrystalDestroyer.tick() }
            runPlace { SubmoduleCrystalPlacer.tick() }
        } else {
            // Make the crystal destroyer run
            SubmoduleCrystalDestroyer.tick()
            // Make the crystal placer run
            SubmoduleCrystalPlacer.tick()
            if (!SubmoduleIdPredict.enabled) {
                // Make the crystal destroyer run
                SubmoduleCrystalDestroyer.tick()
            }
        }
    }

    override val allowsCaching
        get() = true

}
