package io.github.joshrotenberg.dockerkotlin.core.command.service

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to create a new service.
 *
 * Equivalent to `docker service create`.
 *
 * Example usage:
 * ```kotlin
 * ServiceCreateCommand("nginx:latest")
 *     .name("my-nginx")
 *     .replicas(3)
 *     .port(80, 80)
 *     .executeBlocking()
 * ```
 */
class ServiceCreateCommand(
    private val image: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var name: String? = null
    private var replicas: Int? = null
    private var mode: String? = null
    private val envVars = mutableMapOf<String, String>()
    private val labels = mutableMapOf<String, String>()
    private val containerLabels = mutableMapOf<String, String>()
    private val ports = mutableListOf<String>()
    private val mounts = mutableListOf<String>()
    private val networks = mutableListOf<String>()
    private val constraints = mutableListOf<String>()
    private val secrets = mutableListOf<String>()
    private val configs = mutableListOf<String>()
    private var workdir: String? = null
    private var user: String? = null
    private var entrypoint: String? = null
    private val command = mutableListOf<String>()
    private var restartCondition: String? = null
    private var restartDelay: String? = null
    private var restartMaxAttempts: Int? = null
    private var restartWindow: String? = null
    private var updateDelay: String? = null
    private var updateParallelism: Int? = null
    private var updateFailureAction: String? = null
    private var updateOrder: String? = null
    private var rollbackDelay: String? = null
    private var rollbackParallelism: Int? = null
    private var rollbackFailureAction: String? = null
    private var rollbackOrder: String? = null
    private var limitCpu: String? = null
    private var limitMemory: String? = null
    private var reserveCpu: String? = null
    private var reserveMemory: String? = null
    private var endpointMode: String? = null
    private var hostname: String? = null
    private var healthCmd: String? = null
    private var healthInterval: String? = null
    private var healthRetries: Int? = null
    private var healthTimeout: String? = null
    private var healthStartPeriod: String? = null
    private var noHealthcheck = false
    private var readOnly = false
    private var tty = false
    private var detach = true

    /** Set service name. */
    fun name(name: String) = apply { this.name = name }

    /** Number of tasks (replicas). */
    fun replicas(replicas: Int) = apply { this.replicas = replicas }

    /** Service mode (replicated or global). */
    fun mode(mode: String) = apply { this.mode = mode }

    /** Set an environment variable. */
    fun env(key: String, value: String) = apply { envVars[key] = value }

    /** Set multiple environment variables. */
    fun env(vars: Map<String, String>) = apply { envVars.putAll(vars) }

    /** Add a service label. */
    fun label(key: String, value: String) = apply { labels[key] = value }

    /** Add a container label. */
    fun containerLabel(key: String, value: String) = apply { containerLabels[key] = value }

    /** Publish a port. */
    fun port(published: Int, target: Int, protocol: String = "tcp") = apply {
        ports.add("published=$published,target=$target,protocol=$protocol")
    }

    /** Publish a port with full spec. */
    fun port(spec: String) = apply { ports.add(spec) }

    /** Add a mount. */
    fun mount(spec: String) = apply { mounts.add(spec) }

    /** Add a bind mount. */
    fun mount(source: String, target: String, readOnly: Boolean = false) = apply {
        val ro = if (readOnly) ",readonly" else ""
        mounts.add("type=bind,source=$source,target=$target$ro")
    }

    /** Add a volume mount. */
    fun volume(source: String, target: String) = apply {
        mounts.add("type=volume,source=$source,target=$target")
    }

    /** Attach to a network. */
    fun network(network: String) = apply { networks.add(network) }

    /** Add a placement constraint. */
    fun constraint(constraint: String) = apply { constraints.add(constraint) }

    /** Add a secret. */
    fun secret(secret: String) = apply { secrets.add(secret) }

    /** Add a config. */
    fun config(config: String) = apply { configs.add(config) }

    /** Set working directory. */
    fun workdir(workdir: String) = apply { this.workdir = workdir }

    /** Set user. */
    fun user(user: String) = apply { this.user = user }

    /** Set entrypoint. */
    fun entrypoint(entrypoint: String) = apply { this.entrypoint = entrypoint }

    /** Set command to run. */
    fun command(vararg args: String) = apply { command.addAll(args) }

    /** Set restart condition (none, on-failure, any). */
    fun restartCondition(condition: String) = apply { restartCondition = condition }

    /** Set delay between restart attempts. */
    fun restartDelay(delay: String) = apply { restartDelay = delay }

    /** Set maximum number of restart attempts. */
    fun restartMaxAttempts(attempts: Int) = apply { restartMaxAttempts = attempts }

    /** Set window for restart policy evaluation. */
    fun restartWindow(window: String) = apply { restartWindow = window }

    /** Delay between updates. */
    fun updateDelay(delay: String) = apply { updateDelay = delay }

    /** Maximum number of tasks updated simultaneously. */
    fun updateParallelism(parallelism: Int) = apply { updateParallelism = parallelism }

    /** Action on update failure (pause, continue, rollback). */
    fun updateFailureAction(action: String) = apply { updateFailureAction = action }

    /** Update order (start-first, stop-first). */
    fun updateOrder(order: String) = apply { updateOrder = order }

    /** Delay between rollback attempts. */
    fun rollbackDelay(delay: String) = apply { rollbackDelay = delay }

    /** Maximum number of tasks rolled back simultaneously. */
    fun rollbackParallelism(parallelism: Int) = apply { rollbackParallelism = parallelism }

    /** Action on rollback failure. */
    fun rollbackFailureAction(action: String) = apply { rollbackFailureAction = action }

    /** Rollback order. */
    fun rollbackOrder(order: String) = apply { rollbackOrder = order }

    /** Limit CPUs. */
    fun limitCpu(cpu: String) = apply { limitCpu = cpu }

    /** Limit memory. */
    fun limitMemory(memory: String) = apply { limitMemory = memory }

    /** Reserve CPUs. */
    fun reserveCpu(cpu: String) = apply { reserveCpu = cpu }

    /** Reserve memory. */
    fun reserveMemory(memory: String) = apply { reserveMemory = memory }

    /** Endpoint mode (vip or dnsrr). */
    fun endpointMode(mode: String) = apply { endpointMode = mode }

    /** Container hostname. */
    fun hostname(hostname: String) = apply { this.hostname = hostname }

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

    /** Mount root filesystem as read only. */
    fun readOnly() = apply { readOnly = true }

    /** Allocate a pseudo-TTY. */
    fun tty() = apply { tty = true }

    /** Run in foreground (don't detach). */
    fun noDetach() = apply { detach = false }

    override fun buildArgs(): List<String> = buildList {
        add("service")
        add("create")

        name?.let { add("--name"); add(it) }
        replicas?.let { add("--replicas"); add(it.toString()) }
        mode?.let { add("--mode"); add(it) }

        envVars.forEach { (key, value) -> add("--env"); add("$key=$value") }
        labels.forEach { (key, value) -> add("--label"); add("$key=$value") }
        containerLabels.forEach { (key, value) -> add("--container-label"); add("$key=$value") }

        ports.forEach { add("--publish"); add(it) }
        mounts.forEach { add("--mount"); add(it) }
        networks.forEach { add("--network"); add(it) }
        constraints.forEach { add("--constraint"); add(it) }
        secrets.forEach { add("--secret"); add(it) }
        configs.forEach { add("--config"); add(it) }

        workdir?.let { add("--workdir"); add(it) }
        user?.let { add("--user"); add(it) }
        entrypoint?.let { add("--entrypoint"); add(it) }

        restartCondition?.let { add("--restart-condition"); add(it) }
        restartDelay?.let { add("--restart-delay"); add(it) }
        restartMaxAttempts?.let { add("--restart-max-attempts"); add(it.toString()) }
        restartWindow?.let { add("--restart-window"); add(it) }

        updateDelay?.let { add("--update-delay"); add(it) }
        updateParallelism?.let { add("--update-parallelism"); add(it.toString()) }
        updateFailureAction?.let { add("--update-failure-action"); add(it) }
        updateOrder?.let { add("--update-order"); add(it) }

        rollbackDelay?.let { add("--rollback-delay"); add(it) }
        rollbackParallelism?.let { add("--rollback-parallelism"); add(it.toString()) }
        rollbackFailureAction?.let { add("--rollback-failure-action"); add(it) }
        rollbackOrder?.let { add("--rollback-order"); add(it) }

        limitCpu?.let { add("--limit-cpu"); add(it) }
        limitMemory?.let { add("--limit-memory"); add(it) }
        reserveCpu?.let { add("--reserve-cpu"); add(it) }
        reserveMemory?.let { add("--reserve-memory"); add(it) }

        endpointMode?.let { add("--endpoint-mode"); add(it) }
        hostname?.let { add("--hostname"); add(it) }

        healthCmd?.let { add("--health-cmd"); add(it) }
        healthInterval?.let { add("--health-interval"); add(it) }
        healthRetries?.let { add("--health-retries"); add(it.toString()) }
        healthTimeout?.let { add("--health-timeout"); add(it) }
        healthStartPeriod?.let { add("--health-start-period"); add(it) }

        if (noHealthcheck) add("--no-healthcheck")
        if (readOnly) add("--read-only")
        if (tty) add("--tty")
        if (detach) add("--detach")

        add(image)
        addAll(command)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(image: String): ServiceCreateCommand = ServiceCreateCommand(image)
    }
}
