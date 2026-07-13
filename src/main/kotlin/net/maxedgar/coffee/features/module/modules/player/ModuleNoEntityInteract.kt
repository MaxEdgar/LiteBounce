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

package net.maxedgar.coffee.features.module.modules.player

import net.ccbluex.fastutil.objectRBTreeSetOf
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.utils.collection.Filter
import net.maxedgar.coffee.utils.collection.asComparator
import net.maxedgar.coffee.utils.collection.itemSortedSetOf
import net.maxedgar.coffee.utils.item.isMiningTool
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.phys.EntityHitResult
import java.util.SequencedSet

/**
 * Skip crosshair entity targets.
 */
object ModuleNoEntityInteract : ClientModule("NoEntityInteract", ModuleCategories.PLAYER) {

    private fun defaultEntityTypes(): SequencedSet<EntityType<*>> {
        return objectRBTreeSetOf(
            BuiltInRegistries.ENTITY_TYPE.asComparator(),
            EntityTypes.VILLAGER, EntityTypes.ARMOR_STAND
        )
    }

    private fun defaultHoldingItems(): SequencedSet<Item> {
        val set = itemSortedSetOf(
            Items.AIR, Items.SHEARS, Items.TNT, Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.COBWEB
        )
        BuiltInRegistries.ITEM.filterTo(set) {
            it !in set && ItemStackTemplate(it).isMiningTool
        }
        return set
    }

    private val entityTypeFilter by enumChoice("EntityTypeFilter", Filter.BLACKLIST)
    private val entityTypes by entityTypes("EntityTypes", defaultEntityTypes())

    private val holdingItemFilter by enumChoice("HoldingItemFilter", Filter.WHITELIST)
    private val holdingItems by items("HoldingItems", defaultHoldingItems())

    fun test(entity: EntityHitResult): Boolean {
        return !running ||
            entityTypeFilter(entity.entity, entityTypes) &&
            holdingItemFilter(player.mainHandItem.item, holdingItems)
    }

}
