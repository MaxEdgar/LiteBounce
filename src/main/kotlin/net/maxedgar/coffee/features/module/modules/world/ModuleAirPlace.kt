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
package net.maxedgar.coffee.features.module.modules.world


import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.PlayerInteractItemEvent
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.render.cameraclip.ScrollAdjustValueGroup
import net.maxedgar.coffee.render.FULL_BOX
import net.maxedgar.coffee.render.drawBox
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.render.renderEnvironment
import net.maxedgar.coffee.render.withPositionRelativeToCamera
import net.maxedgar.coffee.utils.entity.armorItems
import net.maxedgar.coffee.utils.entity.shouldSwingHand
import net.maxedgar.coffee.utils.item.isConsumable
import net.maxedgar.coffee.utils.math.toBlockPos
import net.minecraft.world.item.ArmorStandItem
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.FireworkRocketItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.phys.BlockHitResult

/**
 * AirPlace module
 *
 * Allows you to place blocks in midair.
 */
object ModuleAirPlace : ClientModule("AirPlace", ModuleCategories.WORLD) {

    private object Preview : ToggleableValueGroup(this, "Preview", true) {
        val outlineOnly by boolean("OutlineOnly", false)
        val fillColor by color("Color", Color4b(69, 119, 255, 104))
        val outlineColor by color("OutlineColor", Color4b.WHITE)
    }

    private val liquidPlace by boolean("PlaceInLiquid", false)

    private object CustomRange : ToggleableValueGroup(this, "CustomRange", false) {
        private val rangeBounds = 1.0f..4.5f
        val range = float("Range", 3.0f, rangeBounds)

        private val scrollAdjust = ScrollAdjustValueGroup(this, "ScrollAdjust", true, { delta ->
            val newValue = range.get() + delta
            range.set(newValue.coerceIn(rangeBounds))
        })

        init {
            tree(scrollAdjust)
        }

    }

    init {
        tree(Preview)
        tree(CustomRange)
    }

    private inline val BlockHitResult.isAirOrFluid: Boolean
        get() = world.getBlockState(blockPos).isAir ||
            (liquidPlace && !world.getFluidState(blockPos).isEmpty && !ModuleLiquidPlace.running)


    private fun ItemStack.isAirPlaceableAt(hit: BlockHitResult): Boolean {
        if (isEmpty || isConsumable) return false
        return when (val i = item) {
            is BlockItem -> i.block.defaultBlockState().canSurvive(world, hit.blockPos)
            is SpawnEggItem, is ArmorStandItem -> true
            is FireworkRocketItem -> !player.armorItems[2].`is`(Items.ELYTRA)
            else -> false
        }
    }

    private fun canPlayerPlaceAt(hit: BlockHitResult): Boolean {
        val main = player.mainHandItem
        if (main.isAirPlaceableAt(hit)) return true

        val off = player.offhandItem
        return off.isAirPlaceableAt(hit)
    }


    private fun getValidHitResult(): BlockHitResult? {
        val hitResult = mc.hitResult as? BlockHitResult ?: return null
        if (player.isSpectator) return null
        if (!hitResult.isAirOrFluid) return null
        if (!canPlayerPlaceAt(hitResult)) return null

        if (CustomRange.running) {
            val distance = CustomRange.range.get().toDouble()
            val playerEye = player.eyePosition
            val direction = hitResult.location.subtract(playerEye).normalize()
            val targetPos = playerEye.add(direction.scale(distance))

            val newHitResult = BlockHitResult(
                targetPos,
                hitResult.direction,
                targetPos.toBlockPos(),
                hitResult.isInside
            )

            if (!newHitResult.isAirOrFluid) return null
            if (!canPlayerPlaceAt(newHitResult)) return null

            return newHitResult
        }

        return hitResult
    }


    @Suppress("unused")
    private val renderHandler = handler<WorldRenderEvent> { event ->
        if (!Preview.running) return@handler
        val hitResult = getValidHitResult() ?: return@handler

        event.renderEnvironment {
            withPositionRelativeToCamera(hitResult.blockPos) {
                drawBox(
                    FULL_BOX,
                    if (Preview.outlineOnly) Color4b.TRANSPARENT else Preview.fillColor,
                    Preview.outlineColor
                )
            }
        }
    }

    @Suppress("unused")
    private val placeHandler = handler<PlayerInteractItemEvent> { event ->
        val hitResult = getValidHitResult() ?: return@handler

        val actionResult = interaction.useItemOn(player, event.hand, hitResult)
        if (actionResult.shouldSwingHand()) player.swing(event.hand)
        event.cancelEvent()
    }
}
