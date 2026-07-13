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
@file:Suppress("detekt:TooManyFunctions")

package net.maxedgar.coffee.features.module.modules.render

import com.mojang.blaze3d.platform.InputConstants
import net.ccbluex.fastutil.enumSetOf
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.Event
import net.maxedgar.coffee.event.EventManager
import net.maxedgar.coffee.event.events.HealthUpdateEvent
import net.maxedgar.coffee.event.events.MouseButtonEvent
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.PerspectiveEvent
import net.maxedgar.coffee.event.events.PlayerMoveEvent
import net.maxedgar.coffee.event.events.PlayerTickEvent
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.newEventHook
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationsValueGroup
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.entity.withStrafe
import net.maxedgar.coffee.utils.input.isPressed
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention.OBJECTION_AGAINST_EVERYTHING
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.math.plus
import net.maxedgar.coffee.utils.movement.DirectionalInput
import net.maxedgar.coffee.utils.navigation.NavigationBaseValueGroup
import net.maxedgar.coffee.utils.raytracing.traceFromPoint
import net.minecraft.client.CameraType
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.lwjgl.glfw.GLFW
import java.util.function.Predicate
import kotlin.math.abs

/**
 * FreeCam module
 *
 * Allows you to move out of your body.
 */
object ModuleFreeCam : ClientModule("FreeCam", ModuleCategories.RENDER, disableOnQuit = true) {

    private val speed by float("Speed", 1f, 0.1f..2f)

    /**
     * Allows to interact from the camera perspective. This is very useful to interact with blocks that
     * are behind the player or walls. Similar functionality to the GhostBlock module.
     */
    private object CameraInteract : ToggleableValueGroup(ModuleFreeCam, "AllowCameraInteract", true) {
        val lookAt by boolean("LookAt", true)
    }

    private class CancelTrigger<E : Event>(val eventType: Class<E>, val predicate: Predicate<E>)
    private inline fun <reified E : Event> cancelTrigger(predicate: Predicate<E>) =
        CancelTrigger(E::class.java, predicate)

    /**
     * This is useful for cancelling FreeCam on certain events.
     * For example, when the player takes damage.
     */
    private enum class CancelOn(
        override val tag: String,
        private val trigger: CancelTrigger<out Event>,
    ) : Tagged {
        DAMAGE("Damage", cancelTrigger<HealthUpdateEvent> { event ->
            event.health < event.previousHealth
        }),
        TELEPORT("Teleport", cancelTrigger<PacketEvent> { event ->
            // ClientboundPlayerPositionPacket not trigger PlayerMoveEvent
            event.packet is ClientboundPlayerPositionPacket
        }),
        MOVE("Move", cancelTrigger<PlayerMoveEvent> { event ->
            // Don't check movement.y because it's gravity / falling motion
            abs(event.movement.x) > 0 || abs(event.movement.z) > 0
        }),
        LIQUID("Liquid", cancelTrigger<PlayerTickEvent> {
            player.isInLiquid
        });

        init {
            EventManager.registerEventHook(
                this.trigger.eventType,
                @Suppress("UNCHECKED_CAST")
                newEventHook { event ->
                    if (this in cancelOn && (this.trigger.predicate as Predicate<Event>).test(event)) {
                        ModuleFreeCam.enabled = false
                    }
                }
            )
        }
    }

    private val cancelOn by multiEnumChoice("CancelOn", enumSetOf<CancelOn>())

    /**
     * Navigation configuration for the FreeCam module
     */
    private object Navigation : NavigationBaseValueGroup<Unit>(ModuleFreeCam, "Navigation", false) {

        private val controlKey by key("Key", InputConstants.KEY_LCONTROL)

        val shouldBeGoing
            get() = running && controlKey != InputConstants.UNKNOWN && controlKey.isPressed

        /**
         * Creates context for navigation
         */
        override fun createNavigationContext() {
            // Nothing to do
        }

        /**
         * Calculates the desired position to move towards
         *
         * @return Target position as [Vec3]
         */
        override fun calculateGoalPosition(context: Unit): Vec3? {
            return if (shouldBeGoing) {
                getCameraLookingAt()
            } else {
                null
            }
        }

    }

    private val midClickCameraTeleport by boolean("MidClickCameraTeleport", false)

    private val keepSneaking by boolean("KeepSneaking", false)

    private val rotations = tree(RotationsValueGroup(this))

    init {
        tree(CameraInteract)
        tree(Navigation)
    }

