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

package net.maxedgar.coffee.utils.client

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class NameGeneratorKtTest {
    @Test
    fun testUsernameLength() {
        val rng = Random(1337)

        val alreadySeenUsernames = HashSet<String>()

        for (i in 0..1000) {
            val randomUsername = randomUsername(16, rng)

            assertTrue(randomUsername.length in 3..16) { "'$randomUsername' does not fit size requirements. [$i]" }
            assertTrue(alreadySeenUsernames.add(randomUsername)) { "'$randomUsername' was generated twice [$i]" }
        }
        println(alreadySeenUsernames)
    }
}
