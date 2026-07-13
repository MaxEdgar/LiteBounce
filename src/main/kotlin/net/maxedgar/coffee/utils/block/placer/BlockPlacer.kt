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
package net.maxedgar.coffee.utils.block.placer

import it.unimi.dsi.fastutil.longs.Long2BooleanLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.ccbluex.fastutil.fastIterator
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug.DebuggedPoint
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug.debugGeometry
import net.maxedgar.coffee.render.FULL_BOX
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.aiming.data.Rotation
import net.maxedgar.coffee.utils.block.SwingMode
import net.maxedgar.coffee.utils.block.doPlacement
import net.maxedgar.coffee.utils.block.getState
import net.maxedgar.coffee.utils.block.isBlockedByEntitiesReturnCrystal
import net.maxedgar.coffee.utils.block.isInteractable
import net.maxedgar.coffee.utils.block.targetfinding.BlockOffsetOptions
import net.maxedgar.coffee.utils.block.targetfinding.BlockPlacementTarget
import net.maxedgar.coffee.utils.block.targetfinding.BlockPlacementTargetFindingOptions
import net.maxedgar.coffee.utils.block.targetfinding.CenterTargetPositionFactory
import net.maxedgar.coffee.utils.block.targetfinding.FaceHandlingOptions
import net.maxedgar.coffee.utils.block.targetfinding.PlayerLocationOnPlacement
import net.maxedgar.coffee.utils.block.targetfinding.findBestBlockPlacementTarget
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.client.player
import net.maxedgar.coffee.utils.collection.getSlot
import net.maxedgar.coffee.utils.inventory.HotbarItemSlot
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.math.center
import net.maxedgar.coffee.utils.math.sq
import net.maxedgar.coffee.utils.raytracing.raytraceBlock
import net.maxedgar.coffee.utils.raytracing.traceFromPlayer
import net.maxedgar.coffee.utils.render.placement.PlacementRenderer
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import java.util.function.LongPredicate
import kotlin.math.max

