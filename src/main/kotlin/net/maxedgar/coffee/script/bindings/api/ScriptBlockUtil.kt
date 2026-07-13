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
package net.maxedgar.coffee.script.bindings.api

import net.maxedgar.coffee.utils.block.getBlock
import net.maxedgar.coffee.utils.block.state
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

/**
 * Object used by the script API to provide an
 */
@Suppress("unused")
object ScriptBlockUtil {

    @JvmName("newBlockPos")
    fun newBlockPos(x: Int, y: Int, z: Int): BlockPos = BlockPos(x, y, z)

    @JvmName("getBlock")
    fun getBlock(blockPos: BlockPos) = blockPos.getBlock()

    @JvmName("getState")
    fun getState(blockPos: BlockPos): BlockState? = blockPos.state

}
