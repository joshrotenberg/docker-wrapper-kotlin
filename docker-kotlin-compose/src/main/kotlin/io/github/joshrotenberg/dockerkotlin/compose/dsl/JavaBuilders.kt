@file:JvmName("ComposeBuilders")

package io.github.joshrotenberg.dockerkotlin.compose.dsl

import java.util.function.Consumer

/**
 * Java-friendly builder for Docker Compose specifications.
 *
 * Example usage:
 * ```java
 * ComposeSpec compose = ComposeBuilder.create()
 *     .name("myapp")
 *     .service("web", web -> web
 *         .image("nginx:alpine")
 *         .ports("8080:80")
 *         .environment("NODE_ENV", "production")
 *         .dependsOn("redis", "db"))
 *     .service("redis", redis -> redis
 *         .image("redis:7-alpine"))
 *     .service("db", db -> db
 *         .image("postgres:16")
 *         .environment("POSTGRES_PASSWORD", "secret")
 *         .volume("pgdata", "/var/lib/postgresql/data"))
 *     .network("backend", net -> net.driver("bridge"))
 *     .volume("pgdata")
 *     .build();
 *
 * String yaml = compose.toYaml();
 * ```
 */
class ComposeBuilder private constructor() {
    private val spec = ComposeSpec()

    /** Set the compose project name. */
    fun name(name: String): ComposeBuilder {
        spec.name = name
        return this
    }

    /** Set the compose file version. */
    fun version(version: String): ComposeBuilder {
        spec.version = version
        return this
    }

    /** Add a service. */
    fun service(name: String, configure: Consumer<ServiceBuilder>): ComposeBuilder {
        val builder = ServiceBuilder()
        configure.accept(builder)
        spec.services[name] = builder.build()
        return this
    }

    /** Add a network with default configuration. */
    fun network(name: String): ComposeBuilder {
        spec.networks[name] = NetworkSpec()
        return this
    }

    /** Add a network with configuration. */
    fun network(name: String, configure: Consumer<NetworkBuilder>): ComposeBuilder {
        val builder = NetworkBuilder()
        configure.accept(builder)
        spec.networks[name] = builder.build()
        return this
    }

    /** Add a volume with default configuration. */
    fun volume(name: String): ComposeBuilder {
        spec.volumes[name] = VolumeSpec()
        return this
    }

    /** Add a volume with configuration. */
    fun volume(name: String, configure: Consumer<VolumeBuilder>): ComposeBuilder {
        val builder = VolumeBuilder()
        configure.accept(builder)
        spec.volumes[name] = builder.build()
        return this
    }

    /** Add a secret. */
    fun secret(name: String, configure: Consumer<SecretBuilder>): ComposeBuilder {
        val builder = SecretBuilder()
        configure.accept(builder)
        spec.secrets[name] = builder.build()
        return this
    }

    /** Add a config. */
    fun config(name: String, configure: Consumer<ConfigBuilder>): ComposeBuilder {
        val builder = ConfigBuilder()
        configure.accept(builder)
        spec.configs[name] = builder.build()
        return this
    }

    /** Build the ComposeSpec. */
    fun build(): ComposeSpec = spec

    companion object {
        /** Create a new ComposeBuilder. */
        @JvmStatic
        fun create(): ComposeBuilder = ComposeBuilder()
    }
}

/**
 * Java-friendly builder for service specifications.
 */
class ServiceBuilder internal constructor() {
    private val spec = ServiceSpec()

    /** Set the image. */
    fun image(image: String): ServiceBuilder {
        spec.image = image
        return this
    }

    /** Set the container name. */
    fun containerName(name: String): ServiceBuilder {
        spec.containerName = name
        return this
    }

    /** Configure build options. */
    fun build(context: String): ServiceBuilder {
        spec.build(context) {}
        return this
    }

