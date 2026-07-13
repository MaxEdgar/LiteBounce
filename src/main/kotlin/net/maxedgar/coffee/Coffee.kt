/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee

import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.maxedgar.coffee.Coffee.CLIENT_NAME
import net.maxedgar.coffee.api.core.ApiConfig
import net.maxedgar.coffee.api.core.ioScope
import net.maxedgar.coffee.api.models.auth.ClientAccount
import net.maxedgar.coffee.api.services.client.ClientUpdate
import net.maxedgar.coffee.api.thirdparty.IpInfoApi
import net.maxedgar.coffee.config.ConfigSystem
import net.maxedgar.coffee.config.autoconfig.AutoConfig
import net.maxedgar.coffee.config.types.Config
import net.maxedgar.coffee.event.EventListener
import net.maxedgar.coffee.event.EventManager
import net.maxedgar.coffee.event.events.ClientShutdownEvent
import net.maxedgar.coffee.event.events.ClientStartEvent
import net.maxedgar.coffee.event.handler
import net.maxedgar.coffee.features.account.AccountManager
import net.maxedgar.coffee.features.blink.BlinkManager
import net.maxedgar.coffee.features.command.CommandManager
import net.maxedgar.coffee.features.cosmetic.ClientAccountManager
import net.maxedgar.coffee.features.cosmetic.CosmeticService
import net.maxedgar.coffee.features.creativetab.tabs.HeadsCreativeModeTab
import net.maxedgar.coffee.features.global.GlobalManager
import net.maxedgar.coffee.features.misc.FriendManager
import net.maxedgar.coffee.features.misc.proxy.ProxyManager
import net.maxedgar.coffee.features.module.ModuleManager
import net.maxedgar.coffee.features.spoofer.SpooferManager
import net.maxedgar.coffee.lang.LanguageManager
import net.maxedgar.coffee.render.FontManager
import net.maxedgar.coffee.render.HAS_AMD_VEGA_APU
import net.maxedgar.coffee.render.gui.ItemImageAtlas
import net.maxedgar.coffee.script.ScriptManager
import net.maxedgar.coffee.utils.aiming.PostRotationExecutor
import net.maxedgar.coffee.utils.aiming.RotationManager
import net.maxedgar.coffee.utils.block.ChunkScanner
import net.maxedgar.coffee.utils.client.GitInfo
import net.maxedgar.coffee.utils.client.InteractionTracker
import net.maxedgar.coffee.utils.client.ServerObserver
import net.maxedgar.coffee.utils.client.clientIdentifier
import net.maxedgar.coffee.utils.client.error.ErrorHandler
import net.maxedgar.coffee.utils.client.mc
import net.maxedgar.coffee.utils.combat.CombatManager
import net.maxedgar.coffee.utils.entity.RenderedEntities
import net.maxedgar.coffee.utils.input.InputTracker
import net.maxedgar.coffee.utils.inventory.EnderChestInventoryTracker
import net.maxedgar.coffee.utils.inventory.InventoryManager
import net.maxedgar.coffee.utils.kotlin.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ReloadableResourceManager
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * Coffee
 *
 * A free mixin-based injection hacked-client for Minecraft using FabricMC.
 * Native Wurst7-inspired ClickGUI and HUD.
 *
 * @author kawaiinekololis (@team CCBlueX)
 */
object Coffee : EventListener {

    /**
     * CLIENT INFORMATION
     *
     * WARNING: Please read the GNU General Public License
     */
    const val CLIENT_NAME = "Coffee V2"
    const val CLIENT_AUTHOR = "MaxEdgar"

    private object Client : Config("Client") {
        val version = text("Version", GitInfo.version())
            .immutable()
        val commit = text("Commit", GitInfo.get("git.commit.id.abbrev")?.let { "git-$it" } ?: "unknown")
            .immutable()
        val branch = text("Branch", GitInfo.branch())
            .immutable()

        init {
            ConfigSystem.root(this)

            version.onChange { previousVersion ->
                runCatching {
                    ConfigSystem.backup("automatic_${previousVersion}-${version.inner}")
                }.onFailure {
                    logger.error("Unable to create backup", it)
                }

                previousVersion
            }
        }
    }

    val clientVersion by Client.version
    val clientCommit by Client.commit
    val clientBranch by Client.branch

    /**
     * Defines if the client is in development mode.
     */
    const val IN_DEVELOPMENT = true

    /**
     * Client logger to print out console messages
     */
    val logger get() = net.maxedgar.coffee.utils.client.logger

    var isInitialized = false
        private set

    /**
     * Creates an [net.minecraft.resources.Identifier] starts with [CLIENT_NAME].
     */
    @JvmStatic
    fun identifier(path: String): Identifier = clientIdentifier(path)

