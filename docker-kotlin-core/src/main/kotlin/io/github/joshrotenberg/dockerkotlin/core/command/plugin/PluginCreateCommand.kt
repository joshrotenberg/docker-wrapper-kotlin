package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to create a plugin from a rootfs and configuration.
 *
 * Equivalent to `docker plugin create`.
 *
 * Example usage:
 * ```kotlin
 * PluginCreateCommand("my-plugin", "/path/to/plugin").executeBlocking()
 * PluginCreateCommand("my-plugin:1.0", "/path/to/plugin").compress().executeBlocking()
 * ```
 */
class PluginCreateCommand(
    private val plugin: String,
    private val pluginDataDir: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var compress = false

    /** Compress the context using gzip. */
    fun compress() = apply { compress = true }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("create")
        if (compress) add("--compress")
        add(plugin)
        add(pluginDataDir)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(plugin: String, pluginDataDir: String): PluginCreateCommand =
            PluginCreateCommand(plugin, pluginDataDir)
    }
}
