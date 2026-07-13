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
package net.maxedgar.coffee.features.module.modules.combat.killaura

import net.maxedgar.coffee.features.module.modules.combat.ModuleAutoWeapon
import net.maxedgar.coffee.utils.client.isOlderThanOrEqual1_8
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.combat.TargetTracker
import net.maxedgar.coffee.utils.entity.wouldBlockHit
import net.maxedgar.coffee.utils.item.isAxe
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

object KillAuraTargetTracker : TargetTracker() {

    /**
     * Allows to ignore when the target is holding a shield,
     * which would normally block attacks.
     */
    private val ignoreShield by boolean("IgnoreShield", true)

    override fun validate(entity: LivingEntity): Boolean {
        return super.validate(entity) && validateShield(entity)
    }

    /**
     * Check if the entity is holding a shield and if the shield would block the attack.
     */
    private fun validateShield(entity: LivingEntity): Boolean {
        if (ignoreShield || entity !is Player || isOlderThanOrEqual1_8) {
            return true
        }

        if (player.mainHandItem.isAxe || ModuleAutoWeapon.willShieldBreak) {
            return true
        }

        return !entity.wouldBlockHit
    }
}
