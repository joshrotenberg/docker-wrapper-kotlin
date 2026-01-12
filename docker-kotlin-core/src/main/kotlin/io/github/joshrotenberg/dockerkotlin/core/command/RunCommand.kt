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

    // High priority options
    private val capsAdd = mutableListOf<String>()
    private val capsDrop = mutableListOf<String>()
    private val mounts = mutableListOf<String>()
    private var healthCmd: String? = null
    private var healthInterval: String? = null
    private var healthTimeout: String? = null
    private var healthRetries: Int? = null
    private var healthStartPeriod: String? = null
    private var noHealthcheck = false

    // Medium priority options
    private val addHosts = mutableListOf<String>()
    private val devices = mutableListOf<String>()
    private val dnsServers = mutableListOf<String>()
    private val dnsSearch = mutableListOf<String>()
    private var gpus: String? = null
    private var logDriver: String? = null
    private val logOpts = mutableMapOf<String, String>()
    private var readOnly = false
    private val securityOpts = mutableListOf<String>()
    private val tmpfs = mutableListOf<String>()
    private val ulimits = mutableListOf<String>()

    // Low priority options
    private var shmSize: String? = null
    private var ipc: String? = null
    private var pid: String? = null
    private var uts: String? = null
    private var stopSignal: String? = null
    private var stopTimeout: Int? = null
    private val sysctls = mutableMapOf<String, String>()
    private var blkioWeight: Int? = null
    private var cgroupParent: String? = null
    private var cgroupns: String? = null
    private var isolation: String? = null
    private var macAddress: String? = null
    private var oomKillDisable = false
    private var oomScoreAdj: Int? = null
    private var userns: String? = null

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

    // High priority options

    /** Add a Linux capability. */
    fun capAdd(capability: String) = apply { capsAdd.add(capability) }

    /** Drop a Linux capability. */
    fun capDrop(capability: String) = apply { capsDrop.add(capability) }

    /** Attach a filesystem mount (more flexible than --volume). */
    fun mount(mount: String) = apply { mounts.add(mount) }

    /** Attach a bind mount. */
    fun mountBind(source: String, target: String, readOnly: Boolean = false) = apply {
        val ro = if (readOnly) ",readonly" else ""
        mounts.add("type=bind,source=$source,target=$target$ro")
    }

    /** Attach a volume mount. */
    fun mountVolume(source: String, target: String, readOnly: Boolean = false) = apply {
        val ro = if (readOnly) ",readonly" else ""
        mounts.add("type=volume,source=$source,target=$target$ro")
    }

    /** Attach a tmpfs mount. */
    fun mountTmpfs(target: String, size: String? = null) = apply {
        val sizeOpt = size?.let { ",tmpfs-size=$it" } ?: ""
        mounts.add("type=tmpfs,target=$target$sizeOpt")
    }

    /** Set health check command. */
    fun healthCmd(cmd: String) = apply { healthCmd = cmd }

    /** Set health check interval. */
    fun healthInterval(interval: String) = apply { healthInterval = interval }

    /** Set health check timeout. */
    fun healthTimeout(timeout: String) = apply { healthTimeout = timeout }

    /** Set health check retries. */
    fun healthRetries(retries: Int) = apply { healthRetries = retries }

    /** Set health check start period. */
    fun healthStartPeriod(period: String) = apply { healthStartPeriod = period }

    /** Disable health check. */
    fun noHealthcheck() = apply { noHealthcheck = true }

    // Medium priority options

    /** Add a custom host-to-IP mapping (host:ip). */
    fun addHost(host: String, ip: String) = apply { addHosts.add("$host:$ip") }

    /** Add a host device to the container. */
    fun device(device: String) = apply { devices.add(device) }

    /** Add a host device with container path. */
    fun device(hostDevice: String, containerDevice: String) = apply {
        devices.add("$hostDevice:$containerDevice")
    }

    /** Set custom DNS servers. */
    fun dns(server: String) = apply { dnsServers.add(server) }

    /** Set DNS search domains. */
    fun dnsSearch(domain: String) = apply { dnsSearch.add(domain) }

    /** Request GPU access (e.g., "all", "device=0"). */
    fun gpus(gpus: String) = apply { this.gpus = gpus }

    /** Set the logging driver. */
    fun logDriver(driver: String) = apply { logDriver = driver }

    /** Set a logging driver option. */
    fun logOpt(key: String, value: String) = apply { logOpts[key] = value }

    /** Mount root filesystem as read-only. */
    fun readOnly() = apply { readOnly = true }

    /** Add a security option. */
    fun securityOpt(opt: String) = apply { securityOpts.add(opt) }

    /** Mount a tmpfs directory. */
    fun tmpfs(path: String) = apply { tmpfs.add(path) }

    /** Mount a tmpfs directory with options. */
    fun tmpfs(path: String, options: String) = apply { tmpfs.add("$path:$options") }

    /** Set ulimit options. */
    fun ulimit(name: String, soft: Long, hard: Long = soft) = apply {
        ulimits.add("$name=$soft:$hard")
    }

    // Low priority options

    /** Set shared memory size. */
    fun shmSize(size: String) = apply { shmSize = size }

    /** Set IPC namespace mode. */
    fun ipc(mode: String) = apply { ipc = mode }

    /** Set PID namespace mode. */
    fun pid(mode: String) = apply { pid = mode }

    /** Set UTS namespace mode. */
    fun uts(mode: String) = apply { uts = mode }

    /** Set stop signal. */
    fun stopSignal(signal: String) = apply { stopSignal = signal }

    /** Set stop timeout in seconds. */
    fun stopTimeout(seconds: Int) = apply { stopTimeout = seconds }

    /** Set a sysctl option. */
    fun sysctl(key: String, value: String) = apply { sysctls[key] = value }

    /** Set block I/O weight (10-1000). */
    fun blkioWeight(weight: Int) = apply { blkioWeight = weight }

    /** Set cgroup parent. */
    fun cgroupParent(parent: String) = apply { cgroupParent = parent }

    /** Set cgroup namespace mode. */
    fun cgroupns(mode: String) = apply { cgroupns = mode }

    /** Set container isolation technology. */
    fun isolation(isolation: String) = apply { this.isolation = isolation }

    /** Set container MAC address. */
    fun macAddress(address: String) = apply { macAddress = address }

    /** Disable OOM killer. */
    fun oomKillDisable() = apply { oomKillDisable = true }

    /** Tune container OOM preferences (-1000 to 1000). */
    fun oomScoreAdj(adj: Int) = apply { oomScoreAdj = adj }

    /** Set user namespace mode. */
    fun userns(mode: String) = apply { userns = mode }

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

        // High priority options
        capsAdd.forEach { add("--cap-add"); add(it) }
        capsDrop.forEach { add("--cap-drop"); add(it) }
        mounts.forEach { add("--mount"); add(it) }
        healthCmd?.let { add("--health-cmd"); add(it) }
        healthInterval?.let { add("--health-interval"); add(it) }
        healthTimeout?.let { add("--health-timeout"); add(it) }
        healthRetries?.let { add("--health-retries"); add(it.toString()) }
        healthStartPeriod?.let { add("--health-start-period"); add(it) }
        if (noHealthcheck) add("--no-healthcheck")

        // Medium priority options
        addHosts.forEach { add("--add-host"); add(it) }
        devices.forEach { add("--device"); add(it) }
        dnsServers.forEach { add("--dns"); add(it) }
        dnsSearch.forEach { add("--dns-search"); add(it) }
        gpus?.let { add("--gpus"); add(it) }
        logDriver?.let { add("--log-driver"); add(it) }
        logOpts.forEach { (k, v) -> add("--log-opt"); add("$k=$v") }
        if (readOnly) add("--read-only")
        securityOpts.forEach { add("--security-opt"); add(it) }
        tmpfs.forEach { add("--tmpfs"); add(it) }
        ulimits.forEach { add("--ulimit"); add(it) }

        // Low priority options
        shmSize?.let { add("--shm-size"); add(it) }
        ipc?.let { add("--ipc"); add(it) }
        pid?.let { add("--pid"); add(it) }
        uts?.let { add("--uts"); add(it) }
        stopSignal?.let { add("--stop-signal"); add(it) }
        stopTimeout?.let { add("--stop-timeout"); add(it.toString()) }
        sysctls.forEach { (k, v) -> add("--sysctl"); add("$k=$v") }
        blkioWeight?.let { add("--blkio-weight"); add(it.toString()) }
        cgroupParent?.let { add("--cgroup-parent"); add(it) }
        cgroupns?.let { add("--cgroupns"); add(it) }
        isolation?.let { add("--isolation"); add(it) }
        macAddress?.let { add("--mac-address"); add(it) }
        if (oomKillDisable) add("--oom-kill-disable")
        oomScoreAdj?.let { add("--oom-score-adj"); add(it.toString()) }
        userns?.let { add("--userns"); add(it) }

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
