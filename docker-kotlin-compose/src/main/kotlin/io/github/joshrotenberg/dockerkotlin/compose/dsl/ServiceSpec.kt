package io.github.joshrotenberg.dockerkotlin.compose.dsl

import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Specification for a Docker Compose service.
 */
class ServiceSpec {
    /** Image to use for the service. */
    var image: String? = null

    /** Container name. */
    var containerName: String? = null

    /** Build configuration. */
    private var buildSpec: BuildSpec? = null

    /** Command to run. */
    private var command: Any? = null

    /** Entrypoint override. */
    private var entrypoint: Any? = null

    /** Environment variables. */
    private val environment = mutableMapOf<String, String>()

    /** Environment files. */
    private val envFiles = mutableListOf<String>()

    /** Port mappings. */
    private val portMappings = mutableListOf<String>()

    /** Exposed ports (internal only). */
    private val exposedPorts = mutableListOf<String>()

    /** Volume mounts. */
    private val volumeMounts = mutableListOf<Any>()

    /** Network connections. */
    private val networkConnections = mutableListOf<Any>()

    /** Service dependencies. */
    private var dependsOnList: Any? = null

    /** Restart policy. */
    var restart: String? = null

    /** Hostname. */
    var hostname: String? = null

    /** Domain name. */
    var domainname: String? = null

    /** User to run as. */
    var user: String? = null

    /** Working directory. */
    var workingDir: String? = null

    /** Privileged mode. */
    var privileged: Boolean? = null

    /** Read-only root filesystem. */
    var readOnly: Boolean? = null

    /** Stdin open. */
    var stdinOpen: Boolean? = null

    /** Allocate TTY. */
    var tty: Boolean? = null

    /** Init process. */
    var init: Boolean? = null

    /** Stop signal. */
    var stopSignal: String? = null

    /** Stop grace period. */
    var stopGracePeriod: String? = null

    /** Labels. */
    private val labelMap = mutableMapOf<String, String>()

    /** Logging configuration. */
    private var loggingSpec: LoggingSpec? = null

    /** Healthcheck configuration. */
    private var healthcheckSpec: HealthcheckSpec? = null

    /** Deploy configuration. */
    private var deploySpec: DeploySpec? = null

    /** Resource limits. */
    private var resourcesSpec: ResourcesSpec? = null

    /** Ulimits. */
    private val ulimitsMap = mutableMapOf<String, Any>()

    /** Sysctls. */
    private val sysctlsMap = mutableMapOf<String, String>()

    /** Capabilities to add. */
    private val capAddList = mutableListOf<String>()

    /** Capabilities to drop. */
    private val capDropList = mutableListOf<String>()

    /** Security options. */
    private val securityOptList = mutableListOf<String>()

    /** DNS servers. */
    private val dnsServers = mutableListOf<String>()

    /** DNS search domains. */
    private val dnsSearchDomains = mutableListOf<String>()

    /** Extra hosts. */
    private val extraHostsList = mutableListOf<String>()

    /** Devices. */
    private val devicesList = mutableListOf<String>()

    /** Tmpfs mounts. */
    private val tmpfsList = mutableListOf<Any>()

    /** Shared memory size. */
    var shmSize: String? = null

    /** PID mode. */
    var pid: String? = null

    /** IPC mode. */
    var ipc: String? = null

    /** Network mode. */
    var networkMode: String? = null

    /** Platform. */
    var platform: String? = null

    /** Profiles. */
    private val profilesList = mutableListOf<String>()

    /** Pull policy. */
    var pullPolicy: String? = null

    /** Secrets. */
    private val secretsList = mutableListOf<Any>()

    /** Configs. */
    private val configsList = mutableListOf<Any>()

    // === DSL Methods ===

    /**
     * Configure build options.
     */
    fun build(context: String, init: BuildSpec.() -> Unit = {}) {
        buildSpec = BuildSpec(context).apply(init)
    }

    /**
     * Set command as a single string.
     */
    fun command(cmd: String) {
        command = cmd
    }

    /**
     * Set command as a list.
     */
    fun command(vararg args: String) {
        command = args.toList()
    }

    /**
     * Set entrypoint as a single string.
     */
    fun entrypoint(ep: String) {
        entrypoint = ep
    }

    /**
     * Set entrypoint as a list.
     */
    fun entrypoint(vararg args: String) {
        entrypoint = args.toList()
    }

    /**
     * Add environment variable.
     */
    fun environment(key: String, value: String) {
        environment[key] = value
    }

    /**
     * Add multiple environment variables.
     */
    fun environment(vararg pairs: Pair<String, String>) {
        pairs.forEach { (k, v) -> environment[k] = v }
    }

    /**
     * Configure environment variables using a block.
     */
    fun environment(init: EnvironmentBuilder.() -> Unit) {
        EnvironmentBuilder(environment).apply(init)
    }

    /**
     * Add environment file.
     */
    fun envFile(file: String) {
        envFiles.add(file)
    }

    /**
     * Add environment files.
     */
    fun envFile(vararg files: String) {
        envFiles.addAll(files)
    }

