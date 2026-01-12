package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to install a plugin.
 *
 * Equivalent to `docker plugin install`.
 *
 * Example usage:
 * ```kotlin
 * PluginInstallCommand("vieux/sshfs").executeBlocking()
 * PluginInstallCommand("vieux/sshfs").grantAllPermissions().executeBlocking()
 * PluginInstallCommand("vieux/sshfs").alias("sshfs").executeBlocking()
 * ```
 */
class PluginInstallCommand(
    private val plugin: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var alias: String? = null
    private var disable = false
    private var disableContentTrust = false
    private var grantAllPermissions = false

    /** Local name for plugin. */
    fun alias(alias: String) = apply { this.alias = alias }

    /** Do not enable the plugin on install. */
    fun disable() = apply { disable = true }

    /** Skip image verification. */
    fun disableContentTrust() = apply { disableContentTrust = true }

    /** Grant all permissions necessary to run the plugin. */
    fun grantAllPermissions() = apply { grantAllPermissions = true }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("install")
        alias?.let { add("--alias"); add(it) }
        if (disable) add("--disable")
        if (disableContentTrust) add("--disable-content-trust")
        if (grantAllPermissions) add("--grant-all-permissions")
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
        fun builder(plugin: String): PluginInstallCommand = PluginInstallCommand(plugin)
    }
}
