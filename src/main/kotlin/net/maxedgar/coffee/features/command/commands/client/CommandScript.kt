/*
 * This file is part of Coffee (https://github.com/MaxEdgar/CoffeeV2)
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
package net.maxedgar.coffee.features.command.commands.client

import net.maxedgar.coffee.features.command.Command
import net.maxedgar.coffee.features.command.builder.CommandBuilder
import net.maxedgar.coffee.features.command.builder.ParameterBuilder
import net.maxedgar.coffee.features.command.builder.enumChoice
import net.maxedgar.coffee.script.DebugProtocol
import net.maxedgar.coffee.script.ScriptDebugOptions
import net.maxedgar.coffee.script.ScriptManager
import net.maxedgar.coffee.utils.client.chat
import net.maxedgar.coffee.utils.client.clickablePath
import net.maxedgar.coffee.utils.client.regular
import net.maxedgar.coffee.utils.client.variable
import net.minecraft.util.Util
import java.io.File

private fun ParameterBuilder<*>.autocompletedFromScriptNames() =
    autocompletedFrom { ScriptManager.root.listFiles()?.map { it.name } }

object CommandScript : Command.Factory {

    override fun createCommand(): Command {
        return CommandBuilder.begin("script")
            .hub()
            .subcommand(reloadSubcommand())
            .subcommand(loadSubcommand())
            .subcommand(unloadSubcommand())
            .subcommand(debugSubcommand())
            .subcommand(listSubcommand())
            .subcommand(browseSubcommand())
            .subcommand(editSubcommand())
            .build()
    }

    private fun editSubcommand() = CommandBuilder.begin("edit").parameter(
        ParameterBuilder.begin<String>("name")
            .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
            .required()
            .autocompletedFromScriptNames()
            .build()
    ).handler {
        val name = args[0] as String
        val scriptFile = ScriptManager.root.resolve(name)

        if (!scriptFile.exists()) {
            chat(regular(command.result("notFound", variable(name))))
            return@handler
        }

        Util.getPlatform().openFile(scriptFile)
        chat(regular(command.result("opened", variable(name))))
    }.build()

    private fun browseSubcommand() = CommandBuilder.begin("browse").handler {
        Util.getPlatform().openFile(ScriptManager.root)
        chat(regular(command.result("browse", clickablePath(ScriptManager.root))))
    }.build()

    private fun listSubcommand() = CommandBuilder.begin("list").handler {
        val scripts = ScriptManager.scripts
        val scriptNames = scripts.map { script -> "${script.scriptName} (${script.language})" }

        if (scriptNames.isEmpty()) {
            chat(regular(command.result("noScripts")))
            return@handler
        }

        chat(regular(command.result("scripts", variable(scriptNames.joinToString(", ")))))
    }.build()

    private fun debugSubcommand() = CommandBuilder.begin("debug")
        .parameter(
            ParameterBuilder.begin<String>("name")
                .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
                .required()
                .autocompletedFromScriptNames()
                .build()
        )
        .parameter(
            ParameterBuilder.enumChoice<DebugProtocol>("protocol")
                .optional()
                .build()
        )
        .parameter(
            ParameterBuilder.begin<Boolean>("suspendOnStart")
                .verifiedBy(ParameterBuilder.BOOLEAN_VALIDATOR)
                .optional()
                .build()
        )
        .parameter(
            ParameterBuilder.begin<Boolean>("inspectInternals")
                .verifiedBy(ParameterBuilder.BOOLEAN_VALIDATOR)
                .optional()
                .build()
        )
        .parameter(
            ParameterBuilder.begin<Int>("port")
                .verifiedBy(ParameterBuilder.intRange(1, 65535))
                .optional()
                .build()
        )
        .handler {
            val name = args[0] as String
            val scriptFile = ScriptManager.root.resolve(name)

            if (!scriptFile.exists()) {
                chat(regular(command.result("notFound", variable(name))))
                return@handler
            }

            unloadIfLoaded(scriptFile, command, name)
            loadScriptWithDebug(args, scriptFile, command, name)
        }
        .build()

    private fun loadScriptWithDebug(
        args: Array<out Any>,
        scriptFile: File,
        command: Command,
        name: String
    ) {
        val protocol = args.getOrNull(1) as DebugProtocol? ?: DebugProtocol.INSPECT

        runCatching {
            ScriptManager.loadScript(
                scriptFile, debugOptions = ScriptDebugOptions(
                    enabled = true,
                    protocol = protocol,
                    suspendOnStart = args.getOrNull(2) as Boolean? == true,
                    inspectInternals = args.getOrNull(3) as Boolean? == true,
                    port = args.getOrNull(4) as Int?
                        ?: if (protocol == DebugProtocol.INSPECT) 4242 else 4711,
                )
            ).enable()
        }.onSuccess {
            chat(regular(command.result("loaded", variable(name))))
        }.onFailure {
            chat(regular(command.result("failedToLoad", variable(it.message ?: "unknown"))))
        }
    }

    private fun unloadIfLoaded(
        scriptFile: File,
        command: Command,
        name: String
    ) {
        ScriptManager.scripts.find { it.file == scriptFile }?.also { script ->
            chat(regular(command.result("alreadyLoaded", variable(name))))

            runCatching {
                ScriptManager.unloadScript(script)
            }.onSuccess {
                chat(regular(command.result("unloaded", variable(name))))
            }.onFailure {
                chat(regular(command.result("failedToUnload", variable(it.message ?: "unknown"))))
            }
        }
    }

    private fun unloadSubcommand() = CommandBuilder.begin("unload").parameter(
        ParameterBuilder.begin<String>("name").verifiedBy(ParameterBuilder.STRING_VALIDATOR).required()
            .autocompletedFrom {
                ScriptManager.scripts.map { it.scriptName }
            }
            .build()
    ).handler {
        val name = args[0] as String

        val script = ScriptManager.scripts.find { it.scriptName.equals(name, true) }

        if (script == null) {
            chat(regular(command.result("notFound", variable(name))))
            return@handler
        }

        runCatching {
            ScriptManager.unloadScript(script)
        }.onSuccess {
            chat(regular(command.result("unloaded", variable(name))))
        }.onFailure {
            chat(regular(command.result("failedToUnload", variable(it.message ?: "unknown"))))
        }
    }.build()

    private fun loadSubcommand() = CommandBuilder.begin("load").parameter(
        ParameterBuilder.begin<String>("name")
            .verifiedBy(ParameterBuilder.STRING_VALIDATOR)
            .required()
            .autocompletedFromScriptNames()
            .build()
    ).handler {
        val name = args[0] as String
        val scriptFile = ScriptManager.root.resolve(name)

        if (!scriptFile.exists()) {
            chat(regular(command.result("notFound", variable(name))))
            return@handler
        }

        // Check if script is already loaded
        if (ScriptManager.scripts.any { it.file == scriptFile }) {
            chat(regular(command.result("alreadyLoaded", variable(name))))
            return@handler
        }

        runCatching {
            ScriptManager.loadScript(scriptFile).enable()
        }.onSuccess {
            chat(regular(command.result("loaded", variable(name))))
        }.onFailure {
            chat(regular(command.result("failedToLoad", variable(it.message ?: "unknown"))))
        }

    }.build()

    private fun reloadSubcommand() = CommandBuilder.begin("reload").handler {
        runCatching {
            ScriptManager.reload()
        }.onSuccess {
            chat(regular(command.result("reloaded")))
        }.onFailure {
            chat(regular(command.result("reloadFailed", variable(it.message ?: "unknown"))))
        }
    }.build()

}