    /** Configure build options. */
    fun build(context: String, configure: Consumer<BuildSpecBuilder>): ServiceBuilder {
        val builder = BuildSpecBuilder(context)
        configure.accept(builder)
        // Access the internal buildSpec through reflection or direct assignment
        spec.build(context) {
            dockerfile = builder.dockerfile
            target = builder.target
            builder.args.forEach { (k, v) -> arg(k, v) }
        }
        return this
    }

    /** Set the command. */
    fun command(vararg args: String): ServiceBuilder {
        spec.command(*args)
        return this
    }

    /** Set the entrypoint. */
    fun entrypoint(vararg args: String): ServiceBuilder {
        spec.entrypoint(*args)
        return this
    }

    /** Add an environment variable. */
    fun environment(key: String, value: String): ServiceBuilder {
        spec.environment(key, value)
        return this
    }

    /** Add environment variables. */
    fun environment(env: Map<String, String>): ServiceBuilder {
        env.forEach { (k, v) -> spec.environment(k, v) }
        return this
    }

    /** Add an environment file. */
    fun envFile(file: String): ServiceBuilder {
        spec.envFile(file)
        return this
    }

    /** Add port mappings. */
    fun ports(vararg mappings: String): ServiceBuilder {
        spec.ports(*mappings)
        return this
    }

    /** Add a port mapping. */
    fun port(hostPort: Int, containerPort: Int): ServiceBuilder {
        spec.port(hostPort, containerPort)
        return this
    }

    /** Expose internal ports. */
    fun expose(vararg ports: Int): ServiceBuilder {
        spec.expose(*ports)
        return this
    }

    /** Add volume mounts. */
    fun volumes(vararg mounts: String): ServiceBuilder {
        spec.volumes(*mounts)
        return this
    }

    /** Add a volume mount. */
    fun volume(source: String, target: String): ServiceBuilder {
        spec.volume(source, target)
        return this
    }

    /** Add a volume mount with mode. */
    fun volume(source: String, target: String, mode: String): ServiceBuilder {
        spec.volume(source, target, mode)
        return this
    }

    /** Connect to networks. */
    fun networks(vararg names: String): ServiceBuilder {
        spec.networks(*names)
        return this
    }

    /** Add service dependencies. */
    fun dependsOn(vararg services: String): ServiceBuilder {
        spec.dependsOn(*services)
        return this
    }

    /** Set restart policy. */
    fun restart(policy: String): ServiceBuilder {
        spec.restart = policy
        return this
    }

    /** Set hostname. */
    fun hostname(hostname: String): ServiceBuilder {
        spec.hostname = hostname
        return this
    }

    /** Set user. */
    fun user(user: String): ServiceBuilder {
        spec.user = user
        return this
    }

    /** Set working directory. */
    fun workingDir(dir: String): ServiceBuilder {
        spec.workingDir = dir
        return this
    }

    /** Enable privileged mode. */
    fun privileged(privileged: Boolean): ServiceBuilder {
        spec.privileged = privileged
        return this
    }

    /** Set read-only root filesystem. */
    fun readOnly(readOnly: Boolean): ServiceBuilder {
        spec.readOnly = readOnly
        return this
    }

    /** Allocate TTY. */
    fun tty(tty: Boolean): ServiceBuilder {
        spec.tty = tty
        return this
    }

    /** Keep stdin open. */
    fun stdinOpen(stdinOpen: Boolean): ServiceBuilder {
        spec.stdinOpen = stdinOpen
        return this
    }

    /** Enable init process. */
    fun init(init: Boolean): ServiceBuilder {
        spec.init = init
        return this
    }

    /** Set stop signal. */
    fun stopSignal(signal: String): ServiceBuilder {
        spec.stopSignal = signal
        return this
    }

    /** Set stop grace period. */
    fun stopGracePeriod(period: String): ServiceBuilder {
        spec.stopGracePeriod = period
        return this
    }

    /** Add a label. */
    fun label(key: String, value: String): ServiceBuilder {
        spec.label(key, value)
        return this
    }

