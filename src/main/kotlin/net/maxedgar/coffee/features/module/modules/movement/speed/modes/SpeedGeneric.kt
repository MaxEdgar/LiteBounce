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
package net.maxedgar.coffee.features.module.modules.movement.speed.modes

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.PlayerAfterJumpEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.tickHandler
import net.maxedgar.coffee.event.waitTicks
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed.doOptimizationsPreventJump
import net.maxedgar.coffee.features.module.modules.player.invcleaner.items.SpearItemFacet.Companion.COMPARING_LUNGE_AND_SPEED
import net.maxedgar.coffee.utils.block.SwingMode
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.entity.moving
import net.maxedgar.coffee.utils.entity.withStrafe
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.item.attackSpeed
import net.maxedgar.coffee.utils.item.durability
import net.maxedgar.coffee.utils.item.getEnchantment
import net.maxedgar.coffee.utils.math.copy
import net.minecraft.core.component.DataComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.enchantment.Enchantments
import kotlin.math.roundToInt

class SpeedSpeedYPort(parent: ModeValueGroup<*>) : SpeedBHopBase("YPort", parent) {

    private val speed by float("Speed", 0.4f, 0.1f..1f)

    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (!player.onGround() && player.moving) {
            player.deltaMovement = player.deltaMovement.copy(y = -1.0)
        }
    }

    @Suppress("unused")
    private val afterJumpHandler = handler<PlayerAfterJumpEvent> {
        player.deltaMovement = player.deltaMovement.withStrafe(speed = speed.toDouble())
    }

}

class SpeedLegitHop(parent: ModeValueGroup<*>) : SpeedBHopBase("LegitHop", parent)

abstract class SpeedBHopBase(name: String, override val parent: ModeValueGroup<*>) : Mode(name) {

    @Suppress("unused")
    private val movementInputHandler = handler<MovementInputEvent> { event ->
        if (!player.onGround() || !event.directionalInput.isMoving) {
            return@handler
        }

        if (doOptimizationsPreventJump()) {
            return@handler
        }

        event.jump = true
    }

}

class SpeedPiercingAttack(parent: ModeValueGroup<*>) : SpeedBHopBase("PiercingAttack", parent) {

    private val swingMode by enumChoice("SwingMode", SwingMode.DO_NOT_HIDE)
    private val holdTime by intRange("HoldTime", 1..1, 1..20, "ticks")
    private val onGround by boolean("OnGround", true)
    private val ignoreHunger by boolean("IgnoreHunger", false)
    private val waitForCooldown by boolean("WaitForCooldown", true)
    private val minimumDurability by int("MinimumDurability", 1, 0..20)

    /**
     * @see net.minecraft.client.Minecraft.startAttack
     * @see net.minecraft.world.entity.player.Player.getCurrentItemAttackStrengthDelay
     */
    @Suppress("unused")
    private val tickHandler = tickHandler {
        if (!onGround && player.onGround()
            || interaction.isSpectator
            || !ignoreHunger && player.foodData.foodLevel < 6) {
            return@tickHandler
        }

        val slot = Slots.Hotbar
            .filter {
                val itemStack = it.itemStack
                itemStack[DataComponents.PIERCING_WEAPON] != null
                    && itemStack.isItemEnabled(world.enabledFeatures())
                    && !player.cannotAttackWithItem(itemStack, 0)
                    && itemStack.getEnchantment(Enchantments.LUNGE) > 0
                    && itemStack.durability >= minimumDurability
            }
            .maxWithOrNull(COMPARING_LUNGE_AND_SPEED) ?: return@tickHandler
        val piercingWeapon = slot.itemStack[DataComponents.PIERCING_WEAPON]!!

        SilentHotbar.selectSlotSilently(this, slot, ticksUntilReset = holdTime.random())
        interaction.piercingAttack(piercingWeapon)
        swingMode.swing(InteractionHand.MAIN_HAND)

        if (waitForCooldown) {
            waitTicks((20.0 / slot.itemStack.attackSpeed).roundToInt())
        }
    }

    override fun disable() {
        SilentHotbar.resetSlot(this)
        super.disable()
    }

}
