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
package net.maxedgar.coffee.features.module.modules.combat.killaura

import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.events.SprintEvent
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.ModuleAutoWeapon
import net.maxedgar.coffee.features.module.modules.combat.elytratarget.ModuleElytraTarget
import net.maxedgar.coffee.features.module.modules.combat.killaura.KillAuraRotationsValueGroup.KillAuraRotationTiming
import net.maxedgar.coffee.features.module.modules.combat.killaura.features.KillAuraAutoBlock
import net.maxedgar.coffee.features.module.modules.combat.killaura.features.KillAuraFailSwing
import net.maxedgar.coffee.features.module.modules.combat.killaura.features.KillAuraFightBot
import net.maxedgar.coffee.features.module.modules.combat.killaura.features.KillAuraRange
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.aiming.utils.raytraceBox
import net.maxedgar.coffee.utils.block.SwingMode
import net.maxedgar.coffee.utils.client.Chronometer
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.client.world
import net.maxedgar.coffee.utils.combat.CombatManager
import net.maxedgar.coffee.utils.combat.attackEntity
import net.maxedgar.coffee.utils.combat.shouldBeAttacked
import net.maxedgar.coffee.utils.entity.boxedDistanceTo
import net.maxedgar.coffee.utils.entity.isBlockingServerside
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.inventory.InventoryManager.isInventoryOpen
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.raytracing.isLookingAtEntity
import net.maxedgar.coffee.utils.render.TargetRenderer
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

/**
 * Simplified KillAura module based on legacy logic.
 * Automatically attacks enemies with simple, performant targeting.
 */
@Suppress("MagicNumber", "LongMethod", "TooManyFunctions")
object ModuleKillAura : ClientModule("KillAura", ModuleCategories.COMBAT) {

    // ========== Simple CPS timing (like legacy MSTimer) ==========
    val attackTimer = Chronometer()
    private val cpsRange by intRange("CPS", 5..8, 1..20)
    private var attackDelay = randomClickDelay()
    private var clicks = 0

    // Range
    val range = tree(KillAuraRange)

    // Target - single source of truth in targetTracker
    val targetTracker = tree(KillAuraTargetTracker)
    val target: LivingEntity?
        get() = targetTracker.target

    // Clicker compatibility (used by FightBot, TickBase, FailSwing)
    val clicker = KillAuraClicker

    // Simple rotation
    private val rotations = tree(KillAuraRotationsValueGroup)

    // Bypass techniques
    internal val raycast by enumChoice("Raycast", RaycastMode.TRACE_ALL)
    private val keepSprint by boolean("KeepSprint", true)

    // Priority
    private enum class KillAuraPriority(override val tag: String) : Tagged {
        HEALTH("Health"), DISTANCE("Distance"), DIRECTION("Direction"), HURT_TIME("HurtTime"), ARMOR("Armor")
    }
    private enum class KillAuraTargetMode(override val tag: String) : Tagged {
        SINGLE("Single"), SWITCH("Switch"), MULTI("Multi")
    }

    private val priority by enumChoice("Priority", KillAuraPriority.DISTANCE)
    private val targetMode by enumChoice("TargetMode", KillAuraTargetMode.SINGLE)
    private val switchDelay by int("SwitchDelay", 15, 1..100, "ticks")
    private val isSwitchMode: Boolean
        get() = targetMode == KillAuraTargetMode.SWITCH

    // Inventory
    internal val ignoreOpenInventory by boolean("IgnoreOpenInventory", true)
    internal val simulateInventoryClosing by boolean("SimulateInventoryClosing", true)

    // Visual
    private val swing by boolean("Swing", true)

    // FOV
    private val fov by float("FOV", 180f, 0f..180f)

    // Hurt time
    private val hurtTime by int("HurtTime", 10, 0..10)

    internal var waitTicks = 0
    private var switchTimer = 0
    private val previousTargetIds = mutableListOf<Int>()

    init {
        tree(KillAuraAutoBlock)
        tree(KillAuraFailSwing)
        tree(KillAuraFightBot)
        tree(TargetRenderer(this) {
            targetTracker.target?.takeUnless { ModuleElytraTarget.isSameTargetRendering(it) }
        })
    }

    override fun onDisabled() {
        targetTracker.reset()
        clicks = 0
        attackTimer.reset()
        previousTargetIds.clear()
        KillAuraAutoBlock.stopBlocking()
    }

    val shouldBlockSprinting
        get() = !ModuleElytraTarget.running && keepSprint

    @Suppress("unused")
    private val sprintHandler = handler<SprintEvent> { event ->
        if (shouldBlockSprinting && (event.source == SprintEvent.Source.MOVEMENT_TICK ||
                event.source == SprintEvent.Source.INPUT)) {
            event.sprint = false
        }
    }

    @Suppress("unused")
    private val rotationUpdateHandler = handler<RotationUpdateEvent> {
        if (waitTicks > 0) {
            waitTicks--
        }

        val isInInventory = isInventoryOpen || mc.gui.screen() is ContainerScreen
        if ((isInInventory && !ignoreOpenInventory) || player.isSpectator || player.isDeadOrDying) {
            targetTracker.reset()
            previousTargetIds.clear()
            return@handler
        }

        updateTarget()
        ModuleAutoWeapon.onTarget(target)
    }