    private object PositionState {
        var available: Boolean = false
            set(value) {
                if (value) {
                    pos = player.eyePosition
                    lastPos = pos
                } else {
                    pos = Vec3.ZERO
                    lastPos = Vec3.ZERO
                }
                field = value
            }

        var pos: Vec3 = Vec3.ZERO
            private set
        private var lastPos: Vec3 = Vec3.ZERO

        fun set(target: Vec3) {
            lastPos = pos
            pos = target
        }

        fun update(velocity: Vec3) = set(pos + velocity)

        fun interpolate(partialTicks: Float) = lastPos.lerp(pos, partialTicks.toDouble())
    }

    override fun onEnabled() {
        PositionState.available = true
        super.onEnabled()
    }

    override fun onDisabled() {
        PositionState.available = false

        // Reset player rotation
        val rotation = RotationManager.currentRotation ?: RotationManager.serverRotation
        player.yRot = rotation.yaw
        player.xRot = rotation.pitch
        super.onDisabled()
    }

    @Suppress("unused")
    private val mouseHandler = handler<MouseButtonEvent> { event ->
        if (midClickCameraTeleport &&
            event.action == GLFW.GLFW_PRESS && event.button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            val target = getCameraLookingAt() ?: return@handler

            // interpolate to prevent tp into block
            PositionState.set(PositionState.pos.lerp(target, 0.9))
        }
    }

    @Suppress("unused")
    private val inputHandler = handler<MovementInputEvent>(priority = FIRST_PRIORITY) { event ->
        val speed = this.speed.toDouble()
        val yAxisMovement = when {
            event.jump -> 1.0f
            event.sneak -> -1.0f
            else -> 0.0f
        }

        ModuleDebug.debugParameter(this, "DirectionalInput", event.directionalInput)
        val velocity = Vec3.ZERO
            .withStrafe(speed, input = event.directionalInput)
            .with(Direction.Axis.Y, yAxisMovement * speed)
        ModuleDebug.debugParameter(this, "Velocity", velocity)
        PositionState.update(velocity)

        event.directionalInput = DirectionalInput.NONE
        event.jump = false
        event.sneak = false
    }

    @Suppress("unused")
    private val forceSneakHandler = handler<MovementInputEvent>(priority = OBJECTION_AGAINST_EVERYTHING) { event ->
        if (keepSneaking) {
            event.sneak = true
        }
    }

    @Suppress("unused")
    private val perspectiveHandler = handler<PerspectiveEvent> { event ->
        event.perspective = CameraType.FIRST_PERSON
    }

    @Suppress("unused")
    private val rotationHandler = handler<RotationUpdateEvent> {
        val lookAt = if (Navigation.shouldBeGoing) {
            // Look at target position
            Navigation.getMovementRotation()
        } else if (CameraInteract.running && CameraInteract.lookAt) {
            // Aim at crosshair target
            val crosshairTarget = mc.hitResult ?: return@handler
            Rotation.lookingAt(crosshairTarget.location, player.eyePosition)
        } else {
            return@handler
        }

        RotationManager.setRotationTarget(rotations.toRotationTarget(lookAt),
            Priority.NOT_IMPORTANT, ModuleFreeCam)
    }

    @Suppress("unused")
    private val alwaysCancelOnHandler = handler<WorldChangeEvent> {
        // If not, will get stuck when world change
        enabled = false
    }

    fun applyCameraPosition(entity: Entity?, partialTicks: Float) {
        if (!running || entity != player || !PositionState.available) {
            return
        }

        val camera = mc.gameRenderer.mainCamera()

        return camera.setPosition(PositionState.interpolate(partialTicks))
    }

    fun renderPlayerFromAllPerspectives(entity: LivingEntity): Boolean {
        if (!running || entity != player) {
            return entity.isSleeping
        }

        return entity.isSleeping || !mc.gameRenderer.mainCamera().isDetached
    }

    /**
     * Modify the raycast position
     */
    fun modifyRaycast(original: Vec3, entity: Entity, tickDelta: Float): Vec3 {
        if (!running || entity !is LocalPlayer || !CameraInteract.running || !PositionState.available) {
            return original
        }

        return PositionState.interpolate(tickDelta)
    }

    fun shouldDisableCameraInteract() = running && !CameraInteract.running

    private fun getCameraLookingAt(): Vec3? {
        if (!PositionState.available) return null

        val cameraPosition = PositionState.interpolate(1f)
        val target = traceFromPoint(
            range = 200.0,
            start = cameraPosition,
            direction = mc.cameraEntity?.rotation?.directionVector ?: return null
        )

        return target.location
    }

}