    /** Configure logging. */
    fun logging(driver: String, configure: Consumer<LoggingBuilder>): ServiceBuilder {
        val builder = LoggingBuilder(driver)
        configure.accept(builder)
        spec.logging(driver) {
            builder.options.forEach { (k, v) -> option(k, v) }
        }
        return this
    }

    /** Configure healthcheck. */
    fun healthcheck(configure: Consumer<HealthcheckBuilder>): ServiceBuilder {
        val builder = HealthcheckBuilder()
        configure.accept(builder)
        spec.healthcheck {
            builder.test?.let { test(it) }
            builder.testCmd?.let { testCmd(*it.toTypedArray()) }
            builder.testShell?.let { testShell(it) }
            interval = builder.interval
            timeout = builder.timeout
            retries = builder.retries
            startPeriod = builder.startPeriod
        }
        return this
    }

    /** Disable healthcheck. */
    fun healthcheckDisable(): ServiceBuilder {
        spec.healthcheckDisable()
        return this
    }

    /** Configure deploy options. */
    fun deploy(configure: Consumer<DeployBuilder>): ServiceBuilder {
        val builder = DeployBuilder()
        configure.accept(builder)
        spec.deploy {
            replicas = builder.replicas
            mode = builder.mode
            builder.resourceLimits?.let { limits ->
                resources {
                    limits {
                        cpus = limits.cpus
                        memory = limits.memory
                    }
                }
            }
        }
        return this
    }

    /** Add capability. */
    fun capAdd(vararg caps: String): ServiceBuilder {
        spec.capAdd(*caps)
        return this
    }

    /** Drop capability. */
    fun capDrop(vararg caps: String): ServiceBuilder {
        spec.capDrop(*caps)
        return this
    }

    /** Set platform. */
    fun platform(platform: String): ServiceBuilder {
        spec.platform = platform
        return this
    }

    /** Set pull policy. */
    fun pullPolicy(policy: String): ServiceBuilder {
        spec.pullPolicy = policy
        return this
    }

    /** Set shared memory size. */
    fun shmSize(size: String): ServiceBuilder {
        spec.shmSize = size
        return this
    }

    internal fun build(): ServiceSpec = spec
}

/**
 * Java-friendly builder for network specifications.
 */
class NetworkBuilder internal constructor() {
    private val spec = NetworkSpec()

    fun driver(driver: String): NetworkBuilder {
        spec.driver = driver
        return this
    }

    fun external(external: Boolean): NetworkBuilder {
        spec.external = external
        return this
    }

    fun internal(internal: Boolean): NetworkBuilder {
        spec.internal = internal
        return this
    }

    fun attachable(attachable: Boolean): NetworkBuilder {
        spec.attachable = attachable
        return this
    }

    fun name(name: String): NetworkBuilder {
        spec.name = name
        return this
    }

    fun driverOpt(key: String, value: String): NetworkBuilder {
        spec.driverOpt(key, value)
        return this
    }

    fun label(key: String, value: String): NetworkBuilder {
        spec.label(key, value)
        return this
    }

    internal fun build(): NetworkSpec = spec
}

/**
 * Java-friendly builder for volume specifications.
 */
class VolumeBuilder internal constructor() {
    private val spec = VolumeSpec()

    fun driver(driver: String): VolumeBuilder {
        spec.driver = driver
        return this
    }

    fun external(external: Boolean): VolumeBuilder {
        spec.external = external
        return this
    }

    fun name(name: String): VolumeBuilder {
        spec.name = name
        return this
    }

    fun driverOpt(key: String, value: String): VolumeBuilder {
        spec.driverOpt(key, value)
        return this
    }

    fun label(key: String, value: String): VolumeBuilder {
        spec.label(key, value)
        return this
    }

    internal fun build(): VolumeSpec = spec
}

/**
 * Java-friendly builder for build specifications.
 */
class BuildSpecBuilder internal constructor(internal val context: String) {
    internal var dockerfile: String? = null
    internal var target: String? = null
    internal val args = mutableMapOf<String, String>()