    /**
     * Add port mapping (short syntax: "8080:80").
     */
    fun ports(vararg mappings: String) {
        portMappings.addAll(mappings)
    }

    /**
     * Add port mapping with explicit host and container ports.
     */
    fun port(hostPort: Int, containerPort: Int, protocol: String? = null) {
        val mapping = if (protocol != null) {
            "$hostPort:$containerPort/$protocol"
        } else {
            "$hostPort:$containerPort"
        }
        portMappings.add(mapping)
    }

    /**
     * Expose port (internal only).
     */
    fun expose(vararg ports: String) {
        exposedPorts.addAll(ports)
    }

    /**
     * Expose port (internal only).
     */
    fun expose(vararg ports: Int) {
        ports.forEach { exposedPorts.add(it.toString()) }
    }

    /**
     * Add volume mount (short syntax).
     */
    fun volumes(vararg mounts: String) {
        volumeMounts.addAll(mounts)
    }

    /**
     * Add volume mount with explicit paths.
     */
    fun volume(source: String, target: String, mode: String? = null) {
        val mount = if (mode != null) "$source:$target:$mode" else "$source:$target"
        volumeMounts.add(mount)
    }

    /**
     * Add named volume with long syntax.
     */
    fun volume(init: VolumeMount.() -> Unit) {
        volumeMounts.add(VolumeMount().apply(init).toMap())
    }

    /**
     * Connect to networks.
     */
    fun networks(vararg names: String) {
        networkConnections.addAll(names)
    }

    /**
     * Connect to network with options.
     */
    fun network(name: String, init: NetworkConnection.() -> Unit = {}) {
        val conn = NetworkConnection().apply(init)
        val map = conn.toMap()
        if (map.isEmpty()) {
            networkConnections.add(name)
        } else {
            networkConnections.add(mapOf(name to map))
        }
    }

    /**
     * Add service dependencies (simple form).
     */
    fun dependsOn(vararg services: String) {
        dependsOnList = services.toList()
    }

    /**
     * Add service dependencies with conditions.
     */
    fun dependsOn(init: DependsOnBuilder.() -> Unit) {
        val builder = DependsOnBuilder()
        builder.init()
        dependsOnList = builder.toMap()
    }

    /**
     * Add labels.
     */
    fun labels(vararg pairs: Pair<String, String>) {
        pairs.forEach { (k, v) -> labelMap[k] = v }
    }

    /**
     * Add label.
     */
    fun label(key: String, value: String) {
        labelMap[key] = value
    }

    /**
     * Configure logging.
     */
    fun logging(driver: String, init: LoggingSpec.() -> Unit = {}) {
        loggingSpec = LoggingSpec(driver).apply(init)
    }

    /**
     * Configure healthcheck.
     */
    fun healthcheck(init: HealthcheckSpec.() -> Unit) {
        healthcheckSpec = HealthcheckSpec().apply(init)
    }

    /**
     * Disable healthcheck.
     */
    fun healthcheckDisable() {
        healthcheckSpec = HealthcheckSpec().apply { disable = true }
    }

    /**
     * Configure deploy options.
     */
    fun deploy(init: DeploySpec.() -> Unit) {
        deploySpec = DeploySpec().apply(init)
    }

    /**
     * Add ulimit.
     */
    fun ulimit(name: String, soft: Int, hard: Int = soft) {
        ulimitsMap[name] = if (soft == hard) soft else mapOf("soft" to soft, "hard" to hard)
    }

    /**
     * Add sysctl.
     */
    fun sysctl(key: String, value: String) {
        sysctlsMap[key] = value
    }

    /**
     * Add capability.
     */
    fun capAdd(vararg caps: String) {
        capAddList.addAll(caps)
    }

    /**
     * Drop capability.
     */
    fun capDrop(vararg caps: String) {
        capDropList.addAll(caps)
    }

    /**
     * Add security option.
     */
    fun securityOpt(vararg opts: String) {
        securityOptList.addAll(opts)
    }

    /**
     * Add DNS server.
     */
    fun dns(vararg servers: String) {
        dnsServers.addAll(servers)
    }

    /**
     * Add DNS search domain.
     */
    fun dnsSearch(vararg domains: String) {
        dnsSearchDomains.addAll(domains)
    }

    /**
     * Add extra host entry.
     */
    fun extraHosts(vararg hosts: String) {
        extraHostsList.addAll(hosts)
    }

    /**
     * Add extra host entry.
     */
    fun extraHost(hostname: String, ip: String) {
        extraHostsList.add("$hostname:$ip")
    }

    /**
     * Add device.
     */
    fun devices(vararg devs: String) {
        devicesList.addAll(devs)
    }

    /**
     * Add tmpfs mount.
     */
    fun tmpfs(vararg paths: String) {
        tmpfsList.addAll(paths)
    }

    /**
     * Add profile.
     */
    fun profiles(vararg names: String) {
        profilesList.addAll(names)
    }

    /**
     * Add secret reference.
     */
    fun secret(name: String) {
        secretsList.add(name)
    }

