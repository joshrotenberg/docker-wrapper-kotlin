package io.github.joshrotenberg.dockerkotlin.core.command.network

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to create a Docker network.
 *
 * Equivalent to `docker network create`.
 *
 * Example usage:
 * ```kotlin
 * NetworkCreateCommand("my-network")
 *     .driver("bridge")
 *     .subnet("172.20.0.0/16")
 *     .executeBlocking()
 * ```
 */
class NetworkCreateCommand(
    private val name: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var driver: String? = null
    private val driverOpts = mutableMapOf<String, String>()
    private var subnet: String? = null
    private var ipRange: String? = null
    private var gateway: String? = null
    private var ipv6 = false
    private var attachable = false
    private var internal = false
    private val labels = mutableMapOf<String, String>()
    private var scope: String? = null
    private var configFrom: String? = null
    private var configOnly = false
    private var ingress = false
    private val auxAddresses = mutableMapOf<String, String>()

    /** Set the network driver (bridge, overlay, macvlan, none, etc.). */
    fun driver(driver: String) = apply { this.driver = driver }

    /** Add a driver-specific option. */
    fun driverOpt(key: String, value: String) = apply { driverOpts[key] = value }

    /** Set the subnet in CIDR format (e.g., "172.20.0.0/16"). */
    fun subnet(subnet: String) = apply { this.subnet = subnet }

    /** Set the IP range in CIDR format. */
    fun ipRange(range: String) = apply { this.ipRange = range }

    /** Set the gateway IP address. */
    fun gateway(gateway: String) = apply { this.gateway = gateway }

    /** Enable IPv6 networking. */
    fun ipv6() = apply { ipv6 = true }

    /** Enable manual container attachment. */
    fun attachable() = apply { attachable = true }

    /** Restrict external access to the network. */
    fun internal() = apply { internal = true }

    /** Add a network label. */
    fun label(key: String, value: String) = apply { labels[key] = value }

    /** Set network scope (local, swarm, global). */
    fun scope(scope: String) = apply { this.scope = scope }

    /** Create network from existing config. */
    fun configFrom(network: String) = apply { this.configFrom = network }

    /** Config only (don't create network). */
    fun configOnly() = apply { configOnly = true }

    /** Create an ingress network. */
    fun ingress() = apply { ingress = true }

    /** Add auxiliary address. */
    fun auxAddress(name: String, ip: String) = apply { auxAddresses[name] = ip }

    override fun buildArgs(): List<String> = buildList {
        add("network")
        add("create")

        driver?.let { add("--driver"); add(it) }

        driverOpts.forEach { (key, value) ->
            add("--opt")
            add("$key=$value")
        }

        subnet?.let { add("--subnet"); add(it) }
        ipRange?.let { add("--ip-range"); add(it) }
        gateway?.let { add("--gateway"); add(it) }

        if (ipv6) add("--ipv6")
        if (attachable) add("--attachable")
        if (internal) add("--internal")

        labels.forEach { (key, value) ->
            add("--label")
            add("$key=$value")
        }

        scope?.let { add("--scope"); add(it) }
        configFrom?.let { add("--config-from"); add(it) }
        if (configOnly) add("--config-only")
        if (ingress) add("--ingress")

        auxAddresses.forEach { (name, ip) ->
            add("--aux-address")
            add("$name=$ip")
        }

        add(name)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(name: String): NetworkCreateCommand = NetworkCreateCommand(name)
    }
}
