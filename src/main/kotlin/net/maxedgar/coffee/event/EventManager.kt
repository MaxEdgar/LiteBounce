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
package net.maxedgar.coffee.event

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.maxedgar.coffee.event.events.AccountManagerAdditionResultEvent
import net.maxedgar.coffee.event.events.AccountManagerLoginResultEvent
import net.maxedgar.coffee.event.events.AccountManagerMessageEvent
import net.maxedgar.coffee.event.events.AccountManagerRemovalResultEvent
import net.maxedgar.coffee.event.events.AllowAutoJumpEvent
import net.maxedgar.coffee.event.events.AttackEntityEvent
import net.maxedgar.coffee.event.events.BedStateChangeEvent
import net.maxedgar.coffee.event.events.BlinkPacketEvent
import net.maxedgar.coffee.event.events.BlockAttackEvent
import net.maxedgar.coffee.event.events.BlockBreakingProgressEvent
import net.maxedgar.coffee.event.events.BlockChangeEvent
import net.maxedgar.coffee.event.events.BlockCountChangeEvent
import net.maxedgar.coffee.event.events.BlockShapeEvent
import net.maxedgar.coffee.event.events.BlockSlipperinessMultiplierEvent
import net.maxedgar.coffee.event.events.BlockVelocityMultiplierEvent

