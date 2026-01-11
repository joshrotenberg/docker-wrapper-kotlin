package io.github.joshrotenberg.dockerkotlin.template

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.ContainerId
import io.github.joshrotenberg.dockerkotlin.core.command.RmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.core.command.StopCommand
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Base interface for container templates.
 *
 * Templates provide pre-configured containers for common services with
 * sensible defaults and easy customization.
 */
interface Template : AutoCloseable {
    /** Template name (used for logging and identification). */
    val name: String

    /** Docker image to use. */
    val image: String

    /** Image tag/version. */
    val tag: String

    /** Container ID (available after start). */
    val containerId: ContainerId?

    /** Whether the container is currently running. */
    val isRunning: Boolean

    /**
     * Start the container.
     *
     * @return The container ID
     */
    suspend fun start(): ContainerId

    /**
     * Start the container and wait for it to be ready.
     *
     * @return The container ID
     */
    suspend fun startAndWait(): ContainerId

    /**
     * Stop the container.
     */
    suspend fun stop()

    /**
     * Remove the container.
     */
    suspend fun remove()

    /**
     * Wait for the container to be ready.
     */
    suspend fun waitForReady()

    /**
     * Get container logs as a flow.
     *
     * @param follow Whether to follow the logs
     * @param tail Number of lines to show from the end
     */
    fun logs(follow: Boolean = false, tail: Int? = null): Flow<String>

    /**
     * Execute a command in the container.
     *
     * @param command Command and arguments to execute
     * @return The execution result
     */
    suspend fun exec(vararg command: String): ExecResult

    /**
     * Stop and remove the container (implements AutoCloseable).
     */
    override fun close()
}

/**
 * Result of executing a command in a container.
 */
data class ExecResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
) {
    val success: Boolean get() = exitCode == 0
}

/**
 * Interface for templates that provide a connection string.
 */
interface HasConnectionString {
    /**
     * Get the connection string for this service.
     */
    fun connectionString(): String
}

/**
 * Pull policy for container images.
 */
enum class PullPolicy {
    /** Always pull before running. */
    ALWAYS,

    /** Pull only if not present locally (default). */
    IF_NOT_PRESENT,

    /** Never pull, fail if not present. */
    NEVER
}

/**
 * Wait strategy for container readiness.
 */
sealed class WaitStrategy {
    /** Wait for the container to be running. */
    data object Running : WaitStrategy()

    /** Wait for a port to be accepting connections. */
    data class ForPort(val port: Int) : WaitStrategy()

    /** Wait for an HTTP endpoint to return expected status. */
    data class ForHttp(
        val path: String,
        val port: Int = 80,
        val statusCode: Int = 200
    ) : WaitStrategy()

    /** Wait for a log message matching a pattern. */
    data class ForLogMessage(val pattern: Regex) : WaitStrategy() {
        constructor(substring: String) : this(Regex.escape(substring).toRegex())
    }

    /** Wait for a command to succeed. */
    data class ForCommand(val command: List<String>) : WaitStrategy() {
        constructor(vararg command: String) : this(command.toList())
    }
}

/**
 * Configuration for container templates.
 */
open class TemplateConfig {
    /** Container name (auto-generated if not specified). */
    var containerName: String? = null

    /** Name prefix for auto-generated names. */
    var namePrefix: String = "dkot"

    /** Pull policy for the image. */
    var pullPolicy: PullPolicy = PullPolicy.IF_NOT_PRESENT

    /** Wait strategy for readiness. */
    var waitStrategy: WaitStrategy = WaitStrategy.Running

    /** Timeout for waiting. */
    var waitTimeout: Duration = 60.seconds

    /** Polling interval for wait strategies. */
    var waitPollInterval: Duration = 500.milliseconds

    /** Whether to remove the container when closed. */
    var removeOnClose: Boolean = true

    /** Environment variables. */
    val environment: MutableMap<String, String> = mutableMapOf()

    /** Port mappings (host to container). */
    val ports: MutableMap<Int, Int> = mutableMapOf()

    /** Dynamic port mappings (container ports with auto-allocated host ports). */
    val dynamicPorts: MutableList<Int> = mutableListOf()

    /** Volume mounts (host path to container path). */
    val volumes: MutableMap<String, String> = mutableMapOf()

    /** Labels. */
    val labels: MutableMap<String, String> = mutableMapOf()

    /** Network to connect to. */
    var network: String? = null

    /** Network aliases. */
    val networkAliases: MutableList<String> = mutableListOf()

    /**
     * Generate a container name.
     */
    fun generateName(service: String): String {
        return containerName ?: "$namePrefix-$service-${UUID.randomUUID().toString().take(8)}"
    }
}

/**
 * Abstract base implementation for templates.
 */
