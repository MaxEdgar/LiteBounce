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
package net.maxedgar.coffee.features.module.modules.misc

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.misc.FriendManager
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.client.SilentHotbar
import net.maxedgar.coffee.utils.client.notification
import net.maxedgar.coffee.utils.entity.rotation
import net.maxedgar.coffee.utils.inventory.Slots
import net.maxedgar.coffee.utils.inventory.useHotbarSlotOrOffhand
import net.maxedgar.coffee.utils.raytracing.findEntityInCrosshair
import net.maxedgar.coffee.utils.raytracing.isLookingAtEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items

/**
 * MiddleClickAction module
 *
 * Allows you to perform actions with middle clicks.
 */
object ModuleMiddleClickAction : ClientModule(
    "MiddleClickAction",
    ModuleCategories.MISC,
    aliases = listOf("FriendClicker", "MiddleClickPearl")
) {

    init {
        doNotIncludeAlways()
    }

    private val mode = modes(this, "Mode", FriendClicker, arrayOf(FriendClicker, Pearl))

    override fun onDisabled() {
        SilentHotbar.resetSlot(Pearl)
        Pearl.disable()
    }

    object Pearl : Mode("Pearl") {

        private val slotResetDelay by int("SlotResetDelay", 1, 0..10, "ticks")
        private val stopOnSubmit by floatRange("StopOnSubmit", 85F..90F, 60F..90F, "Pitch")
        private var wasPressed = false

        val repeatable = handler<GameTickEvent> {
            if (mc.gui.screen() != null) {
                wasPressed = false
                return@handler
            }

            if (player.xRot in stopOnSubmit) {
                wasPressed = false
                return@handler
            }

            val pickup = mc.options.keyPickItem.isDown

            if (pickup) {
                // visually select the slot
                val slot = Slots.OffhandWithHotbar.findSlot(Items.ENDER_PEARL) ?: return@handler
                SilentHotbar.selectSlotSilently(this, slot, slotResetDelay)
                wasPressed = true
            } else if (wasPressed) { // the key was released
                Slots.OffhandWithHotbar.findSlot(Items.ENDER_PEARL)?.let {
                    useHotbarSlotOrOffhand(it, slotResetDelay)
                }
                wasPressed = false
            }
        }

        @Suppress("unused")
        private val handler = handler<WorldChangeEvent> {
            wasPressed = false
        }

        override fun disable() {
            wasPressed = false
        }

        fun cancelPick(): Boolean {
            return ModuleMiddleClickAction.running &&
                mode.activeMode == this &&
                Slots.OffhandWithHotbar.findSlot(Items.ENDER_PEARL) != null
        }

        override val parent: ModeValueGroup<*>
            get() = mode

    }

    object FriendClicker : Mode("FriendClicker") {

        private val pickUpRange by float("PickUpRange", 3.0f, 1f..100f)

        private var clicked = false

        val repeatable = handler<GameTickEvent> {
            val rotation = player.rotation

            val entity = (findEntityInCrosshair(pickUpRange.toDouble(), rotation) { it is Player }
                ?: return@handler).entity as Player

            val entityHitResult = isLookingAtEntity(
                toEntity = entity, rotation = rotation, range = pickUpRange.toDouble(),
                throughWallsRange = 0.0
            )

            val pickup = mc.options.keyPickItem.isDown

            if (entityHitResult != null && pickup && !clicked) {
                val name = entity.scoreboardName

                if (FriendManager.isFriend(name)) {
                    FriendManager.friends.remove(FriendManager.Friend(name, null))
                    notification(
                        "FriendClicker",
                        message("removedFriend", name),
                        NotificationEvent.Severity.INFO
                    )
                } else {
                    FriendManager.friends.add(FriendManager.Friend(name, null))

                    notification(
                        "FriendClicker",
                        message("addedFriend", name),
                        NotificationEvent.Severity.INFO
                    )
                }
            }

            clicked = pickup
        }

        override val parent: ModeValueGroup<*>
            get() = mode

    }

}