    /**
     * Gets client resource.
     *
     * @param path prefix `/resources/coffee/`
     * @throws IllegalArgumentException if the resource is not found
     */
    @JvmStatic
    fun resource(path: String): InputStream =
        Coffee::class.java.getResourceAsStream("/resources/coffee/$path")
            ?: throw IllegalArgumentException("Resource $path not found")

    /**
     * Gets client resource as string.
     */
    @JvmStatic
    fun resourceToString(path: String): String =
        resource(path).use { it.bufferedReader().readText() }

    /**
     * Initializes the client, called when
     * we reached the last stage of the splash screen.
     *
     * The thread should be the main render thread.
     */
    private fun initializeClient(
        workerDispatcher: CoroutineDispatcher,
        renderThreadDispatcher: CoroutineDispatcher,
    ): CompletableFuture<Unit> = CoroutineScope(
        renderThreadDispatcher + CoroutineName("$CLIENT_NAME Initializer")
    ).future {
        if (isInitialized) {
            return@future
        }

        // Ensure we are on the render thread
        RenderSystem.assertOnRenderThread()

        // Initialize managers and features
        Client
        initializeManagers(workerDispatcher, renderThreadDispatcher)
        initializeFeatures()
        initializeResources(workerDispatcher)
        initializeFonts()

        // Register shutdown hook in case [ClientShutdownEvent] is not called
        Runtime.getRuntime().addShutdownHook(Thread(::shutdownClient))

        // Check for AMD Vega iGPU
        if (HAS_AMD_VEGA_APU) {
            logger.info("AMD Vega iGPU detected, enabling different line smooth handling. " +
                "If you believe this is a mistake, please create an issue at " +
                "https://github.com/MaxEdgar/CoffeeV2/issues.")
        }

        // Do backup before loading configs
        if (!ConfigSystem.isFirstLaunch && !Client.jsonFile.exists()) {
            runCatching {
                ConfigSystem.backup("automatic_${Client.version.inner}")
            }.onFailure {
                logger.error("Unable to create backup", it)
            }
        }

        // Load all configurations
        ConfigSystem.loadAll()

        isInitialized = true
        logger.info("$CLIENT_NAME has been successfully initialized.")
    }.exceptionally { throwable ->
        ErrorHandler.fatal(throwable, additionalMessage = "$CLIENT_NAME initializer")
    }

    /**
     * Initializes managers for Event Listener registration.
     */
    private suspend fun initializeManagers(
        workerDispatcher: CoroutineDispatcher,
        renderThreadDispatcher: CoroutineDispatcher,
    ) = withContext(renderThreadDispatcher) {
        // Script system
        val scriptEngineJob = launch(workerDispatcher) {
            runCatching(ScriptManager::initializeEngine).onFailure { error ->
                logger.error("[ScriptAPI] Failed to initialize script engine.", error)
            }
        }

        // Config
        ConfigSystem

        // Utility
        RenderedEntities
        ChunkScanner
        InputTracker

        // Feature managers
        ModuleManager
        CommandManager
        ProxyManager
        AccountManager

        // Utility managers
        RotationManager
        BlinkManager
        InteractionTracker
        CombatManager
        FriendManager
        InventoryManager
        EnderChestInventoryTracker
        ConfigSystem.root(ClientAccountManager)
        ConfigSystem.root(SpooferManager)
        ConfigSystem.root(GlobalManager)
        PostRotationExecutor
        ServerObserver
        ItemImageAtlas

        scriptEngineJob.join()
    }

    /**
     * Initializes in-built and script features.
     */
    private fun initializeFeatures() {
        // Register commands and modules
        CommandManager.registerInbuilt()
        ModuleManager.registerInbuilt()

        // Load user scripts
        runCatching(ScriptManager::loadAll).onFailure { error ->
            logger.error("ScriptManager was unable to load scripts.", error)
        }
    }

