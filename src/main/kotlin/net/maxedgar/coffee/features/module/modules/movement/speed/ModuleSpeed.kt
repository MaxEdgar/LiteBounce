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
package net.maxedgar.coffee.features.module.modules.movement.speed

import net.maxedgar.coffee.config.types.group.Mode
import net.maxedgar.coffee.config.types.group.ModeValueGroup
import net.maxedgar.coffee.config.types.group.ToggleableValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.features.misc.HideAppearance.isDestructed
import net.maxedgar.coffee.features.module.ClientModule
import net.maxedgar.coffee.features.module.ModuleCategories
import net.maxedgar.coffee.features.module.modules.combat.criticals.modes.CriticalsJump
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed.OnlyInCombat.modes
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed.OnlyOnPotionEffect.modes
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed.OnlyOnPotionEffect.potionEffects
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.SpeedCustom
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.SpeedLegitHop
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.SpeedPiercingAttack
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.SpeedSpeedYPort
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.blocksmc.SpeedBlocksMC
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.grim.SpeedGrimCollide
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.hylex.SpeedHylexGround
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.hylex.SpeedHylexLowHop
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.intave.SpeedIntave14
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.intave.SpeedIntave14Fast
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.matrix.SpeedMatrix7
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.ncp.SpeedNCP
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.sentinel.SpeedSentinelDamage
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.spartan.SpeedSpartanV4043
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.spartan.SpeedSpartanV4043FastFall
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.verus.SpeedVerusB3882
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.vulcan.SpeedVulcan286
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.vulcan.SpeedVulcan288
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.vulcan.SpeedVulcanGround286
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.watchdog.SpeedHypixelBHop
import net.maxedgar.coffee.features.module.modules.movement.speed.modes.watchdog.SpeedHypixelLowHop
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.combat.CombatManager
import java.util.function.BooleanSupplier

/**
 * Speed module
 *
 * Allows you to move faster.
 */
object ModuleSpeed : ClientModule("Speed", ModuleCategories.MOVEMENT) {

    /**
     * Initialize speeds choices independently
     *
     * This is useful for the `OnlyOnPotionEffect` choice, which has its own set of modes
     */
    private fun initializeSpeeds(modeValueGroup: ModeValueGroup<*>) = arrayOf(
        SpeedLegitHop(modeValueGroup),
        SpeedCustom(modeValueGroup),
        SpeedSpeedYPort(modeValueGroup),
        SpeedPiercingAttack(modeValueGroup),

        SpeedVerusB3882(modeValueGroup),

        SpeedHypixelBHop(modeValueGroup),
        SpeedHypixelLowHop(modeValueGroup),

        SpeedSpartanV4043(modeValueGroup),
        SpeedSpartanV4043FastFall(modeValueGroup),

        SpeedSentinelDamage(modeValueGroup),

        SpeedVulcan286(modeValueGroup),
        SpeedVulcan288(modeValueGroup),
        SpeedVulcanGround286(modeValueGroup),
        SpeedGrimCollide(modeValueGroup),

        SpeedNCP(modeValueGroup),

        SpeedIntave14(modeValueGroup),
        SpeedIntave14Fast(modeValueGroup),

        SpeedHylexLowHop(modeValueGroup),
        SpeedHylexGround(modeValueGroup),

        SpeedBlocksMC(modeValueGroup),

        SpeedMatrix7(modeValueGroup)
    )

    val modes = choices("Mode", 0, this::initializeSpeeds).apply(::tagBy)

    private val notCondition by multiEnumChoice("Not", NotCondition.SCAFFOLD)

    private val avoidEdgeBump by boolean("AvoidEdgeBump", true)

    init {
        tree(OnlyInCombat)
        tree(OnlyOnPotionEffect)
        tree(SpeedYawOffset)
    }

    override val running: Boolean
        get() {
            // Early return if the module is not ready to be used -
            // prevents accessing player when it's null below
            // in case it was forgotten to be checked
            return when {
                !(super.running || ModuleScaffold.running && ModuleScaffold.autoSpeed) -> false
                !passesRequirements() -> false
                OnlyInCombat.enabled && CombatManager.isInCombat -> false
                OnlyOnPotionEffect.enabled && potionEffects.activeMode.checkPotionEffects() -> false
                else -> true
            }
        }

    private fun passesRequirements() = when {
        // DO NOT REMOVE - PLAYER COULD BE NULL!
        !inGame || isDestructed -> false
        else -> !notCondition.any { it.condition.asBoolean }
    }

    private object OnlyInCombat : ToggleableValueGroup(this, "OnlyInCombat", false) {

        val modes = modes(this, "Mode", activeIndex = 0, ModuleSpeed::initializeSpeeds)

        /**
         * Controls [modes] activation state.
         */
        override val running: Boolean
            get() = when {
                !inGame || isDestructed -> false
                !ModuleSpeed.enabled || !this.enabled || !passesRequirements() -> false
                else -> CombatManager.isInCombat
            }

    }

    private object OnlyOnPotionEffect : ToggleableValueGroup(this, "OnlyOnPotionEffect", false) {

        val potionEffects = modes(
            this,
            "PotionEffect",
            SpeedPotionEffectMode,
            arrayOf(SpeedPotionEffectMode, SlownessPotionEffectMode, BothEffectsMode)
        )

        val modes = modes(this, "Mode", activeIndex = 0, ModuleSpeed::initializeSpeeds)

        /**
         * Controls [modes] activation state.
         */
        override val running: Boolean
            get() = when {
                !inGame || isDestructed -> false
                !ModuleSpeed.enabled || !this.enabled || !passesRequirements() -> false
                else -> potionEffects.activeMode.checkPotionEffects()
            }

    }

    abstract class PotionEffectMode(name: String) : Mode(name) {
        override val parent: ModeValueGroup<PotionEffectMode>
            get() = potionEffects

        abstract fun checkPotionEffects(): Boolean
    }

    @Suppress("ReturnCount", "MagicNumber")
    internal fun doOptimizationsPreventJump(): Boolean {
        if (CriticalsJump.running && CriticalsJump.shouldWaitForJump(0.42f)) {
            return true
        }

        if (avoidEdgeBump && SpeedAntiCornerBump.shouldDelayJump()) {
            return true
        }

        return false
    }

    @Suppress("unused")
    private enum class NotCondition(
        override val tag: String,
        val condition: BooleanSupplier
    ) : Tagged {
        USING_ITEM("WhileUsingItem", { player.isUsingItem }),
        SCAFFOLD("DuringScaffold", { ModuleScaffold.running || ModuleFly.running }),
        SNEAKING("WhileSneaking", { player.isShiftKeyDown }),
        FALL_FLYING("IsFallFlying", { player.isFallFlying }),
        IN_LIQUID("IsInLiquid", { player.isInLiquid }),
    }
}