abstract class AbstractTemplate(
    override val name: String,
    protected val executor: CommandExecutor = CommandExecutor()
) : Template {

    private val logger = LoggerFactory.getLogger(javaClass)

    protected abstract val config: TemplateConfig

    private var _containerId: ContainerId? = null
    override val containerId: ContainerId? get() = _containerId

    private var _isRunning = false
    override val isRunning: Boolean get() = _isRunning

    /** The full image reference (image:tag). */
    val imageRef: String get() = "$image:$tag"

    /**
     * Configure the run command with template-specific settings.
     */
    protected abstract fun configureRunCommand(cmd: RunCommand)

    override suspend fun start(): ContainerId {
        logger.info("Starting container {} with image {}", name, imageRef)

        val cmd = RunCommand(imageRef, executor)
            .name(config.generateName(name))
            .detach()

        // Apply common configuration
        config.environment.forEach { (k, v) -> cmd.env(k, v) }
        config.ports.forEach { (host, container) -> cmd.port(host, container) }
        config.dynamicPorts.forEach { cmd.dynamicPort(it) }
        config.volumes.forEach { (host, container) -> cmd.volume(host, container) }
        config.labels.forEach { (k, v) -> cmd.label(k, v) }
        config.network?.let { cmd.network(it) }
        config.networkAliases.forEach { cmd.networkAlias(it) }

        // Add management labels
        cmd.label("io.github.docker-kotlin.managed", "true")
        cmd.label("io.github.docker-kotlin.template", name)

        // Apply template-specific configuration
        configureRunCommand(cmd)

        _containerId = cmd.execute()
        _isRunning = true

        logger.info("Container {} started with ID {}", name, _containerId?.short())

        return _containerId!!
    }

    override suspend fun startAndWait(): ContainerId {
        val id = start()
        waitForReady()
        return id
    }

    override suspend fun stop() {
        val id = _containerId ?: return

        logger.info("Stopping container {}", id.short())
        StopCommand(id.value, executor).execute()
        _isRunning = false
    }

    override suspend fun remove() {
        val id = _containerId ?: return

        logger.info("Removing container {}", id.short())
        RmCommand(id.value, executor).force().execute()
        _containerId = null
    }

    override suspend fun waitForReady() {
        val startTime = System.currentTimeMillis()
        val timeoutMs = config.waitTimeout.inWholeMilliseconds

        logger.debug("Waiting for container {} to be ready (strategy: {})", name, config.waitStrategy)

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (checkReady()) {
                logger.info("Container {} is ready", name)
                return
            }
            delay(config.waitPollInterval)
        }

        throw TemplateException.WaitTimeout(name, config.waitTimeout)
    }

    /**
     * Check if the container is ready based on the wait strategy.
     */
    protected open suspend fun checkReady(): Boolean {
        return when (val strategy = config.waitStrategy) {
            is WaitStrategy.Running -> isRunning
            is WaitStrategy.ForPort -> checkPortReady(strategy.port)
            is WaitStrategy.ForHttp -> checkHttpReady(strategy)
            is WaitStrategy.ForLogMessage -> checkLogReady(strategy.pattern)
            is WaitStrategy.ForCommand -> checkCommandReady(strategy.command)
        }
    }

    protected open suspend fun checkPortReady(port: Int): Boolean {
        // Default implementation - subclasses can override
        return isRunning
    }

    protected open suspend fun checkHttpReady(strategy: WaitStrategy.ForHttp): Boolean {
        // Default implementation - subclasses can override
        return isRunning
    }

    protected open suspend fun checkLogReady(pattern: Regex): Boolean {
        // Default implementation - subclasses can override
        return isRunning
    }

    protected open suspend fun checkCommandReady(command: List<String>): Boolean {
        if (!isRunning) return false
        val result = exec(*command.toTypedArray())
        return result.success
    }

    override fun logs(follow: Boolean, tail: Int?): Flow<String> = flow {
        // Basic implementation - can be enhanced with streaming
        val id = _containerId ?: return@flow
        val args = buildList {
            add("logs")
            if (follow) add("--follow")
            tail?.let { add("--tail"); add(it.toString()) }
            add(id.value)
        }
        val output = executor.execute(args)
        output.stdout.lines().forEach { emit(it) }
    }

    override suspend fun exec(vararg command: String): ExecResult {
        val id = _containerId ?: throw TemplateException.NotRunning(name)

        val args = buildList {
            add("exec")
            add(id.value)
            addAll(command)
        }

        val output = executor.execute(args)
        return ExecResult(output.stdout, output.stderr, output.exitCode)
    }

    override fun close() {
        if (_isRunning && config.removeOnClose) {
            // Use blocking version for close()
            try {
                _containerId?.let { id ->
                    logger.info("Closing container {}", id.short())
                    StopCommand(id.value, executor).executeBlocking()
                    RmCommand(id.value, executor).force().executeBlocking()
                }
            } catch (e: Exception) {
                logger.warn("Error closing container: {}", e.message)
            }
        }
        _isRunning = false
        _containerId = null
    }
}

/**
 * Exceptions specific to templates.
 */
sealed class TemplateException(message: String) : Exception(message) {
    class WaitTimeout(name: String, timeout: Duration) :
        TemplateException("Timed out waiting for container '$name' to be ready after $timeout")

    class NotRunning(name: String) :
        TemplateException("Container '$name' is not running")

    class StartFailed(name: String, cause: Throwable) :
        TemplateException("Failed to start container '$name': ${cause.message}")
}
