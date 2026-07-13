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
package net.maxedgar.coffee.features.module.modules.render

import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.render.placement.PlacementRenderer
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket
import net.minecraft.world.entity.item.FallingBlockEntity

object ModuleProphuntESP : ClientModule("ProphuntESP", ModuleCategories.RENDER,
    aliases = listOf("BlockUpdateDetector", "FallingBlockESP")
) {

    private val renderer = PlacementRenderer("RenderBlockUpdates", true, this,
        defaultColor = Color4b(255, 179, 72, 90), keep = false
    )

    private val tracking by multiEnumChoice("Tracking", Tracking.entries, canBeNone = false)

    private enum class Tracking(override val tag: String): Tagged {
        FALLING_BLOCKS("FallingBlocks"),
        BLOCK_UPDATES("BlockUpdates"),
        CHUNK_DELTA_UPDATES("ChunkDeltaUpdates"),
    }

    init {
        tree(renderer)
    }

    override fun onDisabled() {
        renderer.clearSilently()
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        if (Tracking.FALLING_BLOCKS in tracking) {
            for (entity in world.entitiesForRendering()) {
                if (entity is FallingBlockEntity) {
                    renderer.addBlock(entity.blockPosition(), update = false)
                }
            }
        }
        renderer.updateAll()
    }

    @Suppress("unused")
    private val networkHandler = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is ClientboundBlockUpdatePacket if Tracking.BLOCK_UPDATES in tracking -> mc.execute {
                renderer.addBlock(packet.pos, update = false)
            }

            is ClientboundSectionBlocksUpdatePacket if Tracking.CHUNK_DELTA_UPDATES in tracking -> mc.execute {
                packet.runUpdates { pos, _ -> renderer.addBlock(pos, update = false) }
            }
        }
    }
}
