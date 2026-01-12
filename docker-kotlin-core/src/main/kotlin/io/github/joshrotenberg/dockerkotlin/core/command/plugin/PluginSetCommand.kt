package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to change settings for a plugin.
 *
 * Equivalent to `docker plugin set`.
 *
 * Example usage:
 * ```kotlin
 * PluginSetCommand("my-plugin", listOf("DEBUG=1")).executeBlocking()
 * PluginSetCommand("my-plugin", listOf("myvar=value", "another=val2")).executeBlocking()
 * ```
 */
class PluginSetCommand(
    private val plugin: String,
    private val settings: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("set")
        add(plugin)
        addAll(settings)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(plugin: String, settings: List<String>): PluginSetCommand =
            PluginSetCommand(plugin, settings)

        @JvmStatic
        fun builder(plugin: String, vararg settings: String): PluginSetCommand =
            PluginSetCommand(plugin, settings.toList())
    }
}
