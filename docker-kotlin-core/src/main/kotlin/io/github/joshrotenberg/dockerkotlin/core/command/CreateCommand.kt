package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to create a new container without starting it.
 *
 * Equivalent to `docker create`.
 *
 * This command has the same options as RunCommand but does not start the container.
 *
 * Example usage:
 * ```kotlin
 * val containerId = CreateCommand("nginx:latest")
 *     .name("my-nginx")
 *     .publish(8080, 80)
 *     .execute()
 * ```
 */
class CreateCommand(
    private val image: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<ContainerId>(executor) {

    private var name: String? = null
    private val envVars = mutableMapOf<String, String>()
    private val ports = mutableListOf<String>()
    private val volumes = mutableListOf<String>()
    private val labels = mutableMapOf<String, String>()
    private var workdir: String? = null
    private var user: String? = null
    private var hostname: String? = null
    private var network: String? = null
    private var restart: String? = null
    private var entrypoint: String? = null
    private val command = mutableListOf<String>()
    private var interactive: Boolean = false
    private var tty: Boolean = false
    private var privileged: Boolean = false
    private var readOnly: Boolean = false
    private var init: Boolean = false
    private var platform: String? = null
    private var pull: String? = null
    private var memory: String? = null
    private var memorySwap: String? = null
    private var cpus: String? = null
    private var cpuShares: Int? = null

    /** Assign a name to the container. */
    fun name(name: String) = apply { this.name = name }

    /** Set environment variable. */
    fun env(key: String, value: String) = apply { this.envVars[key] = value }

    /** Set multiple environment variables. */
    fun envs(vars: Map<String, String>) = apply { this.envVars.putAll(vars) }

    /** Publish a container's port to the host. */
    fun publish(hostPort: Int, containerPort: Int) = apply {
        this.ports.add("$hostPort:$containerPort")
    }

    /** Publish a container's port with protocol. */
    fun publish(hostPort: Int, containerPort: Int, protocol: String) = apply {
        this.ports.add("$hostPort:$containerPort/$protocol")
    }

    /** Publish all exposed ports. */
    fun publishAll() = apply { this.ports.add("-P") }

    /** Bind mount a volume. */
    fun volume(hostPath: String, containerPath: String) = apply {
        this.volumes.add("$hostPath:$containerPath")
    }

    /** Bind mount a volume with options. */
    fun volume(hostPath: String, containerPath: String, options: String) = apply {
        this.volumes.add("$hostPath:$containerPath:$options")
    }

    /** Add a label. */
    fun label(key: String, value: String) = apply { this.labels[key] = value }

    /** Working directory inside the container. */
    fun workdir(dir: String) = apply { this.workdir = dir }

    /** Username or UID. */
    fun user(user: String) = apply { this.user = user }

    /** Container host name. */
    fun hostname(hostname: String) = apply { this.hostname = hostname }

    /** Connect to a network. */
    fun network(network: String) = apply { this.network = network }

    /** Restart policy. */
    fun restart(policy: String) = apply { this.restart = policy }

    /** Overwrite the default ENTRYPOINT. */
    fun entrypoint(entrypoint: String) = apply { this.entrypoint = entrypoint }

    /** Command to run. */
    fun command(vararg cmd: String) = apply { this.command.addAll(cmd) }

    /** Keep STDIN open. */
    fun interactive() = apply { this.interactive = true }

    /** Allocate a pseudo-TTY. */
    fun tty() = apply { this.tty = true }

    /** Give extended privileges. */
    fun privileged() = apply { this.privileged = true }

    /** Mount the container's root filesystem as read only. */
    fun readOnly() = apply { this.readOnly = true }

    /** Run an init inside the container. */
    fun init() = apply { this.init = true }

    /** Set platform. */
    fun platform(platform: String) = apply { this.platform = platform }

    /** Pull image before creating ("always", "missing", "never"). */
    fun pull(policy: String) = apply { this.pull = policy }

    /** Memory limit. */
    fun memory(limit: String) = apply { this.memory = limit }

    /** Swap limit equal to memory plus swap. */
    fun memorySwap(limit: String) = apply { this.memorySwap = limit }

    /** Number of CPUs. */
    fun cpus(cpus: String) = apply { this.cpus = cpus }

    /** CPU shares (relative weight). */
    fun cpuShares(shares: Int) = apply { this.cpuShares = shares }

    override fun buildArgs(): List<String> = buildList {
        add("create")
        name?.let { add("--name"); add(it) }
        envVars.forEach { (key, value) -> add("--env"); add("$key=$value") }
        ports.forEach {
            if (it == "-P") add("-P") else {
                add("--publish"); add(it)
            }
        }
        volumes.forEach { add("--volume"); add(it) }
        labels.forEach { (key, value) -> add("--label"); add("$key=$value") }
        workdir?.let { add("--workdir"); add(it) }
        user?.let { add("--user"); add(it) }
        hostname?.let { add("--hostname"); add(it) }
        network?.let { add("--network"); add(it) }
        restart?.let { add("--restart"); add(it) }
        entrypoint?.let { add("--entrypoint"); add(it) }
        if (interactive) add("--interactive")
        if (tty) add("--tty")
        if (privileged) add("--privileged")
        if (readOnly) add("--read-only")
        if (init) add("--init")
        platform?.let { add("--platform"); add(it) }
        pull?.let { add("--pull"); add(it) }
        memory?.let { add("--memory"); add(it) }
        memorySwap?.let { add("--memory-swap"); add(it) }
        cpus?.let { add("--cpus"); add(it) }
        cpuShares?.let { add("--cpu-shares"); add(it.toString()) }
        add(image)
        addAll(command)
    }

    override suspend fun execute(): ContainerId {
        val output = executeRaw()
        return ContainerId(output.stdout.trim())
    }

    override fun executeBlocking(): ContainerId {
        val output = executeRawBlocking()
        return ContainerId(output.stdout.trim())
    }

    companion object {
        @JvmStatic
        fun builder(image: String): CreateCommand = CreateCommand(image)
    }
}
