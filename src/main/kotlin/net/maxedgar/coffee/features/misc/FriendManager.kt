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
package net.maxedgar.coffee.features.misc

import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.types.Config
import net.maxedgar.coffee.config.types.ValueType
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.AttackEntityEvent
import net.maxedgar.coffee.event.events.TagEntityEvent
import net.maxedgar.coffee.event.handler
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import java.util.TreeSet

object FriendManager : Config("Friends"), EventListener {

    val friends by list(name, TreeSet<Friend>(), valueType = ValueType.FRIEND)

    private val cancelAttack by boolean("CancelAttack", false)

    @Suppress("unused")
    private val tagEntityEvent = handler<TagEntityEvent> {
        if (isFriend(it.entity)) {
            it.assumeFriend()
        }
    }

    @Suppress("unused")
    private val onAttack = handler<AttackEntityEvent> {
        if (cancelAttack && isFriend(it.entity)) {
            it.cancelEvent()
        }
    }

    init {
        ConfigSystem.root(this)
    }

    class Friend(val name: String, var alias: String?) : Comparable<Friend> {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Friend

            return name == other.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }

        override fun compareTo(other: Friend): Int = this.name.compareTo(other.name)

        fun getDefaultName(id: Int): String = "Friend $id"

    }

    fun isFriend(name: String): Boolean = friends.contains(Friend(name, null))
    fun isFriend(entity: Entity): Boolean = entity is Player && isFriend(entity.gameProfile.name)

}
