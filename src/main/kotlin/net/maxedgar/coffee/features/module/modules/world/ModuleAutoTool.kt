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
package net.maxedgar.coffee.features.module.modules.world

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.event.events.BlockBreakingProgressEvent
import net.maxedgar.coffee.event.events.CancelBlockBreakingEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.block.bed.BedBlockTracker
import net.maxedgar.coffee.utils.block.getCenterDistanceSquaredEyes
import net.maxedgar.coffee.utils.block.stateOrEmpty
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.collection.Filter
import net.maxedgar.coffee.utils.collection.blockSortedSetOf
import net.maxedgar.coffee.utils.combat.CombatManager
import net.maxedgar.coffee.utils.inventory.AnchoredHotbarSwapController
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.inventory.InventoryConstraints
import net.maxedgar.coffee.utils.inventory.ItemSlot
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.inventory.findBestToolToMineBlock
import net.maxedgar.coffee.utils.item.getEnchantment
import net.maxedgar.coffee.utils.math.sq
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.function.BiPredicate

/**
 * AutoTool module
 *
 * Automatically chooses the best tool in your inventory to mine a block.
 */
object ModuleAutoTool : ClientModule("AutoTool", ModuleCategories.WORLD) {
    val toolSelector =
        choices(
            "ToolSelector",
            DynamicSelectMode,
            arrayOf(DynamicSelectMode, StaticSelectMode)
        )

    sealed class ToolSelectorMode(name: String) : Mode(name) {
        final override val parent: ModeValueGroup<*>
            get() = toolSelector

        fun getTool(blockState: BlockState): HotbarItemSlot? =
            if (filter(blockState.block, blocks)) {
                getToolSlot(blockState)
            } else {
                null
            }

        protected abstract fun getToolSlot(blockState: BlockState): HotbarItemSlot?
    }

    private object DynamicSelectMode : ToolSelectorMode("Dynamic") {
        private val ignoreDurability by boolean("IgnoreDurability", false)

        object ConsiderInventory : ToggleableValueGroup(this, "ConsiderInventory", enabled = false) {
            private val inventoryConstraints = tree(InventoryConstraints())
            private val swapController = AnchoredHotbarSwapController(
                owner = this,
                inventoryConstraints = inventoryConstraints,
                swapDelayProvider = { swapPreviousDelay },
            )

            override fun onDisabled() {
                swapController.reset()
                super.onDisabled()
            }

            fun onToolInHotbar() {
                swapController.clearRequestedSwap()
                swapController.touchActiveSwitching()
            }

            fun onToolInInventory(slot: ItemSlot) {
                swapController.requestSwapFromInventory(slot)
            }

            fun onNoTool() {
                swapController.clearRequestedSwap()
            }
        }

        init {
            tree(ConsiderInventory)
        }

        override fun getToolSlot(blockState: BlockState): HotbarItemSlot? {
            if (!ConsiderInventory.running) {
                return Slots.Hotbar.findBestToolToMineBlock(blockState, ignoreDurability, SilkTouchHandler)
            } else {
                val slot = Slots.HotbarAndInventory
                    .findBestToolToMineBlock(blockState, ignoreDurability, SilkTouchHandler)

                return when (slot) {
                    is HotbarItemSlot -> {
                        // We found the best tool in hotbar, don't need inventory action
                        ConsiderInventory.onToolInHotbar()
                        slot
                    }
                    is ItemSlot -> {
                        // Request inventory action and keep restore delay alive while actively switching.
                        ConsiderInventory.onToolInInventory(slot)
                        null
                    }
                    null -> {
                        ConsiderInventory.onNoTool()
                        null
                    }
                }
            }
        }
    }

    private object StaticSelectMode : ToolSelectorMode("Static") {
        private val slot by int("Slot", 0, 0..8)

        override fun getToolSlot(blockState: BlockState) = Slots.Hotbar[slot]
    }

    private val filter by enumChoice("Filter", Filter.BLACKLIST)
    private val blocks by blocks("Blocks", blockSortedSetOf())

    private object SilkTouchHandler : ToggleableValueGroup(
        this, "SilkTouchHandler", enabled = false
    ), BiPredicate<ItemStack, BlockState> {
        private val filter by enumChoice("Filter", Filter.WHITELIST)
        private val blocks by blocks(
            "Blocks",
            blockSortedSetOf(Blocks.ENDER_CHEST, Blocks.GLOWSTONE, Blocks.SEA_LANTERN, Blocks.TURTLE_EGG),
        )

        override fun test(itemStack: ItemStack, blockState: BlockState): Boolean =
            !running // If module AutoTool is disabled, this function returns true
                || blockState.block !in blocks
                || (filter == Filter.BLACKLIST) == (itemStack.getEnchantment(Enchantments.SILK_TOUCH) == 0)
    }

    init {
        tree(SilkTouchHandler)
    }

    private val swapPreviousDelay by int("SwapPreviousDelay", 20, 1..100, "ticks")

    private val requireSneaking by boolean("RequireSneaking", false)
    private val notDuringCombat by boolean("NotDuringCombat", false)

    private object RequireNearBed : ToggleableValueGroup(
        this, "RequireNearBed", enabled = false
    ), BedBlockTracker.Subscriber {
        override val maxLayers: Int get() = 1

        override fun onEnabled() {
            BedBlockTracker.subscribe(this)
        }

        override fun onDisabled() {
            BedBlockTracker.unsubscribe(this)
        }

        private val distance by float("Distance", 10.0f, 3.0f..50.0f)

        fun matches(): Boolean {
            return BedBlockTracker.allPositions().any { it.getCenterDistanceSquaredEyes() <= distance.sq() }
        }
    }

    init {
        tree(RequireNearBed)
    }

    val isInventoryConsidered: Boolean
        get() = DynamicSelectMode.ConsiderInventory.running

    @Suppress("unused")
    private val handleBlockBreakingProgress = handler<BlockBreakingProgressEvent> { event ->
        switchToBreakBlock(event.pos)
    }

    @Suppress("unused")
    private val handleCancelBlockBreaking = handler<CancelBlockBreakingEvent> {
        if (isInventoryConsidered) {
            DynamicSelectMode.ConsiderInventory.onNoTool()
        }
    }

    fun switchToBreakBlock(pos: BlockPos) {
        val cancelDueToCombat = notDuringCombat && CombatManager.isInCombat
        val cancelDueToNotSneaking = requireSneaking && !player.isShiftKeyDown
        if (cancelDueToCombat
            || cancelDueToNotSneaking
            || RequireNearBed.enabled && !RequireNearBed.matches()
        ) {
            if (isInventoryConsidered) {
                DynamicSelectMode.ConsiderInventory.onNoTool()
            }
            return
        }

        val blockState = pos.stateOrEmpty
        val slot = toolSelector.activeMode.getTool(blockState) ?: return
        SilentHotbar.selectSlotSilently(this, slot, swapPreviousDelay)
    }

    override fun onDisabled() {
        SilentHotbar.resetSlot(this)
    }


}
