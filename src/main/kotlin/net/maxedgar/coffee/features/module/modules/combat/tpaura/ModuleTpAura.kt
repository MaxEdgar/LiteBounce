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
package net.maxedgar.coffee.features.module.modules.combat.tpaura

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.tpaura.modes.AStarMode
import net.maxedgar.coffee.features.module.modules.combat.tpaura.modes.ImmediateMode
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.block.SwingMode
import net.maxedgar.coffee.utils.clicking.Clicker
import net.maxedgar.coffee.utils.client.Chronometer
import net.maxedgar.coffee.utils.combat.TargetPriority
import net.maxedgar.coffee.utils.combat.TargetSelector
import net.maxedgar.coffee.utils.combat.attackEntity
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.entity.squaredBoxedDistanceTo
import net.maxedgar.coffee.utils.render.WireframePlayer
import net.minecraft.world.phys.Vec3

object ModuleTpAura : ClientModule("TpAura", ModuleCategories.COMBAT, disableOnQuit = true) {

    private val attackRange by float("AttackRange", 4.2f, 3f..5f)

    val clicker = tree(Clicker(this, mc.options.keyAttack))
    val mode = choices("Mode", AStarMode, arrayOf(AStarMode, ImmediateMode))
    val targetSelector = tree(TargetSelector(TargetPriority.HURT_TIME))

    val stuckChronometer = Chronometer()
    var desyncPlayerPosition: Vec3? = null

    private val wireframePlayer = WireframePlayer()

    @Suppress("unused")
    private val attackRepeatable = tickHandler {
        val position = desyncPlayerPosition ?: player.position()

        clicker.click {
            val target = targetSelector.targets().firstOrNull {
                it.squaredBoxedDistanceTo(position) <= attackRange * attackRange
            } ?: return@click false

            attackEntity(target, SwingMode.DO_NOT_HIDE, keepSprint = true)
            true
        }
    }

    @Suppress("unused")
    val renderHandler = handler<WorldRenderEvent> { event ->
        wireframePlayer.pos = desyncPlayerPosition ?: return@handler
        wireframePlayer.setRotation(RotationManager.currentRotation ?: player.rotation)
        wireframePlayer.render(event, Color4b(36, 32, 147, 87), Color4b(36, 32, 147, 255))
    }

}

abstract class TpAuraMode(name: String) : Mode(name) {

    final override val parent: ModeValueGroup<TpAuraMode>
        get() = ModuleTpAura.mode

}
