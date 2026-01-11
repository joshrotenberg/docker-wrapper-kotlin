package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to update configuration of one or more containers.
 *
 * Equivalent to `docker update`.
 *
 * Example usage:
 * ```kotlin
 * UpdateCommand("my-container")
 *     .memory("512m")
 *     .cpus("1.5")
 *     .execute()
 * ```
 */
class UpdateCommand(
    private val containers: List<String>,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<Unit>(executor) {

    constructor(container: String, executor: CommandExecutor = CommandExecutor()) :
            this(listOf(container), executor)

    private var blkioWeight: Int? = null
    private var cpuPeriod: Long? = null
    private var cpuQuota: Long? = null
    private var cpuRtPeriod: Long? = null
    private var cpuRtRuntime: Long? = null
    private var cpuShares: Int? = null
    private var cpus: String? = null
    private var cpusetCpus: String? = null
    private var cpusetMems: String? = null
    private var memory: String? = null
    private var memoryReservation: String? = null
    private var memorySwap: String? = null
    private var pidsLimit: Long? = null
    private var restart: String? = null

    /** Block IO weight (relative weight), 10-1000. */
    fun blkioWeight(weight: Int) = apply { this.blkioWeight = weight }

    /** Limit CPU CFS (Completely Fair Scheduler) period. */
    fun cpuPeriod(period: Long) = apply { this.cpuPeriod = period }

    /** Limit CPU CFS quota. */
    fun cpuQuota(quota: Long) = apply { this.cpuQuota = quota }

    /** Limit CPU real-time period in microseconds. */
    fun cpuRtPeriod(period: Long) = apply { this.cpuRtPeriod = period }

    /** Limit CPU real-time runtime in microseconds. */
    fun cpuRtRuntime(runtime: Long) = apply { this.cpuRtRuntime = runtime }

    /** CPU shares (relative weight). */
    fun cpuShares(shares: Int) = apply { this.cpuShares = shares }

    /** Number of CPUs. */
    fun cpus(cpus: String) = apply { this.cpus = cpus }

    /** CPUs in which to allow execution (0-3, 0,1). */
    fun cpusetCpus(cpus: String) = apply { this.cpusetCpus = cpus }

    /** MEMs in which to allow execution (0-3, 0,1). */
    fun cpusetMems(mems: String) = apply { this.cpusetMems = mems }

    /** Memory limit. */
    fun memory(memory: String) = apply { this.memory = memory }

    /** Memory soft limit. */
    fun memoryReservation(reservation: String) = apply { this.memoryReservation = reservation }

    /** Swap limit equal to memory plus swap: -1 to enable unlimited swap. */
    fun memorySwap(swap: String) = apply { this.memorySwap = swap }

    /** Tune container pids limit (-1 for unlimited). */
    fun pidsLimit(limit: Long) = apply { this.pidsLimit = limit }

    /** Restart policy to apply when a container exits. */
    fun restart(policy: String) = apply { this.restart = policy }

    override fun buildArgs(): List<String> = buildList {
        add("update")
        blkioWeight?.let { add("--blkio-weight"); add(it.toString()) }
        cpuPeriod?.let { add("--cpu-period"); add(it.toString()) }
        cpuQuota?.let { add("--cpu-quota"); add(it.toString()) }
        cpuRtPeriod?.let { add("--cpu-rt-period"); add(it.toString()) }
        cpuRtRuntime?.let { add("--cpu-rt-runtime"); add(it.toString()) }
        cpuShares?.let { add("--cpu-shares"); add(it.toString()) }
        cpus?.let { add("--cpus"); add(it) }
        cpusetCpus?.let { add("--cpuset-cpus"); add(it) }
        cpusetMems?.let { add("--cpuset-mems"); add(it) }
        memory?.let { add("--memory"); add(it) }
        memoryReservation?.let { add("--memory-reservation"); add(it) }
        memorySwap?.let { add("--memory-swap"); add(it) }
        pidsLimit?.let { add("--pids-limit"); add(it.toString()) }
        restart?.let { add("--restart"); add(it) }
        addAll(containers)
    }

    override suspend fun execute() {
        executeRaw()
    }

    override fun executeBlocking() {
        executeRawBlocking()
    }

    companion object {
        @JvmStatic
        fun builder(container: String): UpdateCommand = UpdateCommand(container)

        @JvmStatic
        fun builder(containers: List<String>): UpdateCommand = UpdateCommand(containers)
    }
}
