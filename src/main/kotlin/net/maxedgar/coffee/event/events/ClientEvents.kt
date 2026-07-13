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

package net.maxedgar.coffee.event.events

import com.google.gson.annotations.SerializedName
import net.maxedgar.coffee.annotations.Tag
import net.maxedgar.coffee.config.types.Value
import net.maxedgar.coffee.config.types.group.ValueGroup
import net.maxedgar.coffee.config.types.list.Tagged
import net.maxedgar.coffee.event.CancellableEvent
import net.maxedgar.coffee.event.Event
import net.maxedgar.coffee.features.chat.packet.AxoUser
import net.maxedgar.coffee.features.misc.proxy.Proxy

import net.maxedgar.coffee.render.engine.type.Color4b
import net.maxedgar.coffee.utils.block.bed.BedState
import net.maxedgar.coffee.utils.inventory.InventoryAction
import net.maxedgar.coffee.utils.inventory.InventoryConstraints
import net.maxedgar.coffee.utils.kotlin.Priority
import net.maxedgar.coffee.utils.kotlin.unmodifiable
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Block

@Tag("themeColorChange")
class ThemeColorChangeEvent(val themeId: String, val name: String, val value: Color4b) : Event()

@Tag("clickGuiScaleChange")
class ClickGuiScaleChangeEvent(val value: Float) : Event()

@Tag("clickGuiValueChange")
class ClickGuiValueChangeEvent(val configurable: ValueGroup) : Event()

@Tag("spaceSeperatedNamesChange")
class SpaceSeperatedNamesChangeEvent(val value: Boolean) : Event()

@Tag("clientStart")
object ClientStartEvent : Event()

@Tag("clientShutdown")
object ClientShutdownEvent : Event()

@Tag("clientLanguageChanged")
class ClientLanguageChangedEvent : Event()

@Tag("valueChanged")
class ValueChangedEvent(val value: Value<*>) : Event()

@Tag("moduleActivation")
class ModuleActivationEvent(val moduleName: String) : Event()

@Tag("moduleToggle")
class ModuleToggleEvent(val moduleName: String, val hidden: Boolean, val enabled: Boolean) : Event()

@Tag("refreshArrayList")
object RefreshArrayListEvent : Event()

@Tag("notification")
class NotificationEvent(val title: String, val message: String, val severity: Severity) : Event() {
    enum class Severity {
        INFO, SUCCESS, ERROR, ENABLED, DISABLED
    }
}

@Tag("gameModeChange")
class GameModeChangeEvent(val gameMode: GameType) : Event()

@Tag("targetChange")
class TargetChangeEvent(val target: Any?) : Event()

@Tag("blockCountChange")
class BlockCountChangeEvent(val nextBlock: Block?, val count: Int?) : Event()

@Tag("bedStateChange")
class BedStateChangeEvent(val bedStates: Collection<BedState>) : Event()

@Tag("clientChatStateChange")
class ClientChatStateChange(val state: State) : Event() {
    enum class State {
        @SerializedName("connecting")
        CONNECTING,

        @SerializedName("connected")
        CONNECTED,

        @SerializedName("logon")
        LOGGING_IN,

        @SerializedName("loggedIn")
        LOGGED_IN,

        @SerializedName("disconnected")
        DISCONNECTED,

        @SerializedName("authenticationFailed")
        AUTHENTICATION_FAILED,
    }
}

@Tag("clientChatMessage")
class ClientChatMessageEvent(
    val user: AxoUser,
    val message: String,
    val chatGroup: ChatGroup,
) : Event() {
    enum class ChatGroup(override val tag: String) : Tagged {
        @SerializedName("public")
        PUBLIC_CHAT("PublicChat"),

        @SerializedName("private")
        PRIVATE_CHAT("PrivateChat"),
    }
}

@Tag("clientChatError")
class ClientChatErrorEvent(val error: String) : Event()

@Tag("clientChatJwtToken")
// Do not define as WebSocket event, because it contains sensitive data
class ClientChatJwtTokenEvent(val jwt: String) : Event()

@Tag("accountManagerMessage")
class AccountManagerMessageEvent(val message: String) : Event()

@Tag("accountManagerLogin")
class AccountManagerLoginResultEvent(val username: String? = null, val error: String? = null) : Event()

@Tag("accountManagerAddition")
class AccountManagerAdditionResultEvent(
    val username: String? = null, val error: String? = null
) : Event()

@Tag("accountManagerRemoval")
class AccountManagerRemovalResultEvent(val username: String?) : Event()

@Tag("proxyCheckResult")
class ProxyCheckResultEvent(val proxy: Proxy? = null, val error: String? = null) : Event()

@Tag("virtualScreen")
class VirtualScreenEvent(
    val type: String,
    val action: Action
) : Event() {
    enum class Action {
        @SerializedName("open")
        OPEN,
        @SerializedName("close")
        CLOSE
    }
}

@Tag("serverPinged")
class ServerPingedEvent(val server: ServerData) : Event()

@Tag("rotationUpdate")
object RotationUpdateEvent : Event()

@Tag("resourceReload")
object ResourceReloadEvent : Event()

@Tag("scaleFactorChange")
class ScaleFactorChangeEvent(val scaleFactor: Int) : Event()

@Tag("scheduleInventoryAction")
class ScheduleInventoryActionEvent(val schedule: MutableList<InventoryAction.Chain> = mutableListOf()) : Event() {

    fun schedule(
        constrains: InventoryConstraints,
        action: InventoryAction,
        priority: Priority = Priority.NORMAL
    ) {
        this.schedule.add(InventoryAction.Chain(constrains, listOf(action), priority))
    }

    fun schedule(
        constrains: InventoryConstraints,
        vararg actions: InventoryAction,
        priority: Priority = Priority.NORMAL
    ) {
        this.schedule.add(InventoryAction.Chain(constrains, actions.unmodifiable(), priority))
    }

    fun schedule(
        constrains: InventoryConstraints,
        actions: List<InventoryAction>,
        priority: Priority = Priority.NORMAL
    ) {
        this.schedule.add(InventoryAction.Chain(constrains, actions, priority))
    }
}

@Tag("selectHotbarSlotSilently")
class SelectHotbarSlotSilentlyEvent(val requester: Any?, val slot: Int): CancellableEvent()

@Tag("userLoggedIn")
object UserLoggedInEvent : Event()

@Tag("userLoggedOut")
object UserLoggedOutEvent : Event()

