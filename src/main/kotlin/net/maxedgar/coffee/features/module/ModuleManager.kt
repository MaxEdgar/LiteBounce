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
package net.maxedgar.coffee.features.module

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import net.ccbluex.fastutil.mapToArray
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.autoconfig.AutoConfig
import net.maxedgar.coffee.config.types.VALUE_NAME_ORDER
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.events.DisconnectEvent
import net.maxedgar.coffee.event.events.KeyboardKeyEvent
import net.maxedgar.coffee.event.events.MouseButtonEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.event.sequenceHandler
import net.maxedgar.coffee.event.tickUntil
import net.maxedgar.coffee.features.module.modules.combat.ModuleAimbot
import net.maxedgar.coffee.features.module.modules.combat.ModuleAutoClicker
import net.maxedgar.coffee.features.module.modules.world.automobheal.AutoMobHeal
import net.maxedgar.coffee.features.module.modules.combat.ModuleAutoLeave
import net.maxedgar.coffee.features.module.modules.combat.ModuleAutoRod
import net.maxedgar.coffee.features.module.modules.combat.ModuleAutoShoot
import net.maxedgar.coffee.features.module.modules.combat.ModuleAutoWeapon
import net.maxedgar.coffee.features.module.modules.combat.ModuleFakeLag
import net.maxedgar.coffee.features.module.modules.combat.ModuleHitbox
import net.maxedgar.coffee.features.module.modules.combat.ModuleKeepSprint
import net.maxedgar.coffee.features.module.modules.combat.ModuleMaceKill
import net.maxedgar.coffee.features.module.modules.combat.ModuleNoMissCooldown
import net.maxedgar.coffee.features.module.modules.combat.ModuleSuperKnockback
import net.maxedgar.coffee.features.module.modules.combat.ModuleSwordBlock
import net.maxedgar.coffee.features.module.modules.combat.ModuleTickBase
import net.maxedgar.coffee.features.module.modules.combat.ModuleTimerRange
import net.maxedgar.coffee.features.module.modules.combat.aimbot.ModuleAutoBow
import net.maxedgar.coffee.features.module.modules.combat.autoarmor.ModuleAutoArmor
import net.maxedgar.coffee.features.module.modules.combat.backtrack.ModuleBacktrack
import net.maxedgar.coffee.features.module.modules.combat.criticals.ModuleCriticals
import net.maxedgar.coffee.features.module.modules.combat.crystalaura.ModuleCrystalAura
import net.maxedgar.coffee.features.module.modules.combat.elytratarget.ModuleElytraTarget
import net.maxedgar.coffee.features.module.modules.combat.killaura.ModuleKillAura
import net.maxedgar.coffee.features.module.modules.combat.tpaura.ModuleTpAura
import net.maxedgar.coffee.features.module.modules.combat.velocity.ModuleVelocity
import net.maxedgar.coffee.features.module.modules.exploit.ModuleAbortBreaking
import net.maxedgar.coffee.features.module.modules.exploit.ModuleAntiHunger
import net.maxedgar.coffee.features.module.modules.exploit.ModuleAntiReducedDebugInfo
import net.maxedgar.coffee.features.module.modules.exploit.ModuleClickTp
import net.maxedgar.coffee.features.module.modules.exploit.ModuleClip
import net.maxedgar.coffee.features.module.modules.exploit.ModuleDamage
import net.maxedgar.coffee.features.module.modules.exploit.ModuleExtendedFirework
import net.maxedgar.coffee.features.module.modules.exploit.ModuleGhostHand
import net.maxedgar.coffee.features.module.modules.exploit.ModuleKick
import net.maxedgar.coffee.features.module.modules.exploit.ModuleMoreCarry
import net.maxedgar.coffee.features.module.modules.exploit.ModuleMultiActions
import net.maxedgar.coffee.features.module.modules.exploit.ModuleNameCollector
import net.maxedgar.coffee.features.module.modules.exploit.ModuleNoPitchLimit
import net.maxedgar.coffee.features.module.modules.exploit.ModulePingSpoof
import net.maxedgar.coffee.features.module.modules.exploit.ModulePlugins
import net.maxedgar.coffee.features.module.modules.exploit.ModulePortalMenu
import net.maxedgar.coffee.features.module.modules.exploit.ModuleResetVL
import net.maxedgar.coffee.features.module.modules.exploit.ModuleSleepWalker
import net.maxedgar.coffee.features.module.modules.exploit.ModuleTimeShift
import net.maxedgar.coffee.features.module.modules.exploit.ModuleVehicleOneHit
import net.maxedgar.coffee.features.module.modules.exploit.ModuleYggdrasilSignatureFix
import net.maxedgar.coffee.features.module.modules.exploit.disabler.ModuleDisabler
import net.maxedgar.coffee.features.module.modules.exploit.dupe.ModuleDupe
import net.maxedgar.coffee.features.module.modules.exploit.phase.ModulePhase
import net.maxedgar.coffee.features.module.modules.exploit.servercrasher.ModuleServerCrasher
import net.maxedgar.coffee.features.module.modules.`fun`.ModuleDankBobbing
import net.maxedgar.coffee.features.module.modules.`fun`.ModuleDerp
import net.maxedgar.coffee.features.module.modules.`fun`.ModuleHandDerp
import net.maxedgar.coffee.features.module.modules.`fun`.ModuleSkinDerp
import net.maxedgar.coffee.features.module.modules.`fun`.ModuleTwerk
import net.maxedgar.coffee.features.module.modules.`fun`.ModuleVomit
import net.maxedgar.coffee.features.module.modules.`fun`.notebot.ModuleNotebot
import net.maxedgar.coffee.features.module.modules.misc.ModuleAntiCheatDetect
import net.maxedgar.coffee.features.module.modules.misc.ModuleAntiStaff
import net.maxedgar.coffee.features.module.modules.misc.ModuleAutoAccount
import net.maxedgar.coffee.features.module.modules.misc.ModuleAutoChatGame
import net.maxedgar.coffee.features.module.modules.misc.ModuleAutoConfig
import net.maxedgar.coffee.features.module.modules.misc.ModuleAutoPearl
import net.maxedgar.coffee.features.module.modules.misc.ModuleBetterTab
import net.maxedgar.coffee.features.module.modules.misc.ModuleBookBot
import net.maxedgar.coffee.features.module.modules.misc.ModuleEasyPearl
import net.maxedgar.coffee.features.module.modules.misc.ModuleElytraSwap
import net.maxedgar.coffee.features.module.modules.misc.ModuleFlagCheck
import net.maxedgar.coffee.features.module.modules.misc.ModuleGUICloser
import net.maxedgar.coffee.features.module.modules.misc.ModuleInventoryTracker
import net.maxedgar.coffee.features.module.modules.misc.ModuleItemScroller
import net.maxedgar.coffee.features.module.modules.misc.ModuleMacros
import net.maxedgar.coffee.features.module.modules.misc.ModuleMiddleClickAction
import net.maxedgar.coffee.features.module.modules.misc.ModuleNotifier
import net.maxedgar.coffee.features.module.modules.misc.ModulePacketLogger
import net.maxedgar.coffee.features.module.modules.misc.ModuleSpammer
import net.maxedgar.coffee.features.module.modules.misc.ModuleTargetLock
import net.maxedgar.coffee.features.module.modules.misc.ModuleTeams
import net.maxedgar.coffee.features.module.modules.misc.ModuleTextFieldProtect
import net.maxedgar.coffee.features.module.modules.misc.antibot.ModuleAntiBot
import net.maxedgar.coffee.features.module.modules.misc.betterchat.ModuleBetterChat
import net.maxedgar.coffee.features.module.modules.misc.debugrecorder.ModuleDebugRecorder
import net.maxedgar.coffee.features.module.modules.misc.nameprotect.ModuleNameProtect
import net.maxedgar.coffee.features.module.modules.misc.reporthelper.ModuleReportHelper
import net.maxedgar.coffee.features.module.modules.movement.ModuleAirJump
import net.maxedgar.coffee.features.module.modules.movement.ModuleAnchor
import net.maxedgar.coffee.features.module.modules.movement.ModuleAntiBounce
import net.maxedgar.coffee.features.module.modules.movement.ModuleAntiLevitation
import net.maxedgar.coffee.features.module.modules.movement.ModuleAvoidHazards
import net.maxedgar.coffee.features.module.modules.movement.ModuleBlockBounce
import net.maxedgar.coffee.features.module.modules.movement.ModuleBlockWalk
import net.maxedgar.coffee.features.module.modules.movement.ModuleElytraRecast
import net.maxedgar.coffee.features.module.modules.movement.ModuleEntityControl
import net.maxedgar.coffee.features.module.modules.movement.ModuleFreeze
import net.maxedgar.coffee.features.module.modules.movement.ModuleNoClip
import net.maxedgar.coffee.features.module.modules.movement.ModuleNoJumpDelay
import net.maxedgar.coffee.features.module.modules.movement.ModuleNoPose
import net.maxedgar.coffee.features.module.modules.movement.ModuleNoPush
import net.maxedgar.coffee.features.module.modules.movement.ModuleParkour
import net.maxedgar.coffee.features.module.modules.movement.ModuleSafeWalk
import net.maxedgar.coffee.features.module.modules.movement.ModuleSneak
import net.maxedgar.coffee.features.module.modules.movement.ModuleSprint
import net.maxedgar.coffee.features.module.modules.movement.ModuleStrafe
import net.maxedgar.coffee.features.module.modules.movement.ModuleTargetStrafe
import net.maxedgar.coffee.features.module.modules.movement.ModuleTeleport
import net.maxedgar.coffee.features.module.modules.movement.ModuleVehicleBoost
import net.maxedgar.coffee.features.module.modules.movement.ModuleVehicleControl
import net.maxedgar.coffee.features.module.modules.movement.ModuleSnapTap
import net.maxedgar.coffee.features.module.modules.movement.autododge.ModuleAutoDodge
import net.maxedgar.coffee.features.module.modules.movement.elytrafly.ModuleElytraFly
import net.maxedgar.coffee.features.module.modules.movement.fly.ModuleFly
import net.maxedgar.coffee.features.module.modules.movement.highjump.ModuleHighJump
import net.maxedgar.coffee.features.module.modules.movement.inventorymove.ModuleInventoryMove
import net.maxedgar.coffee.features.module.modules.movement.liquidwalk.ModuleLiquidWalk
import net.maxedgar.coffee.features.module.modules.movement.longjump.ModuleLongJump
import net.maxedgar.coffee.features.module.modules.movement.noslow.ModuleNoSlow
import net.maxedgar.coffee.features.module.modules.movement.noweb.ModuleNoWeb
import net.maxedgar.coffee.features.module.modules.movement.speed.ModuleSpeed
import net.maxedgar.coffee.features.module.modules.movement.spider.ModuleSpider
import net.maxedgar.coffee.features.module.modules.movement.step.ModuleReverseStep
import net.maxedgar.coffee.features.module.modules.movement.step.ModuleStep
import net.maxedgar.coffee.features.module.modules.movement.terrainspeed.ModuleTerrainSpeed
import net.maxedgar.coffee.features.module.modules.player.ModuleAntiAFK
import net.maxedgar.coffee.features.module.modules.player.ModuleAntiExploit
import net.maxedgar.coffee.features.module.modules.player.ModuleAutoBreak
import net.maxedgar.coffee.features.module.modules.player.ModuleAutoFish
import net.maxedgar.coffee.features.module.modules.player.ModuleAutoRespawn
import net.maxedgar.coffee.features.module.modules.player.ModuleAutoWalk
import net.maxedgar.coffee.features.module.modules.player.ModuleAutoWindCharge
import net.maxedgar.coffee.features.module.modules.player.ModuleBlink
import net.maxedgar.coffee.features.module.modules.player.ModuleChestCleaner
import net.maxedgar.coffee.features.module.modules.player.ModuleEagle
import net.maxedgar.coffee.features.module.modules.player.ModuleFastExp
import net.maxedgar.coffee.features.module.modules.player.ModuleFastUse
import net.maxedgar.coffee.features.module.modules.player.ModuleNoBlockInteract
import net.maxedgar.coffee.features.module.modules.player.ModuleNoEntityInteract
import net.maxedgar.coffee.features.module.modules.player.ModuleNoRotateSet
import net.maxedgar.coffee.features.module.modules.player.ModuleNoSlotSet
import net.maxedgar.coffee.features.module.modules.player.ModulePotionSpoof
import net.maxedgar.coffee.features.module.modules.player.ModuleReach
import net.maxedgar.coffee.features.module.modules.player.ModuleReplenish
import net.maxedgar.coffee.features.module.modules.player.ModuleSmartEat
import net.maxedgar.coffee.features.module.modules.player.antivoid.ModuleAntiVoid
import net.maxedgar.coffee.features.module.modules.player.autobuff.ModuleAutoBuff
import net.maxedgar.coffee.features.module.modules.player.ModuleAutoCrafter
import net.maxedgar.coffee.features.module.modules.player.autoqueue.ModuleAutoQueue
import net.maxedgar.coffee.features.module.modules.player.autoshop.ModuleAutoShop
import net.maxedgar.coffee.features.module.modules.player.cheststealer.ModuleChestStealer
import net.maxedgar.coffee.features.module.modules.player.invcleaner.ModuleInventoryCleaner
import net.maxedgar.coffee.features.module.modules.player.nofall.ModuleNoFall
import net.maxedgar.coffee.features.module.modules.player.offhand.ModuleOffhand
import net.maxedgar.coffee.features.module.modules.render.ModuleAnimations
import net.maxedgar.coffee.features.module.modules.render.ModuleAntiBlind
import net.maxedgar.coffee.features.module.modules.render.ModuleAspect
import net.maxedgar.coffee.features.module.modules.render.ModuleAutoF5
import net.maxedgar.coffee.features.module.modules.render.ModuleBedPlates
import net.maxedgar.coffee.features.module.modules.render.ModuleBetterInventory
import net.maxedgar.coffee.features.module.modules.render.ModuleBlockESP
import net.maxedgar.coffee.features.module.modules.render.ModuleBlockOutline
import net.maxedgar.coffee.features.module.modules.render.ModuleBreadcrumbs
import net.maxedgar.coffee.features.module.modules.render.ModuleChams
import net.maxedgar.coffee.features.module.modules.render.ModuleClickGui
import net.maxedgar.coffee.features.module.modules.render.ModuleCombineMobs
import net.maxedgar.coffee.features.module.modules.render.ModuleCrystalView
import net.maxedgar.coffee.features.module.modules.render.ModuleCustomAmbience
import net.maxedgar.coffee.features.module.modules.render.ModuleDamageParticles
import net.maxedgar.coffee.features.module.modules.render.ModuleDebug
import net.maxedgar.coffee.features.module.modules.render.ModuleFreeCam
import net.maxedgar.coffee.features.module.modules.render.ModuleFreeLook
import net.maxedgar.coffee.features.module.modules.render.ModuleFullBright
import net.maxedgar.coffee.features.module.modules.render.ModuleHoleESP
import net.maxedgar.coffee.features.module.modules.render.ModuleHud
import net.maxedgar.coffee.features.module.modules.render.ModuleItemChams
import net.maxedgar.coffee.features.module.modules.render.ModuleItemESP
import net.maxedgar.coffee.features.module.modules.render.ModuleItemTags
import net.maxedgar.coffee.features.module.modules.render.ModuleJumpEffect
import net.maxedgar.coffee.features.module.modules.render.ModuleLogoffSpot
import net.maxedgar.coffee.features.module.modules.render.ModuleMobOwners
import net.maxedgar.coffee.features.module.modules.render.ModuleNewChunks
import net.maxedgar.coffee.features.module.modules.render.ModuleNoBob
import net.maxedgar.coffee.features.module.modules.render.ModuleNoFov
import net.maxedgar.coffee.features.module.modules.render.ModuleNoHurtCam
import net.maxedgar.coffee.features.module.modules.render.ModuleNoSwing
import net.maxedgar.coffee.features.module.modules.render.ModuleParticles
import net.maxedgar.coffee.features.module.modules.render.ModuleProphuntESP
import net.maxedgar.coffee.features.module.modules.render.ModuleProtectionZones
import net.maxedgar.coffee.features.module.modules.render.ModuleQuickPerspectiveSwap
import net.maxedgar.coffee.features.module.modules.render.ModuleRadar
import net.maxedgar.coffee.features.module.modules.render.ModuleRotations
import net.maxedgar.coffee.features.module.modules.render.ModuleSilentHotbar
import net.maxedgar.coffee.features.module.modules.render.ModuleSkinChanger
import net.maxedgar.coffee.features.module.modules.render.ModuleSmoothCamera
import net.maxedgar.coffee.features.module.modules.render.ModuleStorageESP
import net.maxedgar.coffee.features.module.modules.render.ModuleTNTTimer
import net.maxedgar.coffee.features.module.modules.render.ModuleTracers
import net.maxedgar.coffee.features.module.modules.render.ModuleTrueSight
import net.maxedgar.coffee.features.module.modules.render.ModuleVoidESP
import net.maxedgar.coffee.features.module.modules.render.ModuleXRay
import net.maxedgar.coffee.features.module.modules.render.ModuleZoom
import net.maxedgar.coffee.features.module.modules.render.cameraclip.ModuleCameraClip
import net.maxedgar.coffee.features.module.modules.render.crosshair.ModuleCrosshair
import net.maxedgar.coffee.features.module.modules.render.esp.ModuleESP
import net.maxedgar.coffee.features.module.modules.render.hats.ModuleHats
import net.maxedgar.coffee.features.module.modules.render.hitfx.ModuleHitFX
import net.maxedgar.coffee.features.module.modules.render.murdermystery.ModuleMurderMystery
import net.maxedgar.coffee.features.module.modules.render.nametags.ModuleNametags
import net.maxedgar.coffee.features.module.modules.render.trajectories.ModuleTrajectories
import net.maxedgar.coffee.features.module.modules.world.ModuleAirPlace
import net.maxedgar.coffee.features.module.modules.world.ModuleAutoDisable
import net.maxedgar.coffee.features.module.modules.world.ModuleAutoTool
import net.maxedgar.coffee.features.module.modules.world.ModuleBedDefender
import net.maxedgar.coffee.features.module.modules.world.ModuleBlockIn
import net.maxedgar.coffee.features.module.modules.world.ModuleBlockTrap
import net.maxedgar.coffee.features.module.modules.world.ModuleExtinguish
import net.maxedgar.coffee.features.module.modules.world.ModuleFastBreak
import net.maxedgar.coffee.features.module.modules.world.ModuleFastPlace
import net.maxedgar.coffee.features.module.modules.world.ModuleHoleFiller
import net.maxedgar.coffee.features.module.modules.world.ModuleLiquidFiller
import net.maxedgar.coffee.features.module.modules.world.ModuleLiquidPlace
import net.maxedgar.coffee.features.module.modules.world.ModuleNoInterpolation
import net.maxedgar.coffee.features.module.modules.world.ModuleNoSlowBreak
import net.maxedgar.coffee.features.module.modules.world.ModuleProjectilePuncher
import net.maxedgar.coffee.features.module.modules.world.ModuleStrongholdFinder
import net.maxedgar.coffee.features.module.modules.world.ModuleSurround
import net.maxedgar.coffee.features.module.modules.world.ModuleTimer
import net.maxedgar.coffee.features.module.modules.world.autobuild.ModuleAutoBuild
import net.maxedgar.coffee.features.module.modules.world.autofarm.ModuleAutoFarm
import net.maxedgar.coffee.features.module.modules.world.fucker.ModuleFucker
import net.maxedgar.coffee.features.module.modules.world.nuker.ModuleNuker
import net.maxedgar.coffee.features.module.modules.world.packetmine.ModulePacketMine
import net.maxedgar.coffee.features.module.modules.world.scaffold.ModuleScaffold
import net.maxedgar.coffee.features.module.modules.world.traps.ModuleAutoTrap
import net.maxedgar.coffee.script.ScriptApiRequired
import net.maxedgar.coffee.utils.client.clientStartDurationMs
import net.maxedgar.coffee.utils.client.inGame
import net.maxedgar.coffee.utils.client.logger
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.input.InputBind
import org.lwjgl.glfw.GLFW

