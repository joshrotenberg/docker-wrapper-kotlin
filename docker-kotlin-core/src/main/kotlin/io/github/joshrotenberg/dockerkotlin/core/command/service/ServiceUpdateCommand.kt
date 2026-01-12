package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to update a service.
 *
 * Equivalent to `docker service update`.
 *
 * Example usage:
 * ```kotlin
 * ServiceUpdateCommand("my-service")
 *     .image("nginx:latest")
 *     .replicas(5)
 *     .executeBlocking()
 * ```
 */
class ServiceUpdateCommand(
    private val service: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var image: String? = null
    private var replicas: Int? = null
    private var force = false
    private var rollback = false
    private val envAdd = mutableMapOf<String, String>()
    private val envRm = mutableListOf<String>()
    private val labelAdd = mutableMapOf<String, String>()
    private val labelRm = mutableListOf<String>()
    private val constraintAdd = mutableListOf<String>()
    private val constraintRm = mutableListOf<String>()
    private val portAdd = mutableListOf<String>()
    private val portRm = mutableListOf<String>()
    private val mountAdd = mutableListOf<String>()
    private val mountRm = mutableListOf<String>()
    private val networkAdd = mutableListOf<String>()
    private val networkRm = mutableListOf<String>()
    private val secretAdd = mutableListOf<String>()
    private val secretRm = mutableListOf<String>()
    private val configAdd = mutableListOf<String>()
    private val configRm = mutableListOf<String>()
    private var limitCpu: String? = null
    private var limitMemory: String? = null
    private var reserveCpu: String? = null
    private var reserveMemory: String? = null
    private var updateDelay: String? = null
    private var updateParallelism: Int? = null
    private var updateFailureAction: String? = null
    private var updateOrder: String? = null
    private var rollbackDelay: String? = null
    private var rollbackParallelism: Int? = null
    private var rollbackFailureAction: String? = null
    private var rollbackOrder: String? = null
    private var healthCmd: String? = null
    private var healthInterval: String? = null
    private var healthRetries: Int? = null
    private var healthTimeout: String? = null
    private var healthStartPeriod: String? = null
    private var noHealthcheck = false
    private var detach = true
    private var quiet = false

    /** Update the service image. */
    fun image(image: String) = apply { this.image = image }

    /** Number of tasks (replicas). */
    fun replicas(replicas: Int) = apply { this.replicas = replicas }

    /** Force update even if no changes require it. */
    fun force() = apply { force = true }

    /** Rollback to previous specification. */
    fun rollback() = apply { rollback = true }

    /** Add an environment variable. */
    fun envAdd(key: String, value: String) = apply { envAdd[key] = value }

    /** Remove an environment variable. */
    fun envRm(key: String) = apply { envRm.add(key) }

    /** Add a label. */
    fun labelAdd(key: String, value: String) = apply { labelAdd[key] = value }

    /** Remove a label. */
    fun labelRm(key: String) = apply { labelRm.add(key) }

    /** Add a placement constraint. */
    fun constraintAdd(constraint: String) = apply { constraintAdd.add(constraint) }

    /** Remove a placement constraint. */
    fun constraintRm(constraint: String) = apply { constraintRm.add(constraint) }

    /** Add a port. */
    fun portAdd(spec: String) = apply { portAdd.add(spec) }

    /** Remove a port. */
    fun portRm(spec: String) = apply { portRm.add(spec) }

    /** Add a mount. */
    fun mountAdd(spec: String) = apply { mountAdd.add(spec) }

    /** Remove a mount. */
    fun mountRm(target: String) = apply { mountRm.add(target) }

    /** Add a network. */
    fun networkAdd(network: String) = apply { networkAdd.add(network) }

    /** Remove a network. */
    fun networkRm(network: String) = apply { networkRm.add(network) }

    /** Add a secret. */
    fun secretAdd(secret: String) = apply { secretAdd.add(secret) }

    /** Remove a secret. */
    fun secretRm(secret: String) = apply { secretRm.add(secret) }

    /** Add a config. */
    fun configAdd(config: String) = apply { configAdd.add(config) }

    /** Remove a config. */
    fun configRm(config: String) = apply { configRm.add(config) }

    /** Limit CPUs. */
    fun limitCpu(cpu: String) = apply { limitCpu = cpu }

    /** Limit memory. */
    fun limitMemory(memory: String) = apply { limitMemory = memory }

    /** Reserve CPUs. */
    fun reserveCpu(cpu: String) = apply { reserveCpu = cpu }

    /** Reserve memory. */
    fun reserveMemory(memory: String) = apply { reserveMemory = memory }

    /** Delay between updates. */
    fun updateDelay(delay: String) = apply { updateDelay = delay }

    /** Maximum number of tasks updated simultaneously. */
    fun updateParallelism(parallelism: Int) = apply { updateParallelism = parallelism }

    /** Action on update failure. */
    fun updateFailureAction(action: String) = apply { updateFailureAction = action }

    /** Update order. */
    fun updateOrder(order: String) = apply { updateOrder = order }

    /** Delay between rollback attempts. */
    fun rollbackDelay(delay: String) = apply { rollbackDelay = delay }

    /** Maximum number of tasks rolled back simultaneously. */
    fun rollbackParallelism(parallelism: Int) = apply { rollbackParallelism = parallelism }

    /** Action on rollback failure. */
    fun rollbackFailureAction(action: String) = apply { rollbackFailureAction = action }

    /** Rollback order. */
    fun rollbackOrder(order: String) = apply { rollbackOrder = order }

    /** Health check command. */
    fun healthCmd(cmd: String) = apply { healthCmd = cmd }

    /** Health check interval. */
    fun healthInterval(interval: String) = apply { healthInterval = interval }

    /** Health check retries. */
    fun healthRetries(retries: Int) = apply { healthRetries = retries }

    /** Health check timeout. */
    fun healthTimeout(timeout: String) = apply { healthTimeout = timeout }

    /** Health check start period. */
    fun healthStartPeriod(period: String) = apply { healthStartPeriod = period }

    /** Disable health check. */
    fun noHealthcheck() = apply { noHealthcheck = true }

    /** Run in foreground (don't detach). */
    fun noDetach() = apply { detach = false }

    /** Suppress progress output. */
    fun quiet() = apply { quiet = true }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("update")

        image?.let { add("--image"); add(it) }
        replicas?.let { add("--replicas"); add(it.toString()) }
        if (force) add("--force")
        if (rollback) add("--rollback")

        envAdd.forEach { (key, value) -> add("--env-add"); add("$key=$value") }
        envRm.forEach { add("--env-rm"); add(it) }
        labelAdd.forEach { (key, value) -> add("--label-add"); add("$key=$value") }
        labelRm.forEach { add("--label-rm"); add(it) }
        constraintAdd.forEach { add("--constraint-add"); add(it) }
        constraintRm.forEach { add("--constraint-rm"); add(it) }
        portAdd.forEach { add("--publish-add"); add(it) }
        portRm.forEach { add("--publish-rm"); add(it) }
        mountAdd.forEach { add("--mount-add"); add(it) }
        mountRm.forEach { add("--mount-rm"); add(it) }
        networkAdd.forEach { add("--network-add"); add(it) }
        networkRm.forEach { add("--network-rm"); add(it) }
        secretAdd.forEach { add("--secret-add"); add(it) }
        secretRm.forEach { add("--secret-rm"); add(it) }
        configAdd.forEach { add("--config-add"); add(it) }
        configRm.forEach { add("--config-rm"); add(it) }

        limitCpu?.let { add("--limit-cpu"); add(it) }
        limitMemory?.let { add("--limit-memory"); add(it) }
        reserveCpu?.let { add("--reserve-cpu"); add(it) }
        reserveMemory?.let { add("--reserve-memory"); add(it) }

        updateDelay?.let { add("--update-delay"); add(it) }
        updateParallelism?.let { add("--update-parallelism"); add(it.toString()) }
        updateFailureAction?.let { add("--update-failure-action"); add(it) }
        updateOrder?.let { add("--update-order"); add(it) }

        rollbackDelay?.let { add("--rollback-delay"); add(it) }
        rollbackParallelism?.let { add("--rollback-parallelism"); add(it.toString()) }
        rollbackFailureAction?.let { add("--rollback-failure-action"); add(it) }
        rollbackOrder?.let { add("--rollback-order"); add(it) }

        healthCmd?.let { add("--health-cmd"); add(it) }
        healthInterval?.let { add("--health-interval"); add(it) }
        healthRetries?.let { add("--health-retries"); add(it.toString()) }
        healthTimeout?.let { add("--health-timeout"); add(it) }
        healthStartPeriod?.let { add("--health-start-period"); add(it) }

        if (noHealthcheck) add("--no-healthcheck")
        if (detach) add("--detach")
        if (quiet) add("--quiet")

        add(service)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(service: String): ServiceUpdateCommand = ServiceUpdateCommand(service)
    }
}
