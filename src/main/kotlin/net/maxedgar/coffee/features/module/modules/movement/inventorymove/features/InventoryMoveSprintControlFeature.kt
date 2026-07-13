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
package net.maxedgar.coffee.features.module.modules.movement.inventorymove.features

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.SprintEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.movement.inventorymove.ModuleInventoryMove
import net.maxedgar.coffee.utils.inventory.InventoryManager
import net.maxedgar.coffee.utils.kotlin.EventPriorityConvention

object InventoryMoveSprintControlFeature : ToggleableValueGroup(ModuleInventoryMove, "SprintControl", false) {

    private val clientMode by enumChoice("Client", SprintMode.DO_NOT_CHANGE)
    private val serverMode by enumChoice("Server", SprintMode.DO_NOT_CHANGE)

    private enum class SprintMode(override val tag: String) : Tagged {

        /**
         * This can be used to not change the sprint state.
         */
        DO_NOT_CHANGE("DoNotChange"),

        /**
         * This can be used to force sprinting on Scaffold,
         * while not allowing to sprint omnidirectional
         * when Scaffold is not active.
         */
        FORCE_SPRINT("ForceSprint"),

        /**
         * This can be used to disable sprinting on Scaffold,
         * while still allowing to sprint omnidirectional
         * when Scaffold is not active.
         */
        FORCE_NO_SPRINT("ForceNoSprint"),

    }

    override val running: Boolean
        get() = super.running && InventoryManager.isHandledScreenOpen

    @Suppress("unused")
    private val sprintHandler = handler<SprintEvent>(
        priority = EventPriorityConvention.MODEL_STATE
    ) { event ->
        // Movement Tick will affect the client-side sprint state,
        // while we also apply it to Input to count as pressing the Sprint-Key
        if (event.source == SprintEvent.Source.MOVEMENT_TICK || event.source == SprintEvent.Source.INPUT) {
            when (clientMode) {
                SprintMode.FORCE_SPRINT -> if (event.directionalInput.isMoving) {
                    event.sprint = true
                }

                SprintMode.FORCE_NO_SPRINT -> {
                    event.sprint = false
                }

                SprintMode.DO_NOT_CHANGE -> { }

            }
        }

        // Network and Input both count as Network Type
        // which will make the server think we are not sprinting
        if (event.source == SprintEvent.Source.NETWORK || event.source == SprintEvent.Source.INPUT) {
            when (serverMode) {

                SprintMode.FORCE_SPRINT -> if (event.directionalInput.isMoving) {
                    event.sprint = true
                }

                SprintMode.FORCE_NO_SPRINT -> {
                    event.sprint = false
                }

                SprintMode.DO_NOT_CHANGE -> { }
            }
        }
    }

}