private val modules = ObjectRBTreeSet<ClientModule>(VALUE_NAME_ORDER)

/**
 * A fairly simple module manager
 */
object ModuleManager : EventListener, Collection<ClientModule> by modules {

    val modulesConfig = ConfigSystem.root("modules", modules)

    private const val SMART_MOUSE_HOLD_THRESHOLD_MS = 200L

    private enum class SmartBindKeyboardState {
        PENDING_ENABLED, PENDING_DISABLED, HOLDING,
    }
    private class SmartBindMouseState(val pendingEnabled: Boolean, val pressTimestamp: Long)

    private val smartKeyboardStates = Reference2ObjectArrayMap<ClientModule, SmartBindKeyboardState>()
    private val smartMouseStates = Reference2ObjectArrayMap<ClientModule, SmartBindMouseState>()

    /**
     * Handles keystrokes for module binds.
     * This also runs in GUIs, so that if a GUI is opened while a key is pressed,
     * any modules that need to be disabled on key release will be properly disabled.
     */
    @Suppress("unused")
    private val keyboardKeyHandler = handler<KeyboardKeyEvent> { event ->
        when (event.action) {
            GLFW.GLFW_PRESS -> if (mc.gui.screen() == null) {
                // Usually nobody actually wants a module to activate when they press the Minecraft debug key combo.
                if (mc.options.keyDebugModifier.isDown) return@handler
                for (m in modules) {
                    if (!m.bind.matchesKeyPress(event)) {
                        continue
                    }

                    when (m.bind.action) {
                        InputBind.BindAction.TOGGLE -> m.enabled = !m.enabled
                        InputBind.BindAction.HOLD -> m.enabled = true
                        InputBind.BindAction.SMART -> {
                            smartKeyboardStates[m] = if (m.enabled) {
                                SmartBindKeyboardState.PENDING_ENABLED
                            } else {
                                SmartBindKeyboardState.PENDING_DISABLED
                            }
                            m.enabled = true
                        }
                    }
                }
            }

            GLFW.GLFW_REPEAT -> {
                for (m in modules) {
                    if (m.bind.action != InputBind.BindAction.SMART ||
                        !m.bind.matchesKey(event.keyCode, event.scanCode) ||
                        m !in smartKeyboardStates
                    ) {
                        continue
                    }

                    smartKeyboardStates[m] = SmartBindKeyboardState.HOLDING
                }
            }

            GLFW.GLFW_RELEASE -> {
                for (m in modules) {
                    if (!m.bind.matchesKeyRelease(event)) {
                        continue
                    }

                    when (m.bind.action) {
                        InputBind.BindAction.HOLD -> m.enabled = false

                        InputBind.BindAction.SMART -> {
                            val stateBeforePress = smartKeyboardStates.remove(m) ?: continue
                            m.enabled = stateBeforePress == SmartBindKeyboardState.PENDING_DISABLED
                        }

                        InputBind.BindAction.TOGGLE -> {}
                    }
                }
            }
        }
    }