@Suppress("TooManyFunctions")
class BlockPlacer(
    name: String,
    val module: ClientModule,
    val priority: Priority,
    val slotFinder: (BlockPos?) -> HotbarItemSlot?,
    allowSupportPlacements: Boolean = true
) : ValueGroup(name), EventListener {

    val range by float("Range", 4.5f, 1f..6f)
    val wallRange by float("WallRange", 4.5f, 0f..6f)
    val cooldown by intRange("Cooldown", 1..2, 0..40, "ticks")
    val swingMode by enumChoice("Swing", SwingMode.DO_NOT_HIDE)

    /**
     * Construct a center hit result when the raytrace result is invalid.
     * This can make the module rotations wrong as well as place a bit outside the range,
     * but it makes the placements a lot more reliable and works on most servers.
     */
    val constructFailResult by boolean("ConstructFailResult", true)

    /**
     * Defines how long the player should sneak when placing on an interactable block.
     * This can make placing multiple blocks seem smoother.
     */
    private val sneak by intRange("Sneak", 1..1, 0..10, "ticks")

    private val ignores by multiEnumChoice("Ignore", Ignore.entries)

    val ignoreOpenInventory get() = Ignore.OPEN_INVENTORY in ignores

    val ignoreUsingItem get() = Ignore.USING_ITEM in ignores

    val slotResetDelay by intRange("SlotResetDelay", 4..6, 0..40, "ticks")

    val rotationMode = modes(this, "RotationMode") {
        arrayOf(NormalRotationMode(it, this), NoRotationMode(it, this))
    }

    val support = SupportFeature(this)

    init {
        if (allowSupportPlacements) {
            tree(support)
        } else {
            support.enabled = false
        }
    }

    val crystalDestroyer = tree(CrystalDestroyFeature(this, module))

    /**
     * Renders all tracked positions that are queued to be placed.
     */
    val targetRenderer = tree(PlacementRenderer("TargetRendering", false, module))

    /**
     * Renders all placements.
     */
    val placedRenderer = tree(PlacementRenderer(
        "PlacedRendering",
        true,
        module,
        keep = false
    ))

    private val blockPosCache = BlockPos.MutableBlockPos()

    /**
     * Stores all block positions where blocks should be placed paired with a boolean that is `true`
     * if the position was added by [support].
     */
    val blocks = Long2BooleanLinkedOpenHashMap()

    private val inaccessible = LongOpenHashSet()
    var ticksToWait = 0
    var ranAction = false
    private var sneakTimes = 0

    @Suppress("unused")
    private val targetUpdater = handler<RotationUpdateEvent>(priority = -20) {
        if (ticksToWait > 0) {
            ticksToWait--
        } else if (ranAction) {
            ranAction = false
            ticksToWait = cooldown.random()
        }

        val inventoryOpen = !ignoreOpenInventory && mc.gui.screen() is AbstractContainerScreen<*>
        val usingItem = !ignoreUsingItem && player.isUsingItem
        if (inventoryOpen || usingItem) {
            return@handler
        }

        if (blocks.isEmpty()) {
            return@handler
        }

        // return if no blocks are available
        val slot = slotFinder(null) ?: return@handler

        val itemStack = slot.itemStack

        inaccessible.clear()
        rotationMode.activeMode.onTickStart()
        if (scheduleCurrentPlacements(itemStack)) {
            return@handler
        }

        // no possible position found, now a support placement can be considered

        if (support.enabled && support.chronometer.hasElapsed(support.delay.toLong())) {
            findSupportPath(itemStack)
        }
    }

    @Suppress("unused")
    private val movementInputHandler = handler<MovementInputEvent> { event ->
        if (sneakTimes > 0) {
            sneakTimes--
            event.sneak = true
        }
    }

    @Suppress("CognitiveComplexMethod")
    private fun findSupportPath(itemStack: ItemStack) {
        val currentPlaceCandidates = hashSetOf<BlockPos>()

        // remove all positions of the current support path
        blocks.long2BooleanEntrySet().removeIf { entry ->
            if (entry.booleanValue) {
                currentPlaceCandidates.add(BlockPos.of(entry.longKey))
                true
            } else {
                false
            }
        }

        var supportPath: Set<BlockPos>? = null
        // find the best path
        for (entry in blocks.fastIterator()) {
            val posAsLong = entry.longKey
            if (posAsLong in inaccessible) continue
            val pos = blockPosCache.set(posAsLong)

            support.findSupport(pos)?.let { path ->
                val size = path.size
                if (supportPath == null || supportPath.size > size) {
                    supportPath = path
                }

                // one block is almost the best we can get, so why bother scanning the other blocks
                if (size <= 1) {
                    break
                }
            }
        }

        // we found the same path again, updating is not required
        if (currentPlaceCandidates == supportPath) {
            currentPlaceCandidates.forEach { blocks.put(it.asLong(), true) }
            return
        }

        currentPlaceCandidates.forEach { removeFromQueue(blockPosCache.set(it)) }

        supportPath?.let { path ->
            for (pos in path) {
                if (pos.asLong() !in blocks.keys) {
                    addToQueue(pos, isSupport = true)
                }
            }
            scheduleCurrentPlacements(itemStack)
        }

        support.chronometer.reset()
    }

    private fun scheduleCurrentPlacements(itemStack: ItemStack): Boolean {
        var hasPlaced = false

        for (entry in blocks.fastIterator()) {
            val posAsLong = entry.longKey

            if (inaccessible.contains(posAsLong) || isBlocked(posAsLong)) {
                continue
            }

            val searchOptions = BlockPlacementTargetFindingOptions(
                BlockOffsetOptions.Default,
                FaceHandlingOptions(CenterTargetPositionFactory, considerFacingAwayFaces = wallRange > 0),
                stackToPlaceWith = itemStack,
                PlayerLocationOnPlacement(position = player.position()),
            )

            // TODO prioritize faces where sneaking is not required
            val pos = blockPosCache.set(posAsLong)
            val placementTarget = findBestBlockPlacementTarget(pos, searchOptions) ?: continue

            // Check if we can reach the target
            if (!canReach(placementTarget.interactedBlockPos, placementTarget.rotation)) {
                inaccessible.add(posAsLong)
                continue
            }

            debugGeometry("PlacementTarget") {
                DebuggedPoint(pos.center, Color4b.GREEN.with(a = 100))
            }

            // sneak when placing on interactable block to not trigger their action
            if (placementTarget.interactedBlockPos.getState().isInteractable) {
                sneakTimes = sneak.random()
            }

            if (rotationMode.activeMode(entry.booleanValue, pos, placementTarget)) {
                return true
            }

            hasPlaced = true
        }

        return hasPlaced
    }

    private fun isBlocked(posAsLong: Long): Boolean {
        val pos = blockPosCache.set(posAsLong)
        if (!pos.getState()!!.canBeReplaced()) {
            inaccessible.add(posAsLong)
            return true
        }

        val blockedResult = pos.isBlockedByEntitiesReturnCrystal()
        if (crystalDestroyer.enabled) {
            blockedResult.value()?.let {
                crystalDestroyer.currentTarget = it
            }
        }

        if (blockedResult.keyBoolean()) {
            inaccessible.add(posAsLong)
            return true
        }

        return false
    }

    fun doPlacement(isSupport: Boolean, pos: BlockPos, placementTarget: BlockPlacementTarget) {
        // choose block to place
        val slot = if (isSupport) {
            support.filter.getSlot(support.blocks)
        } else {
            slotFinder(pos)
        } ?: return

        val verificationRotation = rotationMode.activeMode.getVerificationRotation(placementTarget.rotation)

        // check if we can still reach the target
        if (!canReach(placementTarget.interactedBlockPos, verificationRotation)) {
            return
        }

        // get the block hit result needed for the placement
        val blockHitResult = raytraceTarget(
            placementTarget.interactedBlockPos,
            verificationRotation,
            placementTarget.direction
        ) ?: return

        SilentHotbar.selectSlotSilently(this, slot, slotResetDelay.random())

        if (slot.itemStack.item !is BlockItem || pos.getState()!!.canBeReplaced()) {
            blocks.remove(pos.asLong())

            // place the block
            doPlacement(blockHitResult, hand = slot.useHand, swingMode = swingMode)
            placedRenderer.addBlock(pos)
            targetRenderer.removeBlock(pos)
        }
    }

    private fun raytraceTarget(pos: BlockPos, providedRotation: Rotation, direction: Direction): BlockHitResult? {
        val blockHitResult = raytraceBlock(
            range = max(range, wallRange).toDouble(),
            rotation = providedRotation,
            pos = pos,
            state = pos.getState()!!
        )

        if (blockHitResult != null && blockHitResult.type == HitResult.Type.BLOCK && blockHitResult.blockPos == pos) {
            return blockHitResult.withDirection(direction)
        }

        if (constructFailResult) {
            return BlockHitResult(pos.center, direction, pos, false)
        }

        return null
    }

    fun canReach(pos: BlockPos, rotation: Rotation): Boolean {
        // not the exact distance but good enough
        val distance = pos.distToCenterSqr(player.eyePosition)
        val wallRangeSq = wallRange.toDouble().sq()

        // if the wall range already covers it, the actual range doesn't matter
        if (distance <= wallRangeSq) {
            return true
        }

        val raycast = traceFromPlayer(range = range.toDouble(), rotation = rotation)
        return raycast.type == HitResult.Type.BLOCK && raycast.blockPos == pos
    }

    /**
     * Removes all positions that are not in [positions] and adds all that are not in the queue.
     */
    fun update(positions: Collection<BlockPos>) {
        val iterator = blocks.fastIterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val position = blockPosCache.set(entry.longKey)
            if (position !in positions) {
                targetRenderer.removeBlock(position)
                iterator.remove()
            } else {
                entry.setValue(false)
            }
        }

        positions.forEach { addToQueue(it, false) }
        targetRenderer.updateAll()
    }

    /**
     * Adds a block to be placed.
     *
     * @param pos The position, can be [BlockPos.MutableBlockPos].
     * @param update Whether the renderer should update the culling.
     */
    fun addToQueue(pos: BlockPos, update: Boolean = true, isSupport: Boolean = false) {
        blocks.computeIfAbsent(pos.asLong(), LongPredicate {
            targetRenderer.addBlock(blockPosCache.set(it), update, FULL_BOX)
            isSupport
        })
    }

    /**
     * Removes a block from the queue.
     *
     * @param pos The position, can be [BlockPos.MutableBlockPos].
     */
    fun removeFromQueue(pos: BlockPos) {
        blocks.remove(pos.asLong())
        targetRenderer.removeBlock(pos)
    }

    /**
     * Discards all blocks.
     */
    fun clear() {
        blocks.fastIterator().forEach { targetRenderer.removeBlock(blockPosCache.set(it.longKey)) }
        blocks.clear()
    }

    /**
     * THis should be called when the module using this placer is disabled.
     */
    fun disable() {
        reset()
        crystalDestroyer.onDisable()
        targetRenderer.clearSilently()
        placedRenderer.clearSilently()
    }

    fun isDone(): Boolean {
        return blocks.isEmpty()
    }

    @Suppress("unused")
    val worldChangeHandler = handler<WorldChangeEvent> {
        reset()
    }

    private fun reset() {
        sneakTimes = 0
        blocks.clear()
        inaccessible.clear()
    }

    override fun parent(): EventListener = module

    private enum class Ignore(override val tag: String) : Tagged {
        OPEN_INVENTORY("OpenInventory"),
        USING_ITEM("UsingItem")
    }
}
