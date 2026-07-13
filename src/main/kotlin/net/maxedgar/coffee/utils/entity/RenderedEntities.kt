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
package net.maxedgar.coffee.utils.entity

import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.PerspectiveEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.global.GlobalSettingsTarget
import net.maxedgar.coffee.features.module.modules.render.ModuleCombineMobs
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.combat.Targets
import net.maxedgar.coffee.utils.combat.shouldBeShown
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.minecraft.world.entity.LivingEntity

private val entities = ReferenceArrayList<LivingEntity>()

/**
 * A readonly [Collection] containing all [LivingEntity] instances that meet the [shouldBeShown] condition.
 *
 * This collection will be auto updated on [GameTickEvent],
 * and be cleared on [WorldChangeEvent] or at the unsubscription of last [EventListener].
 */
object RenderedEntities : Collection<LivingEntity> by entities, EventListener {
    private val registry = ReferenceOpenHashSet<EventListener>()

    private val onUpdate = ReferenceArrayList<Pair<EventListener, Runnable>>()

    context(listener: EventListener)
    fun onUpdated(callback: Runnable) {
        onUpdate += listener to callback
    }

    private fun update() {
        onUpdate.removeIf { (listener, callback) ->
            if (listener !in registry) {
                true
            } else {
                callback.run()
                false
            }
        }
    }

    override val running: Boolean
        get() = registry.isNotEmpty()

    fun subscribe(subscriber: EventListener) {
        registry.add(subscriber)
    }

    fun unsubscribe(subscriber: EventListener) {
        registry.remove(subscriber)
        if (registry.isEmpty()) {
            entities.clear()
            update()
        }
    }

    private fun refresh() {
        entities.clear()

        val shouldCheckCombineMobs = ModuleCombineMobs.running

        for (entity in mc.level?.entitiesForRendering() ?: return) {
            if (entity is LivingEntity && entity.shouldBeShown()) {
                if (shouldCheckCombineMobs && ModuleCombineMobs.trackEntity(entity, true)) {
                    continue
                }

                entities += entity
            }
        }

        update()
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent>(priority = FIRST_PRIORITY) {
        if (inGame) {
            refresh()
        }
    }

    @Suppress("unused")
    private val perspectiveChangeHandler = handler<PerspectiveEvent> {
        if (GlobalSettingsTarget.visual.contains(Targets.SELF)) {
            refresh()
        }
    }

    @Suppress("unused")
    private val worldHandler = handler<WorldChangeEvent> {
        entities.clear()
        update()
    }

}