    /**
     * Add secret with options.
     */
    fun secret(name: String, init: SecretReference.() -> Unit) {
        secretsList.add(SecretReference(name).apply(init).toMap())
    }

    /**
     * Add config reference.
     */
    fun config(name: String) {
        configsList.add(name)
    }

    /**
     * Add config with options.
     */
    fun config(name: String, init: ConfigReference.() -> Unit) {
        configsList.add(ConfigReference(name).apply(init).toMap())
    }

    /**
     * Convert to a Map for YAML serialization.
     */
    fun toMap(): Map<String, Any> = buildMap {
        image?.let { put("image", it) }
        containerName?.let { put("container_name", it) }
        buildSpec?.let { put("build", it.toMap()) }
        command?.let { put("command", it) }
        entrypoint?.let { put("entrypoint", it) }

        if (environment.isNotEmpty()) {
            put("environment", environment.toMap())
        }

        if (envFiles.isNotEmpty()) {
            put("env_file", envFiles.toList())
        }

        if (portMappings.isNotEmpty()) {
            put("ports", portMappings.toList())
        }

        if (exposedPorts.isNotEmpty()) {
            put("expose", exposedPorts.toList())
        }

        if (volumeMounts.isNotEmpty()) {
            put("volumes", volumeMounts.toList())
        }

        if (networkConnections.isNotEmpty()) {
            // Handle mixed format (strings and maps)
            val simplified = networkConnections.map { conn ->
                when (conn) {
                    is String -> conn
                    is Map<*, *> -> conn
                    else -> conn.toString()
                }
            }
            put("networks", simplified)
        }

        dependsOnList?.let { put("depends_on", it) }
        restart?.let { put("restart", it) }
        hostname?.let { put("hostname", it) }
        domainname?.let { put("domainname", it) }
        user?.let { put("user", it) }
        workingDir?.let { put("working_dir", it) }
        privileged?.let { put("privileged", it) }
        readOnly?.let { put("read_only", it) }
        stdinOpen?.let { put("stdin_open", it) }
        tty?.let { put("tty", it) }
        init?.let { put("init", it) }
        stopSignal?.let { put("stop_signal", it) }
        stopGracePeriod?.let { put("stop_grace_period", it) }

        if (labelMap.isNotEmpty()) {
            put("labels", labelMap.toMap())
        }

        loggingSpec?.let { put("logging", it.toMap()) }
        healthcheckSpec?.let { put("healthcheck", it.toMap()) }
        deploySpec?.let { put("deploy", it.toMap()) }

        if (ulimitsMap.isNotEmpty()) {
            put("ulimits", ulimitsMap.toMap())
        }

        if (sysctlsMap.isNotEmpty()) {
            put("sysctls", sysctlsMap.toMap())
        }

        if (capAddList.isNotEmpty()) {
            put("cap_add", capAddList.toList())
        }

        if (capDropList.isNotEmpty()) {
            put("cap_drop", capDropList.toList())
        }

        if (securityOptList.isNotEmpty()) {
            put("security_opt", securityOptList.toList())
        }

        if (dnsServers.isNotEmpty()) {
            put("dns", dnsServers.toList())
        }

        if (dnsSearchDomains.isNotEmpty()) {
            put("dns_search", dnsSearchDomains.toList())
        }

        if (extraHostsList.isNotEmpty()) {
            put("extra_hosts", extraHostsList.toList())
        }

        if (devicesList.isNotEmpty()) {
            put("devices", devicesList.toList())
        }

        if (tmpfsList.isNotEmpty()) {
            put("tmpfs", tmpfsList.toList())
        }

        shmSize?.let { put("shm_size", it) }
        pid?.let { put("pid", it) }
        ipc?.let { put("ipc", it) }
        networkMode?.let { put("network_mode", it) }
        platform?.let { put("platform", it) }

        if (profilesList.isNotEmpty()) {
            put("profiles", profilesList.toList())
        }

        pullPolicy?.let { put("pull_policy", it) }

        if (secretsList.isNotEmpty()) {
            put("secrets", secretsList.toList())
        }

        if (configsList.isNotEmpty()) {
            put("configs", configsList.toList())
        }
    }
}

/**
 * Builder for environment variables.
 */
class EnvironmentBuilder(private val env: MutableMap<String, String>) {
    infix fun String.to(value: String) {
        env[this] = value
    }

    infix fun String.to(value: Int) {
        env[this] = value.toString()
    }

    infix fun String.to(value: Boolean) {
        env[this] = value.toString()
    }
}

/**
 * Builder for depends_on with conditions.
 */
class DependsOnBuilder {
    private val deps = mutableMapOf<String, Map<String, String>>()

    fun service(name: String, condition: String = "service_started") {
        deps[name] = mapOf("condition" to condition)
    }

    fun serviceHealthy(name: String) = service(name, "service_healthy")
    fun serviceStarted(name: String) = service(name, "service_started")
    fun serviceCompletedSuccessfully(name: String) = service(name, "service_completed_successfully")

    fun toMap(): Map<String, Map<String, String>> = deps.toMap()
}
