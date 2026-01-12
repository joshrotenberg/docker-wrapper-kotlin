package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to enable a plugin.
 *
 * Equivalent to `docker plugin enable`.
 *
 * Example usage:
 * ```kotlin
 * PluginEnableCommand("my-plugin").executeBlocking()
 * PluginEnableCommand("my-plugin").timeout(60).executeBlocking()
 * ```
 */
class PluginEnableCommand(
    private val plugin: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var timeout: Int? = null

    /** HTTP client timeout (in seconds). */
    fun timeout(seconds: Int) = apply { timeout = seconds }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("enable")
        timeout?.let { add("--timeout"); add(it.toString()) }
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
        fun builder(plugin: String): PluginEnableCommand = PluginEnableCommand(plugin)
    }
}
