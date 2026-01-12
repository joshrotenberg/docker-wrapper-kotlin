package io.github.joshrotenberg.dockerkotlin.rabbitmq

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.template.AbstractTemplate
import io.github.joshrotenberg.dockerkotlin.template.HasConnectionString
import io.github.joshrotenberg.dockerkotlin.template.TemplateConfig
import io.github.joshrotenberg.dockerkotlin.template.WaitStrategy

/**
 * Configuration for RabbitMQ template.
 */
class RabbitMqConfig : TemplateConfig() {
    /** RabbitMQ version/tag. */
    var version: String = "3.13-management-alpine"

    /** Default virtual host. */
    var virtualHost: String = "/"

    /** Username. */
    var username: String = "guest"

    /** Password. */
    var password: String = "guest"

    /** AMQP port to expose (default 5672). */
    var amqpPort: Int = 5672

    /** Management UI port to expose (default 15672). */
    var managementPort: Int = 15672

    /** Host AMQP port mapping (null for dynamic allocation). */
    var hostAmqpPort: Int? = null

    /** Host management port mapping (null for dynamic allocation). */
    var hostManagementPort: Int? = null

    /** Enable management plugin (default true with -management image). */
    var enableManagement: Boolean = true

    /** Data volume for persistence. */
    var dataVolume: String? = null

    /** Additional plugins to enable. */
    val plugins: MutableList<String> = mutableListOf()

    /** Additional environment variables. */
    val envVars: MutableMap<String, String> = mutableMapOf()
}

/**
 * RabbitMQ container template.
 *
 * Provides a pre-configured RabbitMQ container with sensible defaults.
 * Uses the management image by default for the web UI.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val rabbitmq = RabbitMqTemplate("my-rabbitmq") {
 *     username = "myuser"
 *     password = "secret"
 *     virtualHost = "/myapp"
 * }
 *
 * rabbitmq.use {
 *     it.startAndWait()
 *     println(it.amqpUrl())       // amqp://myuser:secret@localhost:5672/myapp
 *     println(it.managementUrl()) // http://localhost:15672
 * }
 * ```
 */
class RabbitMqTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: RabbitMqConfig.() -> Unit = {}
) : AbstractTemplate(name, executor), HasConnectionString {

    override val config = RabbitMqConfig().apply(configure)

    override val image: String = "rabbitmq"
    override val tag: String get() = config.version

    private var cachedAmqpPort: Int? = null
    private var cachedManagementPort: Int? = null

    init {
        // Set default wait strategy - wait for RabbitMQ to be ready
        if (config.waitStrategy == WaitStrategy.Running) {
            config.waitStrategy = WaitStrategy.ForCommand(
                "rabbitmq-diagnostics", "-q", "ping"
            )
        }
    }

    /** Set the RabbitMQ version. */
    fun version(version: String) = apply { config.version = version }

    /** Set the virtual host. */
    fun virtualHost(vhost: String) = apply { config.virtualHost = vhost }

    /** Set credentials. */
    fun credentials(username: String, password: String) = apply {
        config.username = username
        config.password = password
    }

    /** Set the AMQP port. */
    fun amqpPort(port: Int) = apply {
        config.amqpPort = port
        config.hostAmqpPort = port
    }

    /** Set the management UI port. */
    fun managementPort(port: Int) = apply {
        config.managementPort = port
        config.hostManagementPort = port
    }

    /** Use dynamic port allocation. */
    fun dynamicPorts() = apply {
        config.hostAmqpPort = null
        config.hostManagementPort = null
    }

    /** Enable persistence with a named volume. */
    fun withPersistence(volumeName: String) = apply {
        config.dataVolume = volumeName
    }

    /** Enable additional plugins. */
    fun withPlugin(plugin: String) = apply {
        config.plugins.add(plugin)
    }

    /** Add an environment variable. */
    fun withEnv(key: String, value: String) = apply {
        config.envVars[key] = value
    }

    override fun configureRunCommand(cmd: RunCommand) {
        // Environment variables
        cmd.env("RABBITMQ_DEFAULT_USER", config.username)
        cmd.env("RABBITMQ_DEFAULT_PASS", config.password)
        cmd.env("RABBITMQ_DEFAULT_VHOST", config.virtualHost)

        // Enable additional plugins
        if (config.plugins.isNotEmpty()) {
            cmd.env("RABBITMQ_PLUGINS", config.plugins.joinToString(","))
        }

        // Custom environment variables
        config.envVars.forEach { (k, v) -> cmd.env(k, v) }

        // AMQP port mapping
        if (config.hostAmqpPort != null) {
            cmd.port(config.hostAmqpPort!!, config.amqpPort)
        } else {
            cmd.dynamicPort(config.amqpPort)
        }

        // Management port mapping
        if (config.enableManagement) {
            if (config.hostManagementPort != null) {
                cmd.port(config.hostManagementPort!!, config.managementPort)
            } else {
                cmd.dynamicPort(config.managementPort)
            }
        }

        // Data persistence
        config.dataVolume?.let {
            cmd.namedVolume(it, "/var/lib/rabbitmq")
        }
    }

    override suspend fun checkCommandReady(command: List<String>): Boolean {
        return try {
            val result = exec(*command.toTypedArray())
            result.success
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the mapped AMQP port.
     */
    suspend fun discoverAmqpPort(): Int {
        cachedAmqpPort?.let { return it }
        config.hostAmqpPort?.let { return it }

        val id = containerId ?: return config.amqpPort
        val output = executor.execute(listOf("port", id.value, config.amqpPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.amqpPort

        cachedAmqpPort = port
        return port
    }

    /**
     * Get the mapped management port.
     */
    suspend fun discoverManagementPort(): Int {
        cachedManagementPort?.let { return it }
        config.hostManagementPort?.let { return it }

        val id = containerId ?: return config.managementPort
        val output = executor.execute(listOf("port", id.value, config.managementPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.managementPort

        cachedManagementPort = port
        return port
    }

    /**
     * Get the mapped AMQP port (blocking version).
     */
    fun getAmqpPortBlocking(): Int {
        cachedAmqpPort?.let { return it }
        config.hostAmqpPort?.let { return it }

        val id = containerId ?: return config.amqpPort
        val output = executor.executeBlocking(listOf("port", id.value, config.amqpPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.amqpPort

        cachedAmqpPort = port
        return port
    }

    /**
     * Get the mapped management port (blocking version).
     */
    fun getManagementPortBlocking(): Int {
        cachedManagementPort?.let { return it }
        config.hostManagementPort?.let { return it }

        val id = containerId ?: return config.managementPort
        val output = executor.executeBlocking(listOf("port", id.value, config.managementPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.managementPort

        cachedManagementPort = port
        return port
    }

    /**
     * Get the AMQP port (returns cached or static value only).
     */
    fun getAmqpPort(): Int {
        return cachedAmqpPort ?: config.hostAmqpPort ?: config.amqpPort
    }

    /**
     * Get the management port (returns cached or static value only).
     */
    fun getManagementPort(): Int {
        return cachedManagementPort ?: config.hostManagementPort ?: config.managementPort
    }

    override fun connectionString(): String {
        return amqpUrl()
    }

    /**
     * Get the AMQP URL for connecting to RabbitMQ.
     */
    fun amqpUrl(): String {
        val port = getAmqpPort()
        val vhost = if (config.virtualHost == "/") "" else config.virtualHost
        return "amqp://${config.username}:${config.password}@localhost:$port$vhost"
    }

    /**
     * Get the management UI URL.
     */
    fun managementUrl(): String {
        val port = getManagementPort()
        return "http://localhost:$port"
    }

    /**
     * Execute a rabbitmqctl command.
     */
    suspend fun rabbitmqctl(vararg args: String): String {
        val result = exec("rabbitmqctl", *args)
        return result.stdout.trim()
    }

    /**
     * List queues.
     */
    suspend fun listQueues(): String {
        return rabbitmqctl("list_queues", "-p", config.virtualHost)
    }

    /**
     * List exchanges.
     */
    suspend fun listExchanges(): String {
        return rabbitmqctl("list_exchanges", "-p", config.virtualHost)
    }

    companion object {
        /**
         * Create a new RabbitMqTemplate builder (for Java interop).
         */
        @JvmStatic
        fun builder(name: String): RabbitMqTemplateBuilder = RabbitMqTemplateBuilder(name)
    }
}

/**
 * Builder for RabbitMqTemplate (Java-friendly).
 */
class RabbitMqTemplateBuilder(private val name: String) {
    private val config = RabbitMqConfig()

    fun version(version: String) = apply { config.version = version }
    fun virtualHost(vhost: String) = apply { config.virtualHost = vhost }
    fun credentials(username: String, password: String) = apply {
        config.username = username
        config.password = password
    }

    fun amqpPort(port: Int) = apply { config.amqpPort = port; config.hostAmqpPort = port }
    fun managementPort(port: Int) = apply { config.managementPort = port; config.hostManagementPort = port }
    fun withPersistence(volumeName: String) = apply { config.dataVolume = volumeName }
    fun withPlugin(plugin: String) = apply { config.plugins.add(plugin) }

    fun build(): RabbitMqTemplate = RabbitMqTemplate(name) {
        version = this@RabbitMqTemplateBuilder.config.version
        virtualHost = this@RabbitMqTemplateBuilder.config.virtualHost
        username = this@RabbitMqTemplateBuilder.config.username
        password = this@RabbitMqTemplateBuilder.config.password
        amqpPort = this@RabbitMqTemplateBuilder.config.amqpPort
        managementPort = this@RabbitMqTemplateBuilder.config.managementPort
        hostAmqpPort = this@RabbitMqTemplateBuilder.config.hostAmqpPort
        hostManagementPort = this@RabbitMqTemplateBuilder.config.hostManagementPort
        dataVolume = this@RabbitMqTemplateBuilder.config.dataVolume
        plugins.addAll(this@RabbitMqTemplateBuilder.config.plugins)
    }
}