    fun dockerfile(dockerfile: String): BuildSpecBuilder {
        this.dockerfile = dockerfile
        return this
    }

    fun target(target: String): BuildSpecBuilder {
        this.target = target
        return this
    }

    fun arg(name: String, value: String): BuildSpecBuilder {
        args[name] = value
        return this
    }
}

/**
 * Java-friendly builder for logging specifications.
 */
class LoggingBuilder internal constructor(private val driver: String) {
    internal val options = mutableMapOf<String, String>()

    fun option(key: String, value: String): LoggingBuilder {
        options[key] = value
        return this
    }

    fun maxSize(size: String): LoggingBuilder = option("max-size", size)
    fun maxFile(count: Int): LoggingBuilder = option("max-file", count.toString())
}

/**
 * Java-friendly builder for healthcheck specifications.
 */
class HealthcheckBuilder internal constructor() {
    internal var test: String? = null
    internal var testCmd: List<String>? = null
    internal var testShell: String? = null
    internal var interval: String? = null
    internal var timeout: String? = null
    internal var retries: Int? = null
    internal var startPeriod: String? = null

    fun test(test: String): HealthcheckBuilder {
        this.test = test
        return this
    }

    fun testCmd(vararg args: String): HealthcheckBuilder {
        this.testCmd = args.toList()
        return this
    }

    fun testShell(cmd: String): HealthcheckBuilder {
        this.testShell = cmd
        return this
    }

    fun interval(interval: String): HealthcheckBuilder {
        this.interval = interval
        return this
    }

    fun timeout(timeout: String): HealthcheckBuilder {
        this.timeout = timeout
        return this
    }

    fun retries(retries: Int): HealthcheckBuilder {
        this.retries = retries
        return this
    }

    fun startPeriod(startPeriod: String): HealthcheckBuilder {
        this.startPeriod = startPeriod
        return this
    }
}

/**
 * Java-friendly builder for deploy specifications.
 */
class DeployBuilder internal constructor() {
    internal var replicas: Int? = null
    internal var mode: String? = null
    internal var resourceLimits: ResourceLimitsBuilder? = null

    fun replicas(replicas: Int): DeployBuilder {
        this.replicas = replicas
        return this
    }

    fun mode(mode: String): DeployBuilder {
        this.mode = mode
        return this
    }

    fun resources(configure: Consumer<ResourceLimitsBuilder>): DeployBuilder {
        val builder = ResourceLimitsBuilder()
        configure.accept(builder)
        this.resourceLimits = builder
        return this
    }
}

/**
 * Java-friendly builder for resource limits.
 */
class ResourceLimitsBuilder internal constructor() {
    internal var cpus: String? = null
    internal var memory: String? = null

    fun cpus(cpus: String): ResourceLimitsBuilder {
        this.cpus = cpus
        return this
    }

    fun memory(memory: String): ResourceLimitsBuilder {
        this.memory = memory
        return this
    }
}

/**
 * Java-friendly builder for secret specifications.
 */
class SecretBuilder internal constructor() {
    private val spec = SecretSpec()

    fun file(file: String): SecretBuilder {
        spec.file = file
        return this
    }

    fun external(external: Boolean): SecretBuilder {
        spec.external = external
        return this
    }

    fun name(name: String): SecretBuilder {
        spec.name = name
        return this
    }

    internal fun build(): SecretSpec = spec
}

/**
 * Java-friendly builder for config specifications.
 */
class ConfigBuilder internal constructor() {
    private val spec = ConfigSpec()

    fun file(file: String): ConfigBuilder {
        spec.file = file
        return this
    }

    fun external(external: Boolean): ConfigBuilder {
        spec.external = external
        return this
    }

    fun name(name: String): ConfigBuilder {
        spec.name = name
        return this
    }

    fun content(content: String): ConfigBuilder {
        spec.content = content
        return this
    }

    internal fun build(): ConfigSpec = spec
}
