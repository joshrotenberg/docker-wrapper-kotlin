package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Container ID returned from running a container.
 */
@JvmInline
value class ContainerId(val value: String) {
    /** Get the short (12-character) container ID. */
    fun short(): String = value.take(12)

    override fun toString(): String = value
}

/**
 * Protocol for port mappings.
 */
enum class Protocol(val value: String) {
    TCP("tcp"),
    UDP("udp")
}

/**
 * Restart policy for containers.
 */
sealed class RestartPolicy(val value: String) {
    data object No : RestartPolicy("no")
    data object Always : RestartPolicy("always")
    data object UnlessStopped : RestartPolicy("unless-stopped")
    class OnFailure(val maxRetries: Int? = null) : RestartPolicy(
        if (maxRetries != null) "on-failure:$maxRetries" else "on-failure"
    )
}

/**
 * Command to run a container.
 *
 * Equivalent to `docker run`.
 *
 * Example usage (Kotlin DSL):
 * ```kotlin
 * val containerId = RunCommand("nginx:alpine")
 *     .name("web-server")
 *     .port(8080, 80)
 *     .env("WORKER_PROCESSES", "4")
 *     .detach()
 *     .rm()
 *     .execute()
 * ```
 *
 * Example usage (Java Builder):
 * ```java
 * ContainerId containerId = RunCommand.builder("nginx:alpine")
 *     .name("web-server")
 *     .port(8080, 80)
 *     .env("WORKER_PROCESSES", "4")
 *     .detach()
 *     .rm()
 *     .executeBlocking();
 * ```
 */
class RunCommand(
    private val image: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<ContainerId>(executor) {

    private var containerName: String? = null
    private val ports = mutableListOf<String>()
    private val envVars = mutableMapOf<String, String>()
    private val volumes = mutableListOf<String>()
    private val labels = mutableMapOf<String, String>()
    private var detached = false
    private var autoRemove = false
    private var interactive = false
    private var tty = false
    private var privileged = false
    private var user: String? = null
    private var workdir: String? = null
    private var network: String? = null
    private val networkAliases = mutableListOf<String>()
    private var hostname: String? = null
    private var memory: String? = null
    private var memorySwap: String? = null
    private var cpus: String? = null
    private var cpuShares: Int? = null
    private var restartPolicy: RestartPolicy? = null
    private var entrypoint: String? = null
    private val command = mutableListOf<String>()
    private var init = false
    private var platform: String? = null

    /** Set the container name. */
    fun name(name: String) = apply { containerName = name }

    /** Map a host port to a container port (TCP). */
    fun port(hostPort: Int, containerPort: Int) = apply {
        ports.add("$hostPort:$containerPort")
    }

    /** Map a host port to a container port with protocol. */
    fun port(hostPort: Int, containerPort: Int, protocol: Protocol) = apply {
        ports.add("$hostPort:$containerPort/${protocol.value}")
    }

    /** Expose a container port with dynamic host port allocation. */
    fun dynamicPort(containerPort: Int) = apply {
        ports.add("$containerPort")
    }

    /** Set an environment variable. */
    fun env(key: String, value: String) = apply { envVars[key] = value }

    /** Set multiple environment variables. */
    fun env(vars: Map<String, String>) = apply { envVars.putAll(vars) }

    /** Mount a volume. */
    fun volume(hostPath: String, containerPath: String) = apply {
        volumes.add("$hostPath:$containerPath")
    }

    /** Mount a volume with options (e.g., "ro" for read-only). */
    fun volume(hostPath: String, containerPath: String, options: String) = apply {
        volumes.add("$hostPath:$containerPath:$options")
    }

    /** Mount a named volume. */
    fun namedVolume(volumeName: String, containerPath: String) = apply {
        volumes.add("$volumeName:$containerPath")
    }

    /** Add a label. */
    fun label(key: String, value: String) = apply { labels[key] = value }

    /** Add multiple labels. */
    fun labels(labels: Map<String, String>) = apply { this.labels.putAll(labels) }

    /** Run in detached mode (background). */
    fun detach() = apply { detached = true }

    /** Automatically remove the container when it exits. */
    fun rm() = apply { autoRemove = true }

    /** Keep STDIN open. */
    fun interactive() = apply { interactive = true }

    /** Allocate a pseudo-TTY. */
    fun tty() = apply { this.tty = true }

    /** Run in privileged mode. */
    fun privileged() = apply { this.privileged = true }

    /** Set the user to run as. */
    fun user(user: String) = apply { this.user = user }

    /** Set the working directory. */
    fun workdir(workdir: String) = apply { this.workdir = workdir }

    /** Connect to a network. */
    fun network(network: String) = apply { this.network = network }

    /** Add a network alias. */
    fun networkAlias(alias: String) = apply { networkAliases.add(alias) }

    /** Set the container hostname. */
    fun hostname(hostname: String) = apply { this.hostname = hostname }

    /** Set memory limit (e.g., "512m", "1g"). */
    fun memory(memory: String) = apply { this.memory = memory }

    /** Set memory+swap limit (e.g., "1g"). */
    fun memorySwap(memorySwap: String) = apply { this.memorySwap = memorySwap }

    /** Set CPU limit (e.g., "0.5", "2"). */
    fun cpus(cpus: String) = apply { this.cpus = cpus }

    /** Set CPU shares (relative weight). */
    fun cpuShares(shares: Int) = apply { this.cpuShares = shares }

    /** Set restart policy. */
    fun restart(policy: RestartPolicy) = apply { restartPolicy = policy }

    /** Override the entrypoint. */
    fun entrypoint(entrypoint: String) = apply { this.entrypoint = entrypoint }

    /** Set the command to run. */
    fun command(vararg cmd: String) = apply { command.addAll(cmd) }

    /** Use init as PID 1. */
    fun init() = apply { this.init = true }

    /** Set the platform (e.g., "linux/amd64"). */
    fun platform(platform: String) = apply { this.platform = platform }

    override fun buildArgs(): List<String> = buildList {
        add("run")

        containerName?.let { add("--name"); add(it) }
        ports.forEach { add("--publish"); add(it) }
        envVars.forEach { (k, v) -> add("--env"); add("$k=$v") }
        volumes.forEach { add("--volume"); add(it) }
        labels.forEach { (k, v) -> add("--label"); add("$k=$v") }

        if (detached) add("--detach")
        if (autoRemove) add("--rm")
        if (interactive) add("--interactive")
        if (tty) add("--tty")
        if (privileged) add("--privileged")
        if (init) add("--init")

        user?.let { add("--user"); add(it) }
        workdir?.let { add("--workdir"); add(it) }
        network?.let { add("--network"); add(it) }
        networkAliases.forEach { add("--network-alias"); add(it) }
        hostname?.let { add("--hostname"); add(it) }
        memory?.let { add("--memory"); add(it) }
        memorySwap?.let { add("--memory-swap"); add(it) }
        cpus?.let { add("--cpus"); add(it) }
        cpuShares?.let { add("--cpu-shares"); add(it.toString()) }
        restartPolicy?.let { add("--restart"); add(it.value) }
        entrypoint?.let { add("--entrypoint"); add(it) }
        platform?.let { add("--platform"); add(it) }

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
        /**
         * Create a new RunCommand builder (for Java interop).
         */
        @JvmStatic
        fun builder(image: String): RunCommand = RunCommand(image)
    }
}
