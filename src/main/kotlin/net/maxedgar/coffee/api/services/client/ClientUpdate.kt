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
package net.maxedgar.coffee.api.services.client

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.async
import net.maxedgar.coffee.Coffee
import net.maxedgar.coffee.api.core.ioScope
import net.maxedgar.coffee.utils.client.GitInfo
import net.maxedgar.coffee.utils.client.logger
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object ClientUpdate {

    val update = ioScope.async {
        runCatching {
            val newestBuild = runCatching {
                ClientApi.requestNewestBuildEndpoint(
                    branch = Coffee.clientBranch,
                    release = !Coffee.IN_DEVELOPMENT
                )
            }.onFailure { exception ->
                logger.error("Unable to receive update information", exception)
            }.getOrNull() ?: return@async null

            val newestSemVersion = Semver(newestBuild.lbVersion, Semver.SemverType.LOOSE)

            val isNewer = if (Coffee.IN_DEVELOPMENT) { // check if new build is newer than current build
                val newestVersionDate = newestBuild.date
                val currentVersionDate = OffsetDateTime.parse(
                    GitInfo.get("git.commit.time"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                )

                newestVersionDate > currentVersionDate
            } else {
                // check if version number is higher than current version number (on release builds only!)
                val clientSemVersion = Semver(Coffee.clientVersion, Semver.SemverType.LOOSE)

                newestBuild.release && newestSemVersion.isGreaterThan(clientSemVersion)
            }

            if (isNewer) {
                newestBuild
            } else {
                null
            }
        }.onFailure { exception ->
            logger.error("Failed to check for update", exception)
        }.getOrNull()
    }

}
