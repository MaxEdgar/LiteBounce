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
package net.maxedgar.coffee.features.module.modules.combat.crystalaura.destroy

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.ModuleCrystalAura
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.post.CrystalAuraSpeedDebugger
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.post.SubmoduleSetDead
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug
import net.maxedgar.coffee.render.FULL_BOX
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.aiming.utils.raytraceBox
import net.maxedgar.coffee.utils.block.SwingMode
import net.maxedgar.coffee.utils.client.Chronometer
import net.maxedgar.coffee.utils.combat.attackEntity
import net.maxedgar.coffee.utils.math.isHitByLine
import net.maxedgar.coffee.utils.raytracing.isLookingAtEntity
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.max

object SubmoduleCrystalDestroyer : ToggleableValueGroup(ModuleCrystalAura, "Destroy", true) {

    val swingMode by enumChoice("Swing", SwingMode.DO_NOT_HIDE)
    private val delay by int("Delay", 0, 0..1000, "ms")
    val range by float("Range", 4.5f, 1f..6f)
    val wallsRange by float("WallsRange", 4.5f, 0f..6f)

    // prioritizes faces that are visible, might make the crystal aura slower
    private val prioritizeVisibleFaces by boolean("PrioritizeVisibleFaces", false)

    var postAttackHandlers = arrayOf(CrystalAuraSpeedDebugger, SubmoduleSetDead.CrystalTracker)
    val chronometer = Chronometer()

    fun tick(providedCrystal: EndCrystal? = null) {
        if (!enabled || !chronometer.hasAtLeastElapsed(delay.toLong())) {
            return
        }

        // update the target / validate the provided crystal
        providedCrystal?.let { crystal ->
            // just check if it works, not if it's the best
            CrystalAuraDestroyTargetFactory.validateAndUpdateTarget(crystal)
        } ?: run {
            CrystalAuraDestroyTargetFactory.updateTarget()
        }

        val target = CrystalAuraDestroyTargetFactory.currentTarget ?: return

        val base = FULL_BOX.move(target.blockPosition().below())
        mc.execute {
            ModuleDebug.debugGeometry(
                ModuleCrystalAura,
                "predictedBlock",
                ModuleDebug.DebuggedBox(base, Color4b.GREEN.fade(0.4f))
            )
        }

        val eyePos = player.eyePosition

        // find the best spot (and skip if no spot was found)
        val (rotation, vec3d) =
            raytraceBox(
                eyePos,
                target.boundingBox,
                range = range.toDouble(),
                wallsRange = wallsRange.toDouble(),
                futureTarget = base,
                prioritizeVisible = prioritizeVisibleFaces
            ) ?: return

        queueDestroy(rotation, target, base, eyePos, vec3d)
    }

    private fun queueDestroy(rotation: Rotation, target: EndCrystal, base: AABB, eyePos: Vec3, vec3d: Vec3) {
        // create the action chain to execute
        val action = {
            ModuleCrystalAura.rotationMode.activeMode.rotate(rotation, isFinished = {
                isLookingAtEntity(
                    toEntity = target,
                    rotation = RotationManager.serverRotation,
                    range = range.toDouble(),
                    throughWallsRange = wallsRange.toDouble()
                ) != null
            }, onFinished = {
                if (!chronometer.hasAtLeastElapsed(delay.toLong())) {
                    return@rotate
                }

                val target1 = CrystalAuraDestroyTargetFactory.currentTarget ?: return@rotate

                attackEntity(target1, swingMode)
                postAttackHandlers.forEach { it.attacked(target1.id) }
                chronometer.reset()
            })
        }

        // fixes swinging off thread as other packets might interrupt
        if (swingMode.serverSwing && !base.isHitByLine(eyePos, vec3d)) {
            mc.execute(action)
        } else {
            action()
        }
    }

    fun getMaxRange() = max(range, wallsRange)

}