import net.maxedgar.coffee.event.events.CancelBlockBreakingEvent
import net.maxedgar.coffee.event.events.ChatReceiveEvent
import net.maxedgar.coffee.event.events.ChatSendEvent
import net.maxedgar.coffee.event.events.ChunkDeltaUpdateEvent
import net.maxedgar.coffee.event.events.ChunkLoadEvent
import net.maxedgar.coffee.event.events.ChunkUnloadEvent
import net.maxedgar.coffee.event.events.ClickGuiScaleChangeEvent
import net.maxedgar.coffee.event.events.ClickGuiValueChangeEvent
import net.maxedgar.coffee.event.events.ClientChatErrorEvent
import net.maxedgar.coffee.event.events.ClientChatJwtTokenEvent
import net.maxedgar.coffee.event.events.ClientChatMessageEvent
import net.maxedgar.coffee.event.events.ClientChatStateChange
import net.maxedgar.coffee.event.events.ClientLanguageChangedEvent
import net.maxedgar.coffee.event.events.ClientPlayerEffectEvent
import net.maxedgar.coffee.event.events.ClientShutdownEvent
import net.maxedgar.coffee.event.events.ClientStartEvent
import net.maxedgar.coffee.event.events.DeathEvent
import net.maxedgar.coffee.event.events.DisconnectEvent
import net.maxedgar.coffee.event.events.DrawOutlinesEvent
import net.maxedgar.coffee.event.events.EntityEquipmentChangeEvent
import net.maxedgar.coffee.event.events.EntityHealthUpdateEvent
import net.maxedgar.coffee.event.events.EntityMarginEvent
import net.maxedgar.coffee.event.events.FluidPushEvent
import net.maxedgar.coffee.event.events.FpsChangeEvent
import net.maxedgar.coffee.event.events.FpsLimitEvent
import net.maxedgar.coffee.event.events.FramebufferResizeEvent
import net.maxedgar.coffee.event.events.GameModeChangeEvent
import net.maxedgar.coffee.event.events.GameRenderEvent
import net.maxedgar.coffee.event.events.GameRenderTaskQueueEvent
import net.maxedgar.coffee.event.events.GameTickEvent
import net.maxedgar.coffee.event.events.HealthUpdateEvent
import net.maxedgar.coffee.event.events.InputHandleEvent
import net.maxedgar.coffee.event.events.ItemLoreQueryEvent
import net.maxedgar.coffee.event.events.KeyEvent
import net.maxedgar.coffee.event.events.KeybindChangeEvent
import net.maxedgar.coffee.event.events.KeybindIsPressedEvent
import net.maxedgar.coffee.event.events.KeyboardCharEvent
import net.maxedgar.coffee.event.events.KeyboardKeyEvent
import net.maxedgar.coffee.event.events.ModuleActivationEvent
import net.maxedgar.coffee.event.events.ModuleToggleEvent
import net.maxedgar.coffee.event.events.MouseButtonEvent
import net.maxedgar.coffee.event.events.MouseCursorEvent
import net.maxedgar.coffee.event.events.MouseRotationEvent
import net.maxedgar.coffee.event.events.MouseScrollEvent
import net.maxedgar.coffee.event.events.MouseScrollInHotbarEvent
import net.maxedgar.coffee.event.events.MovementInputEvent
import net.maxedgar.coffee.event.events.NotificationEvent
import net.maxedgar.coffee.event.events.OverlayMessageEvent
import net.maxedgar.coffee.event.events.OverlayRenderEvent
import net.maxedgar.coffee.event.events.PacketEvent
import net.maxedgar.coffee.event.events.PerspectiveEvent
import net.maxedgar.coffee.event.events.PipelineEvent
import net.maxedgar.coffee.event.events.PlayerAfterJumpEvent
import net.maxedgar.coffee.event.events.PlayerFluidCollisionCheckEvent
import net.maxedgar.coffee.event.events.PlayerInteractItemEvent
import net.maxedgar.coffee.event.events.PlayerInteractedItemEvent
import net.maxedgar.coffee.event.events.PlayerJumpEvent
import net.maxedgar.coffee.event.events.PlayerMoveEvent
import net.maxedgar.coffee.event.events.PlayerMovementTickEvent
import net.maxedgar.coffee.event.events.PlayerNetworkMovementTickEvent
import net.maxedgar.coffee.event.events.PlayerPostTickEvent
import net.maxedgar.coffee.event.events.PlayerPushOutEvent
import net.maxedgar.coffee.event.events.PlayerSafeWalkEvent
import net.maxedgar.coffee.event.events.PlayerSneakMultiplier
import net.maxedgar.coffee.event.events.PlayerStepEvent
import net.maxedgar.coffee.event.events.PlayerStepSuccessEvent
import net.maxedgar.coffee.event.events.PlayerStrideEvent
import net.maxedgar.coffee.event.events.PlayerTickEvent
import net.maxedgar.coffee.event.events.PlayerUseMultiplier
import net.maxedgar.coffee.event.events.PlayerVelocityStrafe
import net.maxedgar.coffee.event.events.ProxyCheckResultEvent
import net.maxedgar.coffee.event.events.RefreshArrayListEvent
import net.maxedgar.coffee.event.events.ResourceReloadEvent
import net.maxedgar.coffee.event.events.RotationUpdateEvent
import net.maxedgar.coffee.event.events.ScaleFactorChangeEvent
import net.maxedgar.coffee.event.events.ScheduleInventoryActionEvent
import net.maxedgar.coffee.event.events.ScreenEvent
import net.maxedgar.coffee.event.events.ScreenRenderEvent
import net.maxedgar.coffee.event.events.SelectHotbarSlotSilentlyEvent
import net.maxedgar.coffee.event.events.ServerConnectEvent
import net.maxedgar.coffee.event.events.ServerPingedEvent
import net.maxedgar.coffee.event.events.SessionEvent
import net.maxedgar.coffee.event.events.SpaceSeperatedNamesChangeEvent
import net.maxedgar.coffee.event.events.SprintEvent
import net.maxedgar.coffee.event.events.TagEntityEvent
import net.maxedgar.coffee.event.events.TargetChangeEvent
import net.maxedgar.coffee.event.events.ThemeColorChangeEvent
import net.maxedgar.coffee.event.events.TickPacketProcessEvent
import net.maxedgar.coffee.event.events.TitleEvent
import net.maxedgar.coffee.event.events.UseCooldownEvent
import net.maxedgar.coffee.event.events.UserLoggedInEvent
import net.maxedgar.coffee.event.events.UserLoggedOutEvent
import net.maxedgar.coffee.event.events.ValueChangedEvent
import net.maxedgar.coffee.event.events.VirtualScreenEvent
import net.maxedgar.coffee.event.events.WindowResizeEvent
import net.maxedgar.coffee.event.events.WorldChangeEvent
import net.maxedgar.coffee.event.events.WorldEntityRemoveEvent
import net.maxedgar.coffee.event.events.WorldRenderEvent
import net.maxedgar.coffee.features.misc.HideAppearance.isDestructed
import net.maxedgar.coffee.utils.client.error.ErrorHandler
import net.maxedgar.coffee.utils.client.logger
import net.minecraft.ReportedException

/**
 * Contains all classes of events. Used to create lookup tables ahead of time
 */