    @Suppress("unused")
    private val mouseButtonHandler = handler<MouseButtonEvent> { event ->
        when (event.action) {
            GLFW.GLFW_PRESS -> if (mc.gui.screen() == null) {
                for (m in modules) {
                    if (!m.bind.matchesMousePress(event)) {
                        continue
                    }

                    when (m.bind.action) {
                        InputBind.BindAction.TOGGLE -> m.enabled = !m.enabled
                        InputBind.BindAction.HOLD -> m.enabled = true
                        InputBind.BindAction.SMART -> {
                            smartMouseStates[m] = SmartBindMouseState(m.enabled, clientStartDurationMs)
                            m.enabled = true
                        }
                    }
                }
            }

            GLFW.GLFW_RELEASE -> {
                for (m in modules) {
                    if (!m.bind.matchesMouseRelease(event)) {
                        continue
                    }

                    when (m.bind.action) {
                        InputBind.BindAction.HOLD -> m.enabled = false

                        InputBind.BindAction.SMART -> {
                            val state = smartMouseStates.remove(m) ?: continue

                            // Mouse button events do not emit GLFW_REPEAT, so SMART falls back to:
                            // - hold if the press was long enough
                            // - toggle otherwise
                            val shouldFallbackToHold =
                                clientStartDurationMs - state.pressTimestamp >= SMART_MOUSE_HOLD_THRESHOLD_MS

                            if (shouldFallbackToHold) {
                                m.enabled = false
                            } else {
                                m.enabled = !state.pendingEnabled
                            }
                        }

                        InputBind.BindAction.TOGGLE -> {}
                    }
                }
            }
        }
    }

