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

package net.maxedgar.coffee.features.module.modules.misc.debugrecorder.modes

import com.google.gson.JsonObject
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.misc.debugrecorder.ModuleDebugRecorder
import net.maxedgar.coffee.utils.combat.shouldBeAttacked
import net.maxedgar.coffee.utils.io.toJsonObject
import net.maxedgar.coffee.utils.math.minus
import net.maxedgar.coffee.utils.world.getEntitiesInCube
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

object BoxDebugRecorder : ModuleDebugRecorder.DebugRecorderMode<JsonObject>("Box") {

    private const val RANGE = 10.0

    val repeatable = handler<GameTickEvent> {
        val crosshairTarget = mc.hitResult

        if (crosshairTarget?.type != HitResult.Type.ENTITY || crosshairTarget !is EntityHitResult) {
            return@handler
        }

        recordPacket(JsonObject().apply {
            world.getEntitiesInCube<LivingEntity>(player.position(), RANGE) {
                it.shouldBeAttacked() && it.distanceToSqr(player) < RANGE * RANGE
                    && crosshairTarget.entity.id == it.id
            }.minByOrNull {
                it.distanceToSqr(player)
            }?.let {
                val vector = it.boundingBox.center - crosshairTarget.location
                add("vec", vector.toJsonObject())
            }
        })
    }

}
