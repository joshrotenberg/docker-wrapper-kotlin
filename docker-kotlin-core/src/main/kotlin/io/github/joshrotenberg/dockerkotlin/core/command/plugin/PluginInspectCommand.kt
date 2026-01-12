package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more plugins.
 *
 * Equivalent to `docker plugin inspect`.
 *
 * Example usage:
 * ```kotlin
 * PluginInspectCommand("my-plugin").executeBlocking()
 * PluginInspectCommand("my-plugin").format("{{.Id}}").executeBlocking()
 * ```
 */
class PluginInspectCommand : AbstractDockerCommand<String> {

    private val plugins: List<String>
    private var format: String? = null

    constructor(plugin: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.plugins = listOf(plugin)
    }

    constructor(plugins: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.plugins = plugins
    }

    /** Set output format using Go template. */
    fun format(format: String) = apply { this.format = format }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("inspect")
        format?.let { add("--format"); add(it) }
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
        fun builder(plugin: String): PluginInspectCommand = PluginInspectCommand(plugin)

        @JvmStatic
        fun builder(plugins: List<String>): PluginInspectCommand = PluginInspectCommand(plugins)
    }
}
