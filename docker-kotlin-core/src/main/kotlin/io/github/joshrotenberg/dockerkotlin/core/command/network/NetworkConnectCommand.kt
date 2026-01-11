package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to connect a container to a network.
 *
 * Equivalent to `docker network connect`.
 *
 * Example usage:
 * ```kotlin
 * NetworkConnectCommand("my-network", "my-container")
 *     .ipv4("172.20.0.10")
 *     .alias("db")
 *     .executeBlocking()
 * ```
 */
class NetworkConnectCommand(
    private val network: String,
    private val container: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var ipv4: String? = null
    private var ipv6: String? = null
    private val aliases = mutableListOf<String>()
    private val links = mutableListOf<String>()
    private val linkLocalIps = mutableListOf<String>()
    private val driverOpts = mutableListOf<Pair<String, String>>()

    /** Set IPv4 address. */
    fun ipv4(ip: String) = apply { this.ipv4 = ip }

    /** Set IPv6 address. */
    fun ipv6(ip: String) = apply { this.ipv6 = ip }

    /** Add a network-scoped alias. */
    fun alias(alias: String) = apply { aliases.add(alias) }

    /** Add a link to another container. */
    fun link(container: String) = apply { links.add(container) }

    /** Add a link-local IP address. */
    fun linkLocalIp(ip: String) = apply { linkLocalIps.add(ip) }

    /** Add a driver option. */
    fun driverOpt(key: String, value: String) = apply { driverOpts.add(key to value) }

    override fun buildArgs(): List<String> = buildList {
        add("network")
        add("connect")

        ipv4?.let { add("--ip"); add(it) }
        ipv6?.let { add("--ip6"); add(it) }

        aliases.forEach { add("--alias"); add(it) }
        links.forEach { add("--link"); add(it) }
        linkLocalIps.forEach { add("--link-local-ip"); add(it) }
        driverOpts.forEach { (key, value) -> add("--driver-opt"); add("$key=$value") }

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
        fun builder(network: String, container: String): NetworkConnectCommand =
            NetworkConnectCommand(network, container)
    }
}
