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
package net.maxedgar.coffee.utils.aiming.features.processors.anglesmooth.impl

import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.aiming.RotationTarget
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.aiming.features.processors.anglesmooth.AngleSmooth
import net.maxedgar.coffee.utils.entity.lastRotation
import net.maxedgar.coffee.utils.kotlin.random
import net.minecraft.util.Mth
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.random.Random

/**
 * A fully deterministic, closed-form "humanized" rotation smoother.
 *
 * This exists as a drop-in, CPU-only replacement for the previous PyTorch/DJL-backed "AI" angle
 * smoothing mode. It targets the same goal (natural-looking, non-robotic aim movement) using plain
 * math instead of a bundled neural network:
 *
 * - A sigmoid-based ease curve slows the turn down as it approaches the target (no sudden snapping).
 * - Momentum blending with the previous tick's rotation delta avoids sudden acceleration/deceleration
 *   spikes, mimicking the "velocity awareness" a learned model would otherwise provide.
 * - A small, slowly-varying (correlated, not per-tick independent) wobble is layered on top so the
 *   path isn't a perfectly straight line, without looking jittery.
 *
 * Cost per tick: a handful of float operations (one `exp` call) - no model loading, no native
 * inference runtime, no bundled weights. This is the same order of cost as [SigmoidAngleSmooth] and
 * [InterpolationAngleSmooth], both of which already ship in this project.
 */
class HumanizedAngleSmooth(parent: ModeValueGroup<*>) : AngleSmooth("Humanized", parent, arrayListOf("Natural")) {

    private val horizontalTurnSpeed by floatRange("HorizontalTurnSpeed", 55f..75f, 0f..180f, "%")
    private val verticalTurnSpeed by floatRange("VerticalTurnSpeed", 55f..75f, 0f..180f, "%")

    private val steepness by float("Steepness", 6f, 0.5f..20f)
    private val midpoint by float("Midpoint", 0.35f, 0f..1f)

    /** How much of the previous tick's movement carries over into this tick (0 = none, 1 = full). */
    private val momentum by float("Momentum", 0.35f, 0f..0.9f)

    /** Amplitude of the slow, correlated wobble layered on top of the main movement. */
    private val wobble by float("Wobble", 0.06f, 0f..0.3f)

    // Correlated noise state - a smoothed random walk rather than independent per-tick noise,
    // so the wobble doesn't look like jitter.
    private var wobbleYaw = 0f
    private var wobblePitch = 0f

    private fun sigmoidEase(progress: Float): Float {
        val scaled = 1f - progress
        return (1f / (1f + exp((-steepness * (scaled - midpoint)).toDouble()))).toFloat()
    }

    private fun stepWobble(current: Float): Float {
        // Exponential smoothing towards a new random target - keeps the noise continuous/organic
        // instead of flickering every tick.
        val target = Random.nextFloat() * 2f - 1f
        return current + (target - current) * 0.15f
    }

    override fun process(
        rotationTarget: RotationTarget,
        currentRotation: Rotation,
        targetRotation: Rotation
    ): Rotation {
        val prevRotation = RotationManager.previousRotation ?: player.lastRotation
        val prevDelta = prevRotation.rotationDeltaTo(currentRotation)
        val totalDelta = currentRotation.rotationDeltaTo(targetRotation)

        val maxDelta = max(abs(totalDelta.deltaYaw), abs(totalDelta.deltaPitch)).coerceAtLeast(0.0001f)
        val progress = (maxDelta / 180f).coerceIn(0f, 1f)
        val ease = sigmoidEase(progress)

        val yawSpeed = horizontalTurnSpeed.random() / 100f
        val pitchSpeed = verticalTurnSpeed.random() / 100f

        val rawYawStep = totalDelta.deltaYaw * ease * yawSpeed
        val rawPitchStep = totalDelta.deltaPitch * ease * pitchSpeed

        // Blend with previous tick's velocity so the movement doesn't abruptly start/stop.
        val yawStep = rawYawStep * (1f - momentum) + prevDelta.deltaYaw * momentum
        val pitchStep = rawPitchStep * (1f - momentum) + prevDelta.deltaPitch * momentum

        wobbleYaw = stepWobble(wobbleYaw)
        wobblePitch = stepWobble(wobblePitch)

        return Rotation(
            currentRotation.yaw + yawStep + wobbleYaw * wobble,
            (currentRotation.pitch + pitchStep + wobblePitch * wobble).coerceIn(-90f, 90f)
        )
    }

    override fun calculateTicks(currentRotation: Rotation, targetRotation: Rotation): Int {
        var simulated = currentRotation
        var prevYawStep = 0f
        var prevPitchStep = 0f
        var ticks = 0

        while (!simulated.approximatelyEquals(targetRotation) && ticks < 80) {
            val totalDelta = simulated.rotationDeltaTo(targetRotation)
            val maxDelta = max(abs(totalDelta.deltaYaw), abs(totalDelta.deltaPitch)).coerceAtLeast(0.0001f)
            val progress = (maxDelta / 180f).coerceIn(0f, 1f)
            val ease = sigmoidEase(progress)

            val rawYawStep = totalDelta.deltaYaw * ease * (horizontalTurnSpeed.start / 100f)
            val rawPitchStep = totalDelta.deltaPitch * ease * (verticalTurnSpeed.start / 100f)

            val yawStep = rawYawStep * (1f - momentum) + prevYawStep * momentum
            val pitchStep = rawPitchStep * (1f - momentum) + prevPitchStep * momentum

            simulated = Rotation(
                Mth.wrapDegrees(simulated.yaw + yawStep),
                (simulated.pitch + pitchStep).coerceIn(-90f, 90f)
            )

            prevYawStep = yawStep
            prevPitchStep = pitchStep
            ticks++
        }

        return ticks
    }

}
