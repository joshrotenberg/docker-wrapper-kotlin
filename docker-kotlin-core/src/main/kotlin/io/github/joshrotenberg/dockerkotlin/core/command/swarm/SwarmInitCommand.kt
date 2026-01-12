package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to initialize a swarm.
 *
 * Equivalent to `docker swarm init`.
 *
 * Example usage:
 * ```kotlin
 * SwarmInitCommand()
 *     .advertiseAddr("192.168.1.100")
 *     .execute()
 * ```
 */
class SwarmInitCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var advertiseAddr: String? = null
    private var autolock = false
    private var availability: NodeAvailability? = null
    private var certExpiry: String? = null
    private var dataPathAddr: String? = null
    private var dataPathPort: Int? = null
    private var defaultAddrPool: String? = null
    private var dispatcherHeartbeat: String? = null
    private var externalCa: String? = null
    private var forceNewCluster = false
    private var listenAddr: String? = null
    private var maxSnapshots: Int? = null
    private var snapshotInterval: Int? = null
    private var taskHistoryLimit: Int? = null

    /** Advertised address (format: "<ip|interface>[:port]"). */
    fun advertiseAddr(addr: String) = apply { advertiseAddr = addr }

    /** Enable manager autolocking (requiring unlock key to start). */
    fun autolock() = apply { autolock = true }

    /** Availability of the node ("active", "pause", "drain"). */
    fun availability(availability: NodeAvailability) = apply { this.availability = availability }

    /** Validity period for node certificates (e.g., "2160h0m0s"). */
    fun certExpiry(expiry: String) = apply { certExpiry = expiry }

    /** Address or interface to use for data path traffic. */
    fun dataPathAddr(addr: String) = apply { dataPathAddr = addr }

    /** Port number to use for data path traffic (1024-49151). */
    fun dataPathPort(port: Int) = apply { dataPathPort = port }

    /** Default address pool in CIDR format for global scope networks. */
    fun defaultAddrPool(pool: String) = apply { defaultAddrPool = pool }

    /** Dispatcher heartbeat period (e.g., "5s"). */
    fun dispatcherHeartbeat(heartbeat: String) = apply { dispatcherHeartbeat = heartbeat }

    /** Specifications of one or more certificate signing endpoints. */
    fun externalCa(ca: String) = apply { externalCa = ca }

    /** Force create a new cluster from current state. */
    fun forceNewCluster() = apply { forceNewCluster = true }

    /** Listen address (format: "<ip|interface>[:port]"). */
    fun listenAddr(addr: String) = apply { listenAddr = addr }

    /** Number of additional Raft snapshots to retain. */
    fun maxSnapshots(count: Int) = apply { maxSnapshots = count }

    /** Number of log entries between Raft snapshots. */
    fun snapshotInterval(interval: Int) = apply { snapshotInterval = interval }

    /** Task history retention limit. */
    fun taskHistoryLimit(limit: Int) = apply { taskHistoryLimit = limit }

    override fun buildArgs(): List<String> = buildList {
        add("swarm")
        add("init")
        advertiseAddr?.let { add("--advertise-addr"); add(it) }
        if (autolock) add("--autolock")
        availability?.let { add("--availability"); add(it.value) }
        certExpiry?.let { add("--cert-expiry"); add(it) }
        dataPathAddr?.let { add("--data-path-addr"); add(it) }
        dataPathPort?.let { add("--data-path-port"); add(it.toString()) }
        defaultAddrPool?.let { add("--default-addr-pool"); add(it) }
        dispatcherHeartbeat?.let { add("--dispatcher-heartbeat"); add(it) }
        externalCa?.let { add("--external-ca"); add(it) }
        if (forceNewCluster) add("--force-new-cluster")
        listenAddr?.let { add("--listen-addr"); add(it) }
        maxSnapshots?.let { add("--max-snapshots"); add(it.toString()) }
        snapshotInterval?.let { add("--snapshot-interval"); add(it.toString()) }
        taskHistoryLimit?.let { add("--task-history-limit"); add(it.toString()) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): SwarmInitCommand = SwarmInitCommand()
    }
}

/**
 * Node availability status.
 */
enum class NodeAvailability(val value: String) {
    ACTIVE("active"),
    PAUSE("pause"),
    DRAIN("drain")
}
