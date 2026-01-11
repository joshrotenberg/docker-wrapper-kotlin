package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more Docker networks.
 *
 * Equivalent to `docker network rm`.
 *
 * Example usage:
 * ```kotlin
 * NetworkRmCommand("my-network").executeBlocking()
 * NetworkRmCommand(listOf("net1", "net2")).force().executeBlocking()
 * ```
 */
class NetworkRmCommand : AbstractDockerCommand<String> {

    private val networks: List<String>
    private var force = false

    constructor(network: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.networks = listOf(network)
    }

    constructor(networks: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.networks = networks
    }

    /** Force removal. */
    fun force() = apply { force = true }

    override fun buildArgs(): List<String> = buildList {
        add("network")
        add("rm")
        if (force) add("--force")
        addAll(networks)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(network: String): NetworkRmCommand = NetworkRmCommand(network)

        @JvmStatic
        fun builder(networks: List<String>): NetworkRmCommand = NetworkRmCommand(networks)
    }
}
