package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more networks.
 *
 * Equivalent to `docker network inspect`.
 *
 * Example usage:
 * ```kotlin
 * NetworkInspectCommand("my-network").executeBlocking()
 * NetworkInspectCommand("my-network").format("{{.Driver}}").executeBlocking()
 * ```
 */
class NetworkInspectCommand : AbstractDockerCommand<String> {

    private val networks: List<String>
    private var format: String? = null
    private var verbose = false

    constructor(network: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.networks = listOf(network)
    }

    constructor(networks: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.networks = networks
    }

    /** Set output format using Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Include verbose information. */
    fun verbose() = apply { verbose = true }

    override fun buildArgs(): List<String> = buildList {
        add("network")
        add("inspect")
        format?.let { add("--format"); add(it) }
        if (verbose) add("--verbose")
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
        fun builder(network: String): NetworkInspectCommand = NetworkInspectCommand(network)

        @JvmStatic
        fun builder(networks: List<String>): NetworkInspectCommand = NetworkInspectCommand(networks)
    }
}
