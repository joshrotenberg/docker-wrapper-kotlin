package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to disconnect a container from a network.
 *
 * Equivalent to `docker network disconnect`.
 *
 * Example usage:
 * ```kotlin
 * NetworkDisconnectCommand("my-network", "my-container").executeBlocking()
 * NetworkDisconnectCommand("my-network", "my-container").force().executeBlocking()
 * ```
 */
class NetworkDisconnectCommand(
    private val network: String,
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var force = false

    /** Force disconnection. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("network")
        add("disconnect")
        if (force) add("--force")
        add(network)
        add(container)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(network: String, container: String): NetworkDisconnectCommand =
            NetworkDisconnectCommand(network, container)
    }
}