    @Suppress("unused")
    private val gameHandler = tickHandler {
        if (player.isDeadOrDying || player.isSpectator) return@tickHandler

        if (CombatManager.shouldPauseCombat) {
            KillAuraAutoBlock.stopBlocking()
            targetTracker.reset()
            return@tickHandler
        }

        // CPS timing (like legacy MSTimer)
        if (attackTimer.hasElapsed(attackDelay.toLong())) {
            if (cpsRange.last > 0) clicks++
            attackTimer.reset()
            attackDelay = randomClickDelay()
        }

        val currentTarget = target ?: run {
            KillAuraAutoBlock.stopBlocking()
            return@tickHandler
        }

        // Check range
        val distance = player.boxedDistanceTo(currentTarget).toFloat()
        if (distance > range.interactionRange) {
            if (KillAuraAutoBlock.enabled && distance <= range.scanRange) {
                KillAuraAutoBlock.startBlocking()
            } else {
                KillAuraAutoBlock.stopBlocking()
            }
            return@tickHandler
        }

        // Simple attack loop
        val maxClicks = clicks
        repeat(maxClicks) {
            val wasBlocking = player.isBlockingServerside
            runAttack(currentTarget)
            if (wasBlocking && !player.isBlockingServerside) {
                return@tickHandler
            }
        }
        clicks = 0
    }

    private val rotationTiming: KillAuraRotationTiming
        get() = rotations.rotationTiming

    private fun runAttack(currentTarget: LivingEntity) {
        if (currentTarget.hurtTime > hurtTime) return

        val rotation = findRotation(currentTarget) ?: return

        val isHittable = isLookingAtEntity(
            toEntity = currentTarget,
            rotation = rotation,
            range = range.interactionRange.toDouble(),
            throughWallsRange = range.interactionThroughWallsRange.toDouble()
        ) != null

        if (!isHittable) return

        KillAuraAutoBlock.makeSeemBlock()
        KillAuraAutoBlock.stopBlocking()

        attackEntity(currentTarget, SwingMode.DO_NOT_HIDE, keepSprint)
        if (swing) player.swing(net.minecraft.world.InteractionHand.MAIN_HAND)

        KillAuraAutoBlock.startBlocking()

        if (isSwitchMode) {
            if (switchTimer++ >= switchDelay) {
                previousTargetIds.add(currentTarget.id)
                switchTimer = 0
            }
        }
    }

    fun canAttackNow(): Boolean = running && attackTimer.hasElapsed(attackDelay.toLong())

    private fun updateTarget() {
        if (shouldPrioritize()) return

        targetTracker.reset()

        var bestTarget: LivingEntity? = null
        var bestValue = Double.MAX_VALUE

        for (entity in world.entitiesForRendering()) {
            if (entity !is LivingEntity || !entity.shouldBeAttacked()) continue
            if (isSwitchMode && entity.id in previousTargetIds) continue

            val distance = player.boxedDistanceTo(entity).toFloat()
            val entityFov = crosshairAngleToEntity(entity)

            if (distance > maxRange || fov != 180f && entityFov > fov) continue

            val currentValue = when (priority) {
                KillAuraPriority.DISTANCE -> distance.toDouble()
                KillAuraPriority.HEALTH -> entity.health.toDouble()
                KillAuraPriority.DIRECTION -> entityFov.toDouble()
                KillAuraPriority.HURT_TIME -> entity.hurtTime.toDouble()
                KillAuraPriority.ARMOR -> entity.armorValue.toDouble()
            }

            if (currentValue < bestValue) {
                bestValue = currentValue
                bestTarget = entity
            }
        }

        if (bestTarget != null) {
            val rotation = findRotation(bestTarget)
            if (rotation != null) {
                targetTracker.target = bestTarget
                RotationManager.setRotationTarget(
                    rotations.toRotationTarget(rotation, bestTarget, considerInventory = !ignoreOpenInventory),
                    priority = Priority.IMPORTANT_FOR_USAGE_2,
                    provider = this@ModuleKillAura
                )
            }
        }

        if (target == null && previousTargetIds.isNotEmpty()) {
            previousTargetIds.clear()
            updateTarget()
        }
    }

    private fun findRotation(entity: LivingEntity): Rotation? {
        val eyes = player.eyePosition
        val box = entity.boundingBox
        val rotation = raytraceBox(
            eyes = eyes,
            box = box,
            range = range.interactionRange.toDouble(),
            wallsRange = range.interactionThroughWallsRange.toDouble()
        )
        return rotation?.rotation
    }

    /** Simple crosshair angle calculation (no RotationUtils dependency) */
    private fun crosshairAngleToEntity(entity: Entity): Float {
        val rotation = RotationManager.currentRotation ?: player.rotation
        val delta = entity.boundingBox.center.subtract(player.eyePosition)
        // Mth.atan2 returns degrees in Minecraft, no .toDeg() needed
        val yaw = Mth.atan2(delta.z, delta.x) - 90f
        val pitch = Mth.atan2(-delta.y, kotlin.math.sqrt(delta.x * delta.x + delta.z * delta.z))
        val yawDiff = abs(Mth.wrapDegrees(rotation.yaw - yaw))
        val pitchDiff = abs(Mth.wrapDegrees(rotation.pitch - pitch))
        return max(yawDiff, pitchDiff).toFloat()
    }

    /** Generate a random click delay from the CPS range (like legacy MSTimer) */
    private fun randomClickDelay(): Int {
        val minMs = 1000 / cpsRange.last.coerceAtLeast(1)
        val maxMs = 1000 / cpsRange.first.coerceAtLeast(1)
        return if (maxMs <= minMs) minMs else Random.nextInt(minMs, maxMs)
    }

    @Suppress("FunctionOnlyReturningConstant")
    private fun shouldPrioritize(): Boolean = false

    private val maxRange: Float
        get() = range.interactionRange + range.scanRange



    enum class RaycastMode(override val tag: String) : Tagged {
        TRACE_NONE("None"),
        TRACE_ONLYENEMY("Enemy"),
        TRACE_ALL("All")
    }

}
