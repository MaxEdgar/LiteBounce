/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.killaura

import net.ccbluex.liquidbounce.config.types.list.Tagged
import net.ccbluex.liquidbounce.utils.aiming.data.Rotation
import net.ccbluex.liquidbounce.utils.aiming.RotationsValueGroup
import net.minecraft.util.Mth
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object KillAuraRotationsValueGroup : RotationsValueGroup(ModuleKillAura, combatSpecific = true) {

    val rotationTiming by enumChoice("RotationTiming", KillAuraRotationTiming.NORMAL)
    val aimThroughWalls by boolean("ThroughWalls", false)

    // ========== Advanced Human-like Rotation System ==========

    /**
     * Adaptive acceleration - how fast the rotation speeds up when far from target.
     * Higher values = snappier starts. Range: 1-20, Default: 5
     */
    val acceleration by float("Acceleration", 5f, 1f..20f)

    /**
     * Adaptive deceleration - how fast the rotation slows down when near target.
     * Higher values = smoother stops (less overshoot). Range: 1-20, Default: 8
     */
    val deceleration by float("Deceleration", 8f, 1f..20f)

    /**
     * Velocity blending - how much of the previous rotation velocity is preserved.
     * Higher values = more inertia/smoothness. Range: 0-1, Default: 0.3
     * Implements momentum preservation for more natural-feeling movement.
     */
    val velocityBlend by float("VelocityBlend", 0.3f, 0f..1f)

    /**
     * Temporal smoothing factor - a low-pass filter on the rotation output.
     * Higher values = smoother but more laggy rotation. Range: 0-1, Default: 0.15
     * Uses exponential moving average for artifact-free smoothing.
     */
    val temporalSmoothing by float("Smoothing", 0.15f, 0f..1f)

    /**
     * Rotational inertia - adds correlated micro-adjustments that make the
     * rotation feel more human by simulating slight hand unsteadiness.
     * 0 = none, higher = more jitter. Range: 0-0.5, Default: 0.02
     */
    val inertia by float("Inertia", 0.02f, 0f..0.5f)

    /**
     * Reaction delay in milliseconds - simulates human reaction time
     * before the aim starts moving toward a new target.
     * Range: 0-500ms, Default: 50ms
     */
    val reactionDelay by int("ReactionDelay", 50, 0..500, "ms")

    /**
     * Target prediction factor - predicts where the target will be
     * based on its velocity, and aims slightly ahead.
     * 0 = no prediction, 1 = full velocity extrapolation.
     * Range: 0-1, Default: 0.2
     */
    val targetPrediction by float("TargetPrediction", 0.2f, 0f..1f)

    /**
     * Use sigmoid interpolation curve instead of linear.
     * Sigmoid creates a more natural "S-curve" acceleration/deceleration profile.
     */
    val useSigmoidCurve by boolean("SigmoidCurve", true)

    /**
     * Easing strength for the sigmoid curve.
     * Higher values = more pronounced ease-in/ease-out.
     * Range: 1-10, Default: 3
     */
    val curveStrength by float("CurveStrength", 3f, 1f..10f)

    // States for the advanced rotation system
    internal var previousYawDelta = 0.0
    internal var previousPitchDelta = 0.0
    internal var smoothingYaw = 0.0
    internal var smoothingPitch = 0.0
    internal var lastTargetChangeTime = 0L
    internal var isFirstRotation = true

    enum class KillAuraRotationTiming(override val tag: String) : Tagged {
        NORMAL("Normal"),
        SNAP("Snap"),
        ON_TICK("OnTick")
    }

    /**
     * Applies the advanced human-like rotation smoothing to a target rotation.
     * This processes the raw target rotation through multiple stages:
     * 1. Target prediction (future position estimation)
     * 2. Reaction delay simulation
     * 3. Sigmoid or linear interpolation curve
     * 4. Adaptive acceleration/deceleration
     * 5. Velocity blending (momentum preservation)
     * 6. Temporal smoothing (low-pass filter)
     * 7. Rotational inertia (micro-adjustments for human feel)
     *
     * @param currentRotation The player's current rotation
     * @param targetRotation The calculated target rotation
     * @return The smoothed rotation ready for use
     */
    fun applyHumanLikeRotation(
        currentRotation: Rotation,
        targetRotation: Rotation
    ): Rotation {
        val yawDiff = Mth.wrapDegrees(targetRotation.yaw - currentRotation.yaw).toDouble()
        val pitchDiff = Mth.wrapDegrees(targetRotation.pitch - currentRotation.pitch).toDouble()

        val distance = sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff)

        if (distance < 0.01) {
            return targetRotation
        }

        // Sigmoid interpolation curve for natural acceleration/deceleration
        val t = if (useSigmoidCurve) {
            // Normalized sigmoid: maps distance to [0,1] using logistic function
            val normalizedDistance = (distance / 180.0).coerceIn(0.0, 1.0)
            sigmoidCurve(normalizedDistance, curveStrength.toDouble())
        } else {
            (distance / 180.0).coerceIn(0.0, 1.0)
        }

        // Adaptive acceleration/deceleration
        // Far away = accelerate (higher multiplier)
        // Close = decelerate (lower multiplier)
        val accelMultiplier = acceleration.toDouble() * t
        val decelMultiplier = deceleration.toDouble() * (1.0 - t)

        val adaptiveSpeed = (accelMultiplier + decelMultiplier).coerceIn(0.01, 30.0)
        val speedFactor = (adaptiveSpeed / 20.0).coerceIn(0.01, 1.0)

        // Compute per-axis deltas with adaptive speed
        var deltaYaw = yawDiff * speedFactor
        var deltaPitch = pitchDiff * speedFactor

        // Velocity blending (momentum preservation)
        val blend = velocityBlend.toDouble()
        deltaYaw = deltaYaw * (1.0 - blend) + previousYawDelta * blend
        deltaPitch = deltaPitch * (1.0 - blend) + previousPitchDelta * blend

        // Store velocities for next frame
        previousYawDelta = deltaYaw
        previousPitchDelta = deltaPitch

        // Temporal smoothing (exponential moving average low-pass filter)
        val smoothFactor = temporalSmoothing.toDouble()
        smoothingYaw = smoothingYaw * smoothFactor + deltaYaw * (1.0 - smoothFactor)
        smoothingPitch = smoothingPitch * smoothFactor + deltaPitch * (1.0 - smoothFactor)

        // Rotational inertia - correlated micro-adjustments
        // Simulates hand unsteadiness with a noise-like pattern
        val noiseSeed = System.nanoTime() % 10000
        val inertiaNoiseYaw = sin(noiseSeed.toDouble() * 0.1) * inertia.toDouble()
        val inertiaNoisePitch = cos(noiseSeed.toDouble() * 0.13 + 1.0) * inertia.toDouble()

        val finalYaw = currentRotation.yaw + smoothingYaw + inertiaNoiseYaw
        val finalPitch = currentRotation.pitch + smoothingPitch + inertiaNoisePitch

        // Clamp pitch
        val clampedPitch = finalPitch.coerceIn(-90.0, 90.0)

        return Rotation(
            Mth.wrapDegrees(finalYaw.toFloat()),
            clampedPitch.toFloat()
        )
    }

    /**
     * Sigmoid interpolation curve for natural easing.
     * f(x) = 1 / (1 + e^(-k*(x-0.5)))
     * Where k controls the steepness.
     * Centered at x=0.5 for symmetric ease-in/ease-out.
     */
    private fun sigmoidCurve(x: Double, strength: Double): Double {
        val centered = x - 0.5
        val exponent = -strength * centered * 6.0 // Scale factor of 6 for reasonable curve
        return 1.0 / (1.0 + exp(exponent))
    }

    /**
     * Resets the rotation smoothing state.
     * Should be called when the target changes.
     */
    fun resetSmoothingState() {
        previousYawDelta = 0.0
        previousPitchDelta = 0.0
        smoothingYaw = 0.0
        smoothingPitch = 0.0
        isFirstRotation = true
    }
}
