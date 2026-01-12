package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to list plugins.
 *
 * Equivalent to `docker plugin ls`.
 *
 * Example usage:
 * ```kotlin
 * PluginLsCommand().executeBlocking()
 * PluginLsCommand().filter("enabled", "true").executeBlocking()
 * PluginLsCommand().quiet().executeBlocking()
 * ```
 */
class PluginLsCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val filters = mutableMapOf<String, String>()
    private var format: String? = null
    private var noTrunc = false
    private var quiet = false

    /** Filter output based on conditions provided. */
    fun filter(key: String, value: String) = apply { filters[key] = value }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Don't truncate output. */
    fun noTrunc() = apply { noTrunc = true }

    /** Only display plugin IDs. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("ls")
        filters.forEach { (key, value) -> add("--filter"); add("$key=$value") }
        format?.let { add("--format"); add(it) }
        if (noTrunc) add("--no-trunc")
        if (quiet) add("--quiet")
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): PluginLsCommand = PluginLsCommand()
    }
}
