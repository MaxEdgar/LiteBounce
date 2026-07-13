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
package net.maxedgar.coffee.features.module.modules.combat.crystalaura

import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.destroy.SubmoduleCrystalDestroyer
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.place.SubmoduleCrystalPlacer
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.post.CrystalPostAttackTracker
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.post.SubmoduleSetDead
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.trigger.CrystalAuraTriggerer
import net.maxedgar.coffee.utils.aiming.NoRotationMode
import net.maxedgar.coffee.utils.aiming.NormalRotationMode
import net.maxedgar.coffee.utils.client.FloatValueProvider
import net.maxedgar.coffee.utils.combat.CombatManager
import net.maxedgar.coffee.utils.combat.TargetTracker
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.render.TargetRenderer

/**
 * Module CrystalAura
 *
 * Automatically places and explodes end crystals.
 *
 * @author ccetl
 */
object ModuleCrystalAura : ClientModule(
    "CrystalAura",
    ModuleCategories.COMBAT,
    aliases = listOf("AutoCrystal"),
    disableOnQuit = true
) {

    val targetTracker = tree(TargetTracker(
        rangeValue = FloatValueProvider("Range", 4.5f, 1f..12f)
    ))

    object PredictFeature : ValueGroup("Predict") {
        init {
            treeAll(SelfPredict, TargetPredict)
        }
    }

    init {
        treeAll(
            SubmoduleCrystalPlacer,
            SubmoduleCrystalDestroyer,
            CrystalAuraDamageOptions,
            CrystalAuraTriggerer,
            PredictFeature,
            SubmoduleIdPredict,
            SubmoduleSetDead,
            SubmoduleBasePlace
        )
    }

    init {
        tree(TargetRenderer(this, targetTracker))
    }

    val rotationMode = modes(this, "RotationMode") {
        arrayOf(
            NormalRotationMode(it, this, Priority.IMPORTANT_FOR_USAGE_2, true),
            NoRotationMode(it, this)
        )
    }

    override fun onDisabled() {
        CrystalAuraTriggerer.terminateRunningTasks()
        SubmoduleCrystalPlacer.placementRenderer.clearSilently()
        SubmoduleCrystalDestroyer.postAttackHandlers.forEach(CrystalPostAttackTracker::onToggle)
        SubmoduleBasePlace.onDisabled()
        CrystalAuraDamageOptions.cacheMap.clear()
    }

    override fun onEnabled() {
        SubmoduleCrystalDestroyer.postAttackHandlers.forEach(CrystalPostAttackTracker::onToggle)
    }

    @Suppress("unused")
    private val simulatedTickHandler = handler<RotationUpdateEvent>(1) {
        CrystalAuraDamageOptions.cacheMap.clear()
        if (CombatManager.shouldPauseCombat) {
            return@handler
        }

        targetTracker.selectFirst()
    }

}
