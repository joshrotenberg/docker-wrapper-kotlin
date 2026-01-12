package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more plugins.
 *
 * Equivalent to `docker plugin rm`.
 *
 * Example usage:
 * ```kotlin
 * PluginRmCommand("my-plugin").executeBlocking()
 * PluginRmCommand(listOf("plugin1", "plugin2")).force().executeBlocking()
 * ```
 */
class PluginRmCommand : AbstractDockerCommand<String> {

    private val plugins: List<String>
    private var force = false

    constructor(plugin: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.plugins = listOf(plugin)
    }

    constructor(plugins: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.plugins = plugins
    }

    /** Force the removal of an active plugin. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("rm")
        if (force) add("--force")
        addAll(plugins)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(plugin: String): PluginRmCommand = PluginRmCommand(plugin)

        @JvmStatic
        fun builder(plugins: List<String>): PluginRmCommand = PluginRmCommand(plugins)
    }
}