    /**
     * Simultaneously initializes resources
     * such as translations, cosmetics, player heads, configs and so on,
     * which do not rely on the main thread.
     */
    private suspend fun initializeResources(
        dispatcher: CoroutineDispatcher,
    ) = withContext(dispatcher) {
        logger.info("Initializing API...")
        // Lookup API config
        ApiConfig.config

        supervisorScope {
            launch {
                // Load translations
                LanguageManager.loadDefault()
            }
            launch {
                val update = withTimeoutOrNull(8.seconds) { ClientUpdate.update.await() } ?: return@launch
                logger.info("[Update] Update available: $clientVersion -> ${update.lbVersion}")
            }
            launch {
                // Load cosmetics
                CosmeticService.refreshCarriers(force = true) {
                    logger.info("Successfully loaded ${CosmeticService.carriers.size} cosmetics carriers.")
                }
            }
            launch {
                // Download player heads
                HeadsCreativeModeTab.heads.getFinalState()
            }
            launch {
                // Load configs
                AutoConfig.reloadConfigs()
            }
            launch {
                IpInfoApi.original
            }
            launch {
                ConfigSystem.load(ClientAccountManager)
                if (ClientAccount.ENV_ACCOUNT != null) {
                    ClientAccountManager.clientAccount = ClientAccount.ENV_ACCOUNT
                }

                if (ClientAccountManager.clientAccount != ClientAccount.EMPTY_ACCOUNT) {
                    runCatching {
                        ClientAccountManager.clientAccount.renew()
                    }.onFailure {
                        logger.error("Failed to renew client account token.", it)
                        ClientAccountManager.clientAccount = ClientAccount.EMPTY_ACCOUNT
                    }.onSuccess {
                        logger.info("Successfully renewed client account token.")
                    }

                    ConfigSystem.store(ClientAccountManager)
                }
            }
        }

        logger.info("API initialization done.")
    }

    /**
     * Initializes the font system.
     * Browser/GUI theme initialization has been removed as part of the native UI migration.
     */
    private suspend fun initializeFonts() = withContext(Dispatchers.Minecraft) {
        RenderSystem.assertOnRenderThread()

        // Prepare glyph manager
        val duration = measureTime {
            FontManager.createGlyphManager()
        }
        logger.info("Completed loading fonts in ${duration.inWholeMilliseconds} ms.")
        logger.info("Fonts: [ ${FontManager.fontFaces.keys.joinToString()} ]")
    }

    /**
     * Shuts down the client. This will save all configurations and stop all running tasks.
     */
    private fun shutdownClient() {
        if (!isInitialized) {
            return
        }
        isInitialized = false
        logger.info("Shutting down client...")

        // Unregister all event listener and stop all running tasks
        ChunkScanner.stopThread()
        EventManager.unregisterAll()

        // Save all configurations
        ConfigSystem.storeAll()
    }

    /**
     * Should be executed to start the client.
     */
    @Suppress("unused")
    private val startHandler = handler<ClientStartEvent> {
        runCatching {
            logger.info("Launching $CLIENT_NAME v$clientVersion by $CLIENT_AUTHOR")
            // Print client information
            logger.info("Client Version: $clientVersion ($clientCommit)")
            logger.info("Client Branch: $clientBranch")
            logger.info("Operating System: ${System.getProperty("os.name")} (${System.getProperty("os.version")})")
            logger.info("Java Version: ${System.getProperty("java.version")}")
            logger.info("Screen Resolution: ${mc.window.screenWidth}x${mc.window.screenHeight}")
            logger.info("Refresh Rate: ${mc.window.refreshRate} Hz")

            // Initialize event manager
            EventManager

            // Register resource reloader
            val resourceManager = mc.resourceManager
            if (resourceManager is ReloadableResourceManager) {
                resourceManager.registerReloadListener(ClientResourceReloader)
            } else {
                logger.warn("Failed to register resource reloader!")

                // Run resource reloader directly as fallback
                initializeClient(
                    workerDispatcher = Dispatchers.Default,
                    renderThreadDispatcher = Dispatchers.Minecraft,
                )
            }
        }.onFailure {
            ErrorHandler.fatal(it, additionalMessage = "Client start")
        }
    }

    /**
     * Resource reloader which is executed on client start and reload.
     */
    private object ClientResourceReloader : PreparableReloadListener {
        override fun reload(
            store: PreparableReloadListener.SharedState,
            prepareExecutor: Executor,
            synchronizer: PreparableReloadListener.PreparationBarrier,
            applyExecutor: Executor
        ): CompletableFuture<Void> {
            return synchronizer.wait(net.minecraft.util.Unit.INSTANCE)
                .thenCompose {
                    val prepareDispatcher = prepareExecutor.asCoroutineDispatcher()
                    val applyDispatcher = applyExecutor.asCoroutineDispatcher()
                    @Suppress("UNCHECKED_CAST") // Kotlin Unit to Java Void
                    initializeClient(
                        workerDispatcher = prepareDispatcher,
                        renderThreadDispatcher = applyDispatcher,
                    ) as CompletableFuture<Void>
                }
        }

        override fun getName() = CLIENT_NAME
    }

    /**
     * Should be executed to stop the client.
     */
    @Suppress("unused")
    private val shutdownHandler = handler<ClientShutdownEvent> {
        shutdownClient()
    }

}