    /**
     * Handles world change and enables modules that are not enabled yet
     */
    @Suppress("unused")
    private val handleWorldChange = sequenceHandler<WorldChangeEvent> { event ->
        // Delayed start handling
        if (event.world != null) {
            tickUntil { inGame }
            AutoConfig.withLoading {
                for (module in modules) {
                    if (!module.enabled || module.calledSinceStartup) continue

                    try {
                        module.calledSinceStartup = true
                        // inGame is false here, so use onToggle0
                        module.onToggled(true)
                    } catch (e: Exception) {
                        logger.error("Failed to enable module ${module.name}", e)
                    }
                }
            }
        }

        // Store modules configuration after world change, happens on disconnect as well
        ConfigSystem.store(modulesConfig)
    }

    /**
     * Handles disconnect and if [ClientModule.disableOnQuit] is true disables module
     */
    @Suppress("unused")
    private val handleDisconnect = handler<DisconnectEvent> {
        for (module in modules) {
            if (module.disableOnQuit) {
                try {
                    module.enabled = false
                } catch (e: Exception) {
                    logger.error("Failed to disable module ${module.name}", e)
                }
            }
        }
    }

    /**
     * Register inbuilt client modules
     */
    @Suppress("LongMethod")
    fun registerInbuilt() {
        val builtin = arrayOf(
            // Combat
            ModuleAimbot,
            ModuleAutoArmor,
            ModuleAutoBow,
            ModuleAutoClicker,
            AutoMobHeal,
            ModuleAutoLeave,
            ModuleAutoBuff,
            ModuleAutoRod,
            ModuleAutoWeapon,
            ModuleFakeLag,
            ModuleCriticals,
            ModuleHitbox,
            ModuleKillAura,
            ModuleTpAura,
            ModuleSuperKnockback,
            ModuleTimerRange,
            ModuleTickBase,
            ModuleVelocity,
            ModuleBacktrack,
            ModuleSwordBlock,
            ModuleAutoShoot,
            ModuleKeepSprint,
            ModuleMaceKill,
            ModuleNoMissCooldown,

            // Exploit
            ModuleAbortBreaking,
            ModuleAntiReducedDebugInfo,
            ModuleAntiHunger,
            ModuleClip,
            ModuleExtendedFirework,
            ModuleResetVL,
            ModuleDamage,
            ModuleDisabler,
            ModuleGhostHand,
            ModuleKick,
            ModuleMoreCarry,
            ModuleMultiActions,
            ModuleNewChunks,
            ModuleNameCollector,
            ModuleNoPitchLimit,
            ModulePingSpoof,
            ModulePlugins,
            ModulePortalMenu,
            ModuleSleepWalker,
            ModuleVehicleOneHit,
            ModuleServerCrasher,
            ModuleDupe,
            ModuleClickTp,
            ModuleTimeShift,
            ModuleTeleport,
            ModulePhase,
            ModuleYggdrasilSignatureFix,

            // Fun
            ModuleDankBobbing,
            ModuleDerp,
            ModuleNotebot,
            ModuleSkinDerp,
            ModuleHandDerp,
            ModuleTwerk,
            ModuleVomit,

            // Misc
            ModuleAutoConfig,
            ModuleGUICloser,
            ModuleBookBot,
            ModuleAntiBot,
            ModuleBetterTab,
            ModuleItemScroller,
            ModuleBetterChat,
            ModuleElytraTarget,
            ModuleMacros,
            ModuleMiddleClickAction,
            ModuleInventoryTracker,
            ModuleNameProtect,
            ModuleTextFieldProtect,
            ModuleNotifier,
            ModuleSpammer,
            ModuleAutoAccount,
            ModuleTeams,
            ModuleElytraSwap,
            ModuleAutoChatGame,
            ModuleReportHelper,
            ModuleTargetLock,
            ModuleAutoPearl,
            ModuleAntiStaff,
            ModuleFlagCheck,
            ModulePacketLogger,
            ModuleDebugRecorder,
            ModuleAntiCheatDetect,
            ModuleEasyPearl,

            // Movement
            ModuleAirJump,
            ModuleAntiBounce,
            ModuleAntiLevitation,
            ModuleAutoDodge,
            ModuleAvoidHazards,
            ModuleBlockBounce,
            ModuleBlockWalk,
            ModuleElytraRecast,
            ModuleElytraFly,
            ModuleFly,
            ModuleFreeze,
            ModuleHighJump,
            ModuleInventoryMove,
            ModuleLiquidWalk,
            ModuleLongJump,
            ModuleNoClip,
            ModuleNoJumpDelay,
            ModuleNoPose,
            ModuleNoPush,
            ModuleNoSlow,
            ModuleNoWeb,
            ModuleParkour,
            ModuleEntityControl,
            ModuleSafeWalk,
            ModuleSneak,
            ModuleSpeed,
            ModuleSprint,
            ModuleStep,
            ModuleReverseStep,
            ModuleStrafe,
            ModuleTerrainSpeed,
            ModuleVehicleBoost,
            ModuleVehicleControl,
            ModuleSpider,
            ModuleTargetStrafe,
            ModuleAnchor,
            ModuleSnapTap,

            // Player
            ModuleAntiVoid,
            ModuleAntiAFK,
            ModuleAntiExploit,
            ModuleAutoBreak,
            ModuleAutoCrafter,
            ModuleAutoFish,
            ModuleAutoRespawn,
            ModuleAutoWindCharge,
            ModuleOffhand,
            ModuleAutoShop,
            ModuleAutoWalk,
            ModuleBlink,
            ModuleChestCleaner,
            ModuleChestStealer,
            ModuleEagle,
            ModuleFastExp,
            ModuleFastUse,
            ModuleInventoryCleaner,
            ModuleNoBlockInteract,
            ModuleNoEntityInteract,
            ModuleNoFall,
            ModuleNoRotateSet,
            ModuleNoSlotSet,
            ModuleReach,
            ModuleAutoQueue,
            ModuleSmartEat,
            ModuleReplenish,
            ModulePotionSpoof,

            // Render
            ModuleAnimations,
            ModuleAntiBlind,
            ModuleBetterInventory,
            ModuleBlockESP,
            ModuleBlockOutline,
            ModuleBreadcrumbs,
            ModuleCameraClip,
            ModuleClickGui,
            ModuleDamageParticles,
            ModuleParticles,
            ModuleESP,
            ModuleLogoffSpot,
            ModuleFreeCam,
            ModuleSmoothCamera,
            ModuleFreeLook,
            ModuleFullBright,
            ModuleHoleESP,
            ModuleHud,
            ModuleHats,
            ModuleItemESP,
            ModuleItemTags,
            ModuleJumpEffect,
            ModuleMobOwners,
            ModuleMurderMystery,
            ModuleHitFX,
            ModuleNametags,
            ModuleCombineMobs,
            ModuleAspect,
            ModuleAutoF5,
            ModuleChams,
            ModuleBedPlates,
            ModuleNoBob,
            ModuleNoFov,
            ModuleNoHurtCam,
            ModuleNoSwing,
            ModuleCustomAmbience,
            ModuleProphuntESP,
            ModuleQuickPerspectiveSwap,
            ModuleRadar,
            ModuleRotations,
            ModuleSilentHotbar,
            ModuleStorageESP,
            ModuleTNTTimer,
            ModuleTracers,
            ModuleTrajectories,
            ModuleTrueSight,
            ModuleVoidESP,
            ModuleXRay,
            ModuleDebug,
            ModuleZoom,
            ModuleItemChams,
            ModuleCrystalView,
            ModuleSkinChanger,
            ModuleProtectionZones,
            ModuleCrosshair,

            // World
            ModuleAirPlace,
            ModuleAutoBuild,
            ModuleAutoDisable,
            ModuleAutoFarm,
            ModuleAutoTool,
            ModuleCrystalAura,
            ModuleFastBreak,
            ModuleFastPlace,
            ModuleFucker,
            ModuleAutoTrap,
            ModuleBlockTrap,
            ModuleNoSlowBreak,
            ModuleLiquidFiller,
            ModuleLiquidPlace,
            ModuleProjectilePuncher,
            ModuleScaffold,
            ModuleTimer,
            ModuleNuker,
            ModuleExtinguish,
            ModuleBedDefender,
            ModuleBlockIn,
            ModuleSurround,
            ModulePacketMine,
            ModuleHoleFiller,
            ModuleStrongholdFinder,
            ModuleNoInterpolation,
        )

        builtin.forEach { module ->
            addModule(module)
            module.walkKeyPath()
            module.verifyFallbackDescription()
        }
    }

    fun addModule(module: ClientModule) {
        if (!modules.add(module)) {
            error("Module '${module.name}' is already registered.")
        }
        module.walkInit()
        module.onRegistration()
    }

    fun removeModule(module: ClientModule) {
        if (!modules.remove(module)) {
            error("Module '${module.name}' is not registered.")
        }
        if (module.enabled) {
            module.enabled = false
        }
        module.unregister()
    }

    fun clear() {
        modules.clear()
    }

    /**
     * This is being used by UltralightJS for the implementation of the ClickGUI. DO NOT REMOVE!
     */
    @JvmName("getCategories")
    @ScriptApiRequired
    fun getCategories() = ModuleCategories.entries.mapToArray { it.tag }

    @JvmName("getModules")
    @ScriptApiRequired
    fun getModules(): Collection<ClientModule> = modules

    @JvmName("getModuleByName")
    @ScriptApiRequired
    fun getModuleByName(module: String) = find { it.name.equals(module, true) }

    operator fun get(moduleName: String) = modules.find { it.name.equals(moduleName, true) }

}
