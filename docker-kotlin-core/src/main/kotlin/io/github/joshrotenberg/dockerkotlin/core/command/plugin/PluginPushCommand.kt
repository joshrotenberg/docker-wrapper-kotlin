package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to push a plugin to a registry.
 *
 * Equivalent to `docker plugin push`.
 *
 * Example usage:
 * ```kotlin
 * PluginPushCommand("user/my-plugin").executeBlocking()
 * PluginPushCommand("user/my-plugin").disableContentTrust().executeBlocking()
 * ```
 */
class PluginPushCommand(
    private val plugin: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var disableContentTrust = false

    /** Skip image signing. */
    fun disableContentTrust() = apply { disableContentTrust = true }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("push")
        if (disableContentTrust) add("--disable-content-trust")
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
        fun builder(plugin: String): PluginPushCommand = PluginPushCommand(plugin)
    }
}
