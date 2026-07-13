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

package net.maxedgar.coffee.features.module.modules.render.trajectories

import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.events.OverlayRenderEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.module.modules.render.TimeUnit
import net.maxedgar.coffee.render.FontManager
import net.maxedgar.coffee.render.engine.font.HorizontalAnchor
import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.render.engine.type.Vec3f
import net.maxedgar.coffee.utils.text.asPlainText
import net.maxedgar.coffee.utils.text.textOf
import net.maxedgar.coffee.utils.math.toFixed
import net.maxedgar.coffee.utils.render.WorldToScreen
import net.maxedgar.coffee.utils.render.trajectory.TrajectoryInfoRenderer
import net.minecraft.world.phys.Vec3

object TrajectoryDetailedInfoRenderer : ToggleableValueGroup(ModuleTrajectories, "ShowDetailedInfo", false) {
    private val showAt by enumChoice("ShowAt", ShowAt.ENTITY)

    private enum class ShowAt(
        override val tag: String,
    ) : Tagged {
        OWNER("Owner"),
        ENTITY("Entity"),
        LANDING("Landing");

        fun apply(
            renderer: TrajectoryInfoRenderer,
            result: TrajectoryInfoRenderer.SimulationResult,
        ): Vec3 = when (this) {
            OWNER -> renderer.simulationOwner.position()
            ENTITY -> result.positions.firstOrNull()
            LANDING -> result.positions.lastOrNull()
        } ?: renderer.simulationOwner.position()
    }

    private val item by boolean("Item", true)
    private val ownerName by boolean("OwnerName", true)
    private val distance by boolean("Distance", true)
    private val timeUnit by enumChoice("TimeUnit", TimeUnit.TICKS, aliases = listOf("DurationUnit"))
    private val color by color("Color", Color4b.WHITE)

    private val scale by float("Scale", 1F, 0.25F..4F)
    private val renderOffset by vec3d("RenderOffset", useLocateButton = false)
    private fun Vec3.calcScreenPosWithOffset(): Vec3f? {
        return WorldToScreen.calculateScreenPos(add(renderOffset))
    }

    private val fontRenderer get() = FontManager.FONT_RENDERER

    val renderHandler = handler<OverlayRenderEvent> { event ->
        with(event.context) {
            ModuleTrajectories.simulationResults.forEachIndexed { index, (renderer, result) ->
                val screenPos =
                    when {
                        showAt === ShowAt.OWNER && renderer.simulationOwner === player -> when (renderer.type) {
                            // If this renderer is created by player holding items and showAt is OWNER,
                            // then show at the landing position
                            TrajectoryInfoRenderer.Type.HYPOTHETICAL ->
                                ShowAt.LANDING.apply(renderer, result).calcScreenPosWithOffset()

                            else -> {
                                val centerX = mc.window.guiScaledWidth * 0.5F
                                val centerY = mc.window.guiScaledHeight * 0.5F
                                Vec3f(centerX + 50F, centerY + index * (mc.font.lineHeight + 1), 0F)
                            }
                        }

                        else -> showAt.apply(renderer, result).calcScreenPosWithOffset()
                    } ?: return@forEachIndexed

                pose().pushMatrix()
                pose().translate(screenPos.x, screenPos.y)
                pose().scale(scale)

                val texts = buildList {
                    add(timeUnit.format(result.positions.size).asPlainText())
                    if (distance && result.positions.isNotEmpty()) {
                        add("Dist: ${player.position().distanceTo(result.positions.last()).toFixed(1)}m".asPlainText())
                    }
                    val displayOwner = renderer.displayOwner
                    if (ownerName && displayOwner != null && displayOwner !== player) {
                        add(textOf("Owner: ".asPlainText(), displayOwner.displayName))
                    }
                }

                if (item) {
                    item(renderer.icon, -8, 0)

                    pose().pushMatrix()
                    pose().translate(0F, 16F)
                }
                val fontRenderer = fontRenderer
                pose().scale(fontRenderer.scaleToVanillaFont)
                var y = 0F

                for (text in texts) {
                    val processedText = fontRenderer.process(text, color)

                    fontRenderer.draw(processedText) {
                        this.y = y
                        horizontalAnchor = HorizontalAnchor.CENTER
                        shadow = true
                    }

                    y += fontRenderer.height + 1f
                }
                if (item) {
                    pose().popMatrix()
                }

                pose().popMatrix()
            }
        }
    }
}
