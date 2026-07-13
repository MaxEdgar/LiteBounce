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

package net.maxedgar.coffee.features.module.modules.player.autoqueue.actions

import kotlinx.coroutines.delay
import net.maxedgar.coffee.utils.network.sendChatOrCommand
import kotlin.time.Duration.Companion.milliseconds

object AutoQueueActionChat : AutoQueueAction("Chat") {
    private val startDelay by intRange("StartDelay", 0..0, 0..5000, "ms")
    private val messageDelay by intRange("MessageDelay", 0..0, 0..5000, "ms")

    private val messages by textList("Messages", arrayListOf("/play solo_normal"))

    override suspend fun execute() {
        var flag = true

        for (message in messages) {
            val delayMs = if (flag) {
                flag = false
                startDelay.random()
            } else {
                messageDelay.random()
            }

            delay(delayMs.milliseconds)

            network.sendChatOrCommand(message)
        }
    }
}
