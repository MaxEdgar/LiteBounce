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

package net.maxedgar.coffee.event.events

import net.maxedgar.coffee.annotations.Tag
import net.maxedgar.coffee.event.CancellableEvent
import net.maxedgar.coffee.event.Event
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.combat.EntityTargetClassification
import net.maxedgar.coffee.utils.combat.EntityTargetingInfo
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.kotlin.PriorityField
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

@Tag("attack")
class AttackEntityEvent(
    val entity: Entity
) : CancellableEvent()

@Tag("entityMargin")
class EntityMarginEvent(val entity: Entity, var margin: Float) : Event()

@Tag("entityHealthUpdate")
class EntityHealthUpdateEvent(val entity: LivingEntity, val old: Float, val new: Float, val max: Float) : Event()

@Tag("tagEntityEvent")
class TagEntityEvent(val entity: Entity, var targetingInfo: EntityTargetingInfo) : Event() {
    val color: PriorityField<Color4b?> = PriorityField(null, Priority.NOT_IMPORTANT)

    /**
     * Don't start combat this target
     */
    fun dontTarget() {
        if (this.targetingInfo.classification == EntityTargetClassification.TARGET) {
            this.targetingInfo = this.targetingInfo.copy(classification = EntityTargetClassification.INTERESTING)
        }
    }

    /**
     * Fully ignore that target
     */
    fun ignore() {
        this.targetingInfo = targetingInfo.copy(classification = EntityTargetClassification.IGNORED)
    }

    fun assumeFriend() {
        this.targetingInfo = targetingInfo.copy(isFriend = true)
    }

    fun color(col: Color4b, priority: Priority) {
        this.color.trySet(col, priority)
    }
}
