package io.github.joshrotenberg.dockerkotlin.core.command.plugin

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to upgrade an existing plugin.
 *
 * Equivalent to `docker plugin upgrade`.
 *
 * Example usage:
 * ```kotlin
 * PluginUpgradeCommand("my-plugin", "user/my-plugin:latest").executeBlocking()
 * PluginUpgradeCommand("my-plugin", "user/my-plugin:v2").grantAllPermissions().executeBlocking()
 * ```
 */
class PluginUpgradeCommand(
    private val plugin: String,
    private val remote: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var disableContentTrust = false
    private var grantAllPermissions = false
    private var skipRemoteCheck = false

    /** Skip image verification. */
    fun disableContentTrust() = apply { disableContentTrust = true }

    /** Grant all permissions necessary to run the plugin. */
    fun grantAllPermissions() = apply { grantAllPermissions = true }

    /** Do not check if specified remote plugin matches existing plugin image. */
    fun skipRemoteCheck() = apply { skipRemoteCheck = true }

    override fun buildArgs(): List<String> = buildList {
        add("plugin")
        add("upgrade")
        if (disableContentTrust) add("--disable-content-trust")
        if (grantAllPermissions) add("--grant-all-permissions")
        if (skipRemoteCheck) add("--skip-remote-check")
        add(plugin)
        add(remote)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(plugin: String, remote: String): PluginUpgradeCommand =
            PluginUpgradeCommand(plugin, remote)
    }
}
