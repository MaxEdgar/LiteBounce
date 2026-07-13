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

package net.maxedgar.coffee.config.types

import net.maxedgar.coffee.config.ConfigSystem.configs
import net.maxedgar.coffee.config.ConfigSystem.rootFolder
import net.maxedgar.coffee.config.types.group.ValueGroup
import java.io.File

open class Config(name: String, value: MutableCollection<Value<*>> = mutableListOf()) : ValueGroup(name, value) {

    val jsonFile: File
        get() {
            require(this in configs) { "${this.name} is not registered" }
            return File(rootFolder, "${this.loweredName}.json")
        }

    /**
     * We write to this temp file, we can safely rename [jsonTmpFile] to [jsonFile],
     * to eliminate any chances of data loss.
     */
    val jsonTmpFile: File
        get() {
            require(this in configs) { "${this.name} is not registered" }
            return File(rootFolder, "${this.loweredName}.json.tmp")
        }

}
