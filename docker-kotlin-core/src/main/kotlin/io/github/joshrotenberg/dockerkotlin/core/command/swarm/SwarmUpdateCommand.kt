package io.github.joshrotenberg.dockerkotlin.core.command.swarm

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to update the swarm.
 *
 * Equivalent to `docker swarm update`.
 *
 * Example usage:
 * ```kotlin
 * SwarmUpdateCommand()
 *     .taskHistoryLimit(10)
 *     .autolock()
 *     .execute()
 * ```
 */
class SwarmUpdateCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    private var autolock: Boolean? = null
    private var certExpiry: String? = null
    private var dispatcherHeartbeat: String? = null
    private var externalCa: String? = null
    private var maxSnapshots: Int? = null
    private var snapshotInterval: Int? = null
    private var taskHistoryLimit: Int? = null

    /** Enable or disable manager autolocking. */
    fun autolock(enabled: Boolean = true) = apply { autolock = enabled }

    /** Validity period for node certificates (e.g., "2160h0m0s"). */
    fun certExpiry(expiry: String) = apply { certExpiry = expiry }

    /** Dispatcher heartbeat period (e.g., "5s"). */
    fun dispatcherHeartbeat(heartbeat: String) = apply { dispatcherHeartbeat = heartbeat }

    /** Specifications of one or more certificate signing endpoints. */
    fun externalCa(ca: String) = apply { externalCa = ca }

    /** Number of additional Raft snapshots to retain. */
    fun maxSnapshots(count: Int) = apply { maxSnapshots = count }

    /** Number of log entries between Raft snapshots. */
    fun snapshotInterval(interval: Int) = apply { snapshotInterval = interval }

    /** Task history retention limit. */
    fun taskHistoryLimit(limit: Int) = apply { taskHistoryLimit = limit }

    override fun buildArgs(): List<String> = buildList {
        add("swarm")
        add("update")
        autolock?.let { add("--autolock=$it") }
        certExpiry?.let { add("--cert-expiry"); add(it) }
        dispatcherHeartbeat?.let { add("--dispatcher-heartbeat"); add(it) }
        externalCa?.let { add("--external-ca"); add(it) }
        maxSnapshots?.let { add("--max-snapshots"); add(it.toString()) }
        snapshotInterval?.let { add("--snapshot-interval"); add(it.toString()) }
        taskHistoryLimit?.let { add("--task-history-limit"); add(it.toString()) }
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(): SwarmUpdateCommand = SwarmUpdateCommand()
    }
}
