package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to disable a plugin.
 *
 * Equivalent to `docker plugin disable`.
 *
 * Example usage:
 * ```kotlin
 * PluginDisableCommand("my-plugin").executeBlocking()
 * PluginDisableCommand("my-plugin").force().executeBlocking()
 * ```
 */
class PluginDisableCommand(
    private val plugin: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var force = false

    /** Force the disable of an active plugin. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("disable")
        if (force) add("--force")
        add(plugin)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(plugin: String): PluginDisableCommand = PluginDisableCommand(plugin)
    }
}