@JvmField
internal val ALL_EVENT_CLASSES: Array<Class<out Event>> = arrayOf(
    GameTickEvent::class.java,
    GameRenderTaskQueueEvent::class.java,
    TickPacketProcessEvent::class.java,
    BlockChangeEvent::class.java,
    ChunkLoadEvent::class.java,
    ChunkDeltaUpdateEvent::class.java,
    ChunkUnloadEvent::class.java,
    DisconnectEvent::class.java,
    GameRenderEvent::class.java,
    WorldRenderEvent::class.java,
    OverlayRenderEvent::class.java,
    ScreenRenderEvent::class.java,
    WindowResizeEvent::class.java,
    FramebufferResizeEvent::class.java,
    MouseButtonEvent::class.java,
    MouseScrollEvent::class.java,
    MouseCursorEvent::class.java,
    KeyboardKeyEvent::class.java,
    KeyboardCharEvent::class.java,
    InputHandleEvent::class.java,
    MovementInputEvent::class.java,
    SprintEvent::class.java,
    KeyEvent::class.java,
    MouseRotationEvent::class.java,
    KeybindChangeEvent::class.java,
    KeybindIsPressedEvent::class.java,
    AttackEntityEvent::class.java,
    SessionEvent::class.java,
    ScreenEvent::class.java,
    ChatSendEvent::class.java,
    ChatReceiveEvent::class.java,
    UseCooldownEvent::class.java,
    BlockShapeEvent::class.java,
    BlockBreakingProgressEvent::class.java,
    BlockVelocityMultiplierEvent::class.java,
    BlockSlipperinessMultiplierEvent::class.java,
    EntityMarginEvent::class.java,
    EntityHealthUpdateEvent::class.java,
    HealthUpdateEvent::class.java,
    DeathEvent::class.java,
    PlayerTickEvent::class.java,
    PlayerPostTickEvent::class.java,
    PlayerMovementTickEvent::class.java,
    PlayerNetworkMovementTickEvent::class.java,
    PlayerPushOutEvent::class.java,
    PlayerMoveEvent::class.java,
    PlayerJumpEvent::class.java,
    PlayerAfterJumpEvent::class.java,
    PlayerUseMultiplier::class.java,
    PlayerInteractItemEvent::class.java,
    PlayerInteractedItemEvent::class.java,
    PlayerVelocityStrafe::class.java,
    PlayerStrideEvent::class.java,
    PlayerSafeWalkEvent::class.java,
    CancelBlockBreakingEvent::class.java,
    PlayerStepEvent::class.java,
    PlayerStepSuccessEvent::class.java,
    FluidPushEvent::class.java,
    PipelineEvent::class.java,
    PacketEvent::class.java,
    ClientStartEvent::class.java,
    ClientShutdownEvent::class.java,
    ClientLanguageChangedEvent::class.java,
    ValueChangedEvent::class.java,
    ModuleActivationEvent::class.java,
    ModuleToggleEvent::class.java,
    NotificationEvent::class.java,
    ClientChatStateChange::class.java,
    ClientChatMessageEvent::class.java,
    ClientChatErrorEvent::class.java,
    ClientChatJwtTokenEvent::class.java,
    WorldChangeEvent::class.java,
    AccountManagerMessageEvent::class.java,
    AccountManagerAdditionResultEvent::class.java,
    AccountManagerRemovalResultEvent::class.java,
    AccountManagerLoginResultEvent::class.java,
    VirtualScreenEvent::class.java,
    FpsChangeEvent::class.java,
    FpsLimitEvent::class.java,
    ClientPlayerEffectEvent::class.java,
    RotationUpdateEvent::class.java,
    RefreshArrayListEvent::class.java,
    ServerConnectEvent::class.java,
    ServerPingedEvent::class.java,
    TargetChangeEvent::class.java,
    BlockCountChangeEvent::class.java,
    BedStateChangeEvent::class.java,
    GameModeChangeEvent::class.java,
    ResourceReloadEvent::class.java,
    ProxyCheckResultEvent::class.java,
    ScaleFactorChangeEvent::class.java,
    DrawOutlinesEvent::class.java,
    OverlayMessageEvent::class.java,
    ScheduleInventoryActionEvent::class.java,
    SelectHotbarSlotSilentlyEvent::class.java,
    SpaceSeperatedNamesChangeEvent::class.java,
    ClickGuiScaleChangeEvent::class.java,
    ThemeColorChangeEvent::class.java,
    TagEntityEvent::class.java,
    MouseScrollInHotbarEvent::class.java,
    PlayerFluidCollisionCheckEvent::class.java,
    PlayerSneakMultiplier::class.java,
    PerspectiveEvent::class.java,
    ItemLoreQueryEvent::class.java,
    EntityEquipmentChangeEvent::class.java,
    ClickGuiValueChangeEvent::class.java,
    BlockAttackEvent::class.java,
    BlinkPacketEvent::class.java,
    AllowAutoJumpEvent::class.java,
    WorldEntityRemoveEvent::class.java,
    TitleEvent.Title::class.java,
    TitleEvent.Subtitle::class.java,
    TitleEvent.Fade::class.java,
    TitleEvent.Clear::class.java,
    UserLoggedInEvent::class.java,
    UserLoggedOutEvent::class.java,
)

