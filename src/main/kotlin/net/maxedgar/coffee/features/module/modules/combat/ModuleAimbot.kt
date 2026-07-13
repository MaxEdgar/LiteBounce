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
package net.maxedgar.coffee.features.module.modules.combat

import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.MouseRotationEvent
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.killaura.KillAuraRequirements
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug.debugGeometry
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.aiming.RotationTarget
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.aiming.data.RotationWithVector
import net.maxedgar.coffee.utils.aiming.features.MovementCorrection
import net.maxedgar.coffee.utils.aiming.features.processors.anglesmooth.impl.InterpolationAngleSmooth
import net.maxedgar.coffee.utils.aiming.features.processors.anglesmooth.impl.LinearAngleSmooth
import net.maxedgar.coffee.utils.aiming.features.processors.anglesmooth.impl.SigmoidAngleSmooth
import net.maxedgar.coffee.utils.aiming.point.PointTracker
import net.maxedgar.coffee.utils.aiming.preference.LeastDifferencePreference
import net.maxedgar.coffee.utils.aiming.utils.RotationUtil
import net.maxedgar.coffee.utils.aiming.utils.raytraceBox
import net.maxedgar.coffee.utils.aiming.utils.setRotation
import net.maxedgar.coffee.utils.client.Timer
import net.maxedgar.coffee.utils.combat.TargetPriority
import net.maxedgar.coffee.utils.combat.TargetTracker
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.inventory.InventoryManager
import net.maxedgar.coffee.utils.render.TargetRenderer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.entity.Entity

/**
 * Aimbot module
 *
 * Automatically faces selected entities around you.
 */
object ModuleAimbot : ClientModule("Aimbot", ModuleCategories.COMBAT, aliases = listOf("AimAssist", "AutoAim")) {

    private val range = float("Range", 4.2f, 1f..8f)

    val targetTracker = tree(TargetTracker(TargetPriority.DIRECTION, range = range))

    init {
        tree(TargetRenderer(this, targetTracker))
    }
    private val pointTracker = tree(PointTracker(this))

    private val requires by multiEnumChoice<KillAuraRequirements>("Requires")

    private val requirementsMet
        get() = mc.gui.screen() == null && requires.all { it.asBoolean }

    private var angleSmooth = modes(this, "AngleSmooth") {
        arrayOf(
            InterpolationAngleSmooth(it),
            SigmoidAngleSmooth(it),
            LinearAngleSmooth(it)
        )
    }

    private val axis by multiEnumChoice<Axis>("Axis", Axis.HORIZONTAL, Axis.VERTICAL)

    private val ignores by multiEnumChoice<IgnoreOpened>("Ignore")

    private var targetRotation: Rotation? = null
    private var playerRotation: Rotation? = null

    @Suppress("unused", "ComplexCondition")
    private val tickHandler = handler<RotationUpdateEvent> { _ ->
        playerRotation = player.rotation

        if (!requirementsMet) {
            targetTracker.reset()
            targetRotation = null
            return@handler
        }

        targetRotation = findNextTargetRotation()?.let { (target, rotation) ->
            angleSmooth.activeMode.process(
                RotationTarget(
                    rotation = rotation.rotation,
                    entity = target,
                    processors = listOf(angleSmooth.activeMode),
                    ticksUntilReset = 1,
                    resetThreshold = 1f,
                    considerInventory = true,
                    movementCorrection = MovementCorrection.CHANGE_LOOK
                ),
                player.rotation,
                rotation.rotation
            )
        }

        // Update Auto Weapon
        ModuleAutoWeapon.onTarget(targetTracker.target)
    }

    override fun onDisabled() {
        targetTracker.reset()
    }

    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        val partialTicks = event.partialTicks
        val target = targetTracker.target ?: return@handler

        if (IgnoreOpened.SCREEN !in ignores && mc.gui.screen() != null) {
            return@handler
        }

        if (IgnoreOpened.CONTAINER !in ignores && (InventoryManager.isInventoryOpen ||
                mc.gui.screen() is AbstractContainerScreen<*>)) {
            return@handler
        }

        lookAt(partialTicks)
    }

    @Suppress("unused")
    private val mouseMovement = handler<MouseRotationEvent> { event ->
        fun updateRotation(rotation: Rotation): Rotation =
            RotationUtil.applyMouseTurnDelta(rotation, event.cursorDeltaX, event.cursorDeltaY)

        playerRotation?.let { rotation ->
            playerRotation = updateRotation(rotation)
        }

        targetRotation?.let { rotation ->
            targetRotation = updateRotation(rotation)
        }
    }

    /**
     * Looks at the target rotation, with interpolation based on the timer speed and partial ticks to make it smooth.
     */
    private fun lookAt(partialTicks: Float) {
        val playerRotation = playerRotation ?: return
        val targetRotation = targetRotation ?: return
        val timerSpeed = Timer.timerSpeed
        val interpolatedRotation = playerRotation.interpolateTo(targetRotation, timerSpeed * partialTicks)

        player.setRotation(
            Rotation(
                yaw = if (Axis.HORIZONTAL in axis) interpolatedRotation.yaw else playerRotation.yaw,
                pitch = if (Axis.VERTICAL in axis) interpolatedRotation.pitch else playerRotation.pitch,
            )
        )
    }

    private fun findNextTargetRotation(): Pair<Entity, RotationWithVector>? {
        for (entity in targetTracker.targets()) {
            val eyes = player.eyePosition
            val point = pointTracker.findPoint(eyes, entity)

            debugGeometry("Box") { ModuleDebug.DebuggedBox(point.box, Color4b.ORANGE.with(a = 90)) }
            debugGeometry("Point") { ModuleDebug.DebuggedPoint(point.pos, Color4b.WHITE, size = 0.1) }

            val rotationPreference = LeastDifferencePreference.leastDifferenceToLastPoint(eyes, point.pos)
            val rotation = raytraceBox(
                eyes = eyes,
                box = point.box,
                range = targetTracker.maxRange.toDouble(),
                wallsRange = 0.0,
                rotationPreference = rotationPreference
            ) ?: continue

            targetTracker.target = entity
            return entity to rotation
        }

        targetTracker.reset()
        return null
    }

    private enum class IgnoreOpened(
        override val tag: String
    ) : Tagged {
        SCREEN("Screen"),
        CONTAINER("Container")
    }

    private enum class Axis(override val tag: String) : Tagged {
        HORIZONTAL("Horizontal"),
        VERTICAL("Vertical")
    }
}
