package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to join a swarm as a node and/or manager.
 *
 * Equivalent to `docker swarm join`.
 *
 * Example usage:
 * ```kotlin
 * SwarmJoinCommand("192.168.1.100:2377")
 *     .token("SWMTKN-1-xxx")
 *     .execute()
 * ```
 */
class SwarmJoinCommand(
    private val remoteAddr: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var advertiseAddr: String? = null
    private var availability: NodeAvailability? = null
    private var dataPathAddr: String? = null
    private var listenAddr: String? = null
    private var token: String? = null

    /** Advertised address (format: "<ip|interface>[:port]"). */
    fun advertiseAddr(addr: String) = apply { advertiseAddr = addr }

    /** Availability of the node ("active", "pause", "drain"). */
    fun availability(availability: NodeAvailability) = apply { this.availability = availability }

    /** Address or interface to use for data path traffic. */
    fun dataPathAddr(addr: String) = apply { dataPathAddr = addr }

    /** Listen address (format: "<ip|interface>[:port]"). */
    fun listenAddr(addr: String) = apply { listenAddr = addr }

    /** Token for entry into the swarm. */
    fun token(token: String) = apply { this.token = token }

    override fun buildArgs(): List<String> = buildList {
        add("swarm")
        add("join")
        advertiseAddr?.let { add("--advertise-addr"); add(it) }
        availability?.let { add("--availability"); add(it.value) }
        dataPathAddr?.let { add("--data-path-addr"); add(it) }
        listenAddr?.let { add("--listen-addr"); add(it) }
        token?.let { add("--token"); add(it) }
        add(remoteAddr)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(remoteAddr: String): SwarmJoinCommand = SwarmJoinCommand(remoteAddr)
    }
}