inline fun <reified E : Event> eventFlow(): SharedFlow<E> =
    EventManager.eventFlow(E::class.java)

/**
 * A modern and fast event handler using lambda handlers
 */
object EventManager {

    private val registry: Map<Class<out Event>, EventHookRegistry<in Event>> =
        ALL_EVENT_CLASSES.associateWithTo(
            Reference2ObjectOpenHashMap(ALL_EVENT_CLASSES.size)
        ) { EventHookRegistry() }

    private val flows: Map<Class<out Event>, MutableSharedFlow<Event>> =
        ALL_EVENT_CLASSES.associateWithTo(
            Reference2ObjectOpenHashMap(ALL_EVENT_CLASSES.size)
        ) { MutableSharedFlow(replay = 0, extraBufferCapacity = 0) }

    /**
     * Used by handler methods
     */
    fun <T : Event> registerEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>): EventHook<T> {
        val handlers = registry[eventClass]
            ?: error("The event '${eventClass.name}' is not registered in Events.kt::ALL_EVENT_CLASSES.")

        @Suppress("UNCHECKED_CAST")
        val hook = eventHook as EventHook<in Event>

        handlers.addIfAbsent(hook)

        return eventHook
    }

    /**
     * Unregisters a handler.
     */
    fun <T : Event> unregisterEventHook(eventClass: Class<out Event>, eventHook: EventHook<T>) {
        @Suppress("UNCHECKED_CAST")
        registry[eventClass]?.remove(eventHook as EventHook<in Event>)
    }

    fun unregisterEventHandler(eventListener: EventListener) {
        registry.values.forEach {
            it.remove(eventListener)
        }
    }

    fun unregisterAll() {
        registry.values.forEach {
            it.clear()
        }
    }

    /**
     * Call event to listeners
     *
     * @param event to call
     */
    fun <T : Event> callEvent(event: T): T {
        if (isDestructed) {
            return event
        }

        val eventType = event.javaClass
        val target = registry[eventType] ?: return event

        event.isCompleted = false
        for (eventHook in target.snapshot) {
            @Suppress("UNCHECKED_CAST")
            eventHook as EventHook<T>
            if (!eventHook.handlerClass.running) {
                continue
            }

            try {
                eventHook.handler.accept(event)
            } catch (e: ReportedException) {
                ErrorHandler.fatal(
                    error = e,
                    needToReport = true,
                    additionalMessage = "Event (${eventType.simpleName}) handler of ${eventHook.handlerClass}"
                )
            } catch (e: Throwable) {
                logger.error(
                    "Exception while executing event handler of {}, event={}",
                    eventHook.handlerClass.javaClass.simpleName,
                    event,
                    e,
                )
            }
        }
        event.isCompleted = true

        @Suppress("UNCHECKED_CAST")
        (flows[event.javaClass] as MutableSharedFlow<T>).tryEmit(event)

        return event
    }

    /**
     * Gets a [SharedFlow] for the given event class.
     * The flow receives the event instances after all [EventHook]s are executed.
     * So the [Event.isCompleted] will be true when the event is emitted.
     */
    fun <T : Event> eventFlow(eventClass: Class<T>): SharedFlow<T> {
        @Suppress("UNCHECKED_CAST")
        return flows[eventClass] as SharedFlow<T>
    }
}
