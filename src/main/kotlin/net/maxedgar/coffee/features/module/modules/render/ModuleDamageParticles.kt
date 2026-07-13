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
package net.maxedgar.coffee.features.module.modules.render

import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.EntityHealthUpdateEvent
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.OverlayRenderEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.maxedgar.coffee.utils.math.Easing
import net.maxedgar.coffee.utils.math.fma
import net.maxedgar.coffee.utils.render.WorldToScreen
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import java.text.DecimalFormat
import kotlin.math.abs

/**
 * DamageParticles module
 *
 * Show health changes of entities
 */
object ModuleDamageParticles : ClientModule("DamageParticles", ModuleCategories.RENDER) {

    private val ttl by float("TimeToLive", 3F, 0.5F..5.0F, "s")
    private val scale by float("Scale", 2F, 0.25F..4F)
    private val scaleTransition by easing("ScaleTransition", Easing.QUAD_OUT)
    private val displacement by vec3d("Displacement", Vec3.Y_AXIS.scale(1.5))
    private val displacementTransition by easing("DisplacementTransition", Easing.QUAD_OUT)
    private val trackMode by enumChoice("TrackMode", TrackMode.ON_TICK)

    init {
        tree(Colors)
    }

    private object Colors : ValueGroup("Colors") {
        val damage by color("Damage", Color4b.RED)
        val death by color("Death", Color4b.RED)
        val heal by color("Heal", Color4b.GREEN)
        val maxHealth by color("MaxHealth", Color4b.GREEN)
    }

    private enum class TrackMode(override val tag: String) : Tagged {
        ON_TICK("OnTick"),
        ON_UPDATE("OnUpdate"),
    }

    /**
     * Ordered by [Particle.startTime]
     */
    private val particles = ArrayDeque<Particle>()

    private val entityHealthMap = Reference2FloatOpenHashMap<LivingEntity>()

    private const val EPSILON = 0.05F
    private val FORMATTER = DecimalFormat("0.#")

    private fun trackEntityHealth(entity: LivingEntity, oldHealth: Float, newHealth: Float, maxHealth: Float) {
        val delta = abs(oldHealth - newHealth)
        if (delta > EPSILON) {
            val color = when {
                newHealth <= 0F -> Colors.death
                oldHealth > newHealth -> Colors.damage
                newHealth < maxHealth -> Colors.heal
                else -> Colors.maxHealth
            }

            particles += Particle(
                System.currentTimeMillis(),
                FORMATTER.format(delta),
                color,
                entity.boundingBox.center.add(entity.knownMovement),
            )
        }
    }

    private fun shouldNotTrack(entity: LivingEntity) = entity.tickCount == 0 || entity === player

    override fun onDisabled() {
        particles.clear()
        entityHealthMap.clear()
    }

    @Suppress("unused")
    private val worldChangeHandler = handler<WorldChangeEvent> {
        particles.clear()
        entityHealthMap.clear()
    }

    @Suppress("unused")
    private val entityHealthUpdateHandler = handler<EntityHealthUpdateEvent> {
        if (trackMode !== TrackMode.ON_UPDATE) {
            return@handler
        }

        val entity = it.entity
        if (shouldNotTrack(entity)) {
            return@handler
        }

        trackEntityHealth(entity, it.old, it.new, it.max)
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent>(priority = FIRST_PRIORITY) {
        val earliest = System.currentTimeMillis() - (ttl * 1000).toLong()
        while (particles.isNotEmpty() && particles.first().startTime < earliest) {
            particles.removeFirst()
        }

        if (trackMode !== TrackMode.ON_TICK) {
            return@handler
        }

        val entities = world.entitiesForRendering()
        for (entity in entities) {
            if (entity !is LivingEntity || shouldNotTrack(entity)) {
                continue
            }

            val newHealth = entity.health
            val maxHealth = entity.maxHealth
            val oldHealth = entityHealthMap.put(entity, newHealth)
            if (oldHealth != 0F) {
                trackEntityHealth(entity, oldHealth, newHealth, maxHealth)
            }
        }

        entityHealthMap.keys.removeIf { it !in entities || it.isDeadOrDying }
    }

    @Suppress("unused")
    private val renderHandler = handler<OverlayRenderEvent> { event ->
        val now = System.currentTimeMillis()
        particles.forEachIndexed { i, particle ->
            val progress = (now - particle.startTime).toFloat() / (ttl * 1000.0F)

            val currentPos = particle.pos.fma(displacementTransition.transform(progress).toDouble(), displacement)
            val screenPos = WorldToScreen.calculateScreenPos(currentPos) ?: return@forEachIndexed

            val currentScale = scale * scaleTransition.transform(progress)

            with(event.context) {
                pose().pushMatrix()
                pose().translate(screenPos.x, screenPos.y)
                pose().scale(currentScale, currentScale)

                centeredText(
                    mc.font,
                    particle.text,
                    0,
                    0,
                    particle.color.argb,
                )
                pose().popMatrix()
            }
        }

    }

    @JvmRecord
    private data class Particle(val startTime: Long, val text: String, val color: Color4b, val pos: Vec3)

}
