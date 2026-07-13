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

package net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball.techniques.FlyFireballCustomTechnique
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball.techniques.FlyFireballLegitTechnique
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball.trigger.FlyFireballInstantTrigger
import net.maxedgar.coffee.features.module.modules.movement.fly.modes.fireball.trigger.FlyFireballOnEdgeTrigger
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.inventory.useHotbarSlotOrOffhand
import net.minecraft.world.item.Items

internal object FlyFireball : Mode("Fireball") {

    override val parent: ModeValueGroup<*>
        get() = ModuleFly.modes

    val technique = modes("Technique", FlyFireballLegitTechnique,
        arrayOf(FlyFireballLegitTechnique, FlyFireballCustomTechnique))

    val trigger = modes("Trigger", FlyFireballInstantTrigger,
        arrayOf(FlyFireballInstantTrigger, FlyFireballOnEdgeTrigger))

    // Silent fireball selection
    val slotResetDelay by intRange("SlotResetDelay", 4..6, 0..40, "ticks")

    var wasTriggered = false

    private fun findFireballSlot(): HotbarItemSlot? = Slots.OffhandWithHotbar.findSlot(Items.FIRE_CHARGE)

    fun throwFireball() {
        useHotbarSlotOrOffhand(
            findFireballSlot() ?: return,
            ticksUntilReset = slotResetDelay.random(),
        )
    }

}
