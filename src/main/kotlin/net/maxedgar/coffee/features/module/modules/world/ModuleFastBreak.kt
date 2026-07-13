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

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.group.NoneMode
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.item.isMiningTool
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket

/**
 * FastBreak module
 *
 * Allows you to break blocks faster.
 */
object ModuleFastBreak : ClientModule("FastBreak", ModuleCategories.WORLD) {

    private const val VANILLA_DESTROY_DELAY = 5

    private val breakDamage by float("BreakDamage", 0.8f, 0.1f..1f)
    private val onlyTool by boolean("OnlyTool", false)

    private val modeChoice = choices("Mode", 0) { arrayOf(NoneMode(it), AbortAnother) }.apply(::tagBy)

    val repeatable = handler<GameTickEvent> {
        if (onlyTool && !player.mainHandItem.isMiningTool) {
            return@handler
        }

        interaction.destroyDelay = 0

        if (interaction.destroyProgress > breakDamage) {
            interaction.destroyProgress = 1f
        }
    }

    override fun onDisabled() {
        interaction.destroyDelay = VANILLA_DESTROY_DELAY
        super.onDisabled()
    }

    /**
     * Bypass Grim 2.3.48 anti-cheat
     * Tested on eu.loyisa.cn
     *
     * https://github.com/GrimAnticheat/Grim/issues/1296
     */
    object AbortAnother : Mode("AbortAnother") {

        override val parent: ModeValueGroup<Mode>
            get() = modeChoice

        val packetHandler = handler<PacketEvent> {
            if (onlyTool && !player.mainHandItem.isMiningTool) {
                return@handler
            }

            val packet = it.packet

            if (packet is ServerboundPlayerActionPacket &&
                packet.action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK
            ) {
                val blockPos = packet.pos

                // Abort block break on the block above (which we are not breaking)
                network.send(
                    ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                        blockPos.above(), packet.direction
                    )
                )
            }
        }

    }

}
