package io.github.joshrotenberg.dockerkotlin.testing

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.ContainerId
import io.github.joshrotenberg.dockerkotlin.core.command.RmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.StopCommand
import io.github.joshrotenberg.dockerkotlin.template.ExecResult
import io.github.joshrotenberg.dockerkotlin.template.HasConnectionString
import io.github.joshrotenberg.dockerkotlin.template.Template
import io.github.joshrotenberg.dockerkotlin.template.TemplateException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Options for controlling container lifecycle behavior.
 */
data class GuardOptions(
    /** Remove container on close (default: true). */
    val removeOnClose: Boolean = true,

    /** Stop container on close (default: true). */
    val stopOnClose: Boolean = true,

    /** Keep container running if test fails/panics (default: false). */
    val keepOnFailure: Boolean = false,

    /** Capture container logs and print on failure (default: false). */
    val captureLogs: Boolean = false,

    /** Reuse existing container if already running (default: false). */
    val reuseIfRunning: Boolean = false,

    /** Automatically wait for container to be ready after start (default: true). */
    val waitForReady: Boolean = true,

    /** Timeout for stop operations during cleanup (default: 10 seconds). */
    val stopTimeout: Duration = 10.seconds
)

/**
 * RAII-style guard for automatic container lifecycle management.
 *
 * When this guard is closed, the container is automatically stopped and
 * removed (unless configured otherwise).
 *
 * ## Basic Usage
 *
 * ```kotlin
 * ContainerGuard(RedisTemplate("test-redis") { port(6379) })
 *     .waitForReady(true)
 *     .start()
 *     .use { guard ->
 *         // Container is running and ready
 *         val url = guard.connectionString()
 *         // Use the container...
 *     }
 * // Container automatically stopped and removed
 * ```
 *
 * ## Debugging Failed Tests
 *
 * ```kotlin
 * ContainerGuard(template)
 *     .keepOnFailure(true)   // Keep container if test fails
 *     .captureLogs(true)     // Print logs on failure
 *     .start()
 *     .use { guard ->
 *         // If test fails, container stays running for inspection
 *     }
 * ```
 *
 * ## Container Reuse
 *
 * Speed up local development by reusing running containers:
 *
 * ```kotlin
 * ContainerGuard(template)
 *     .reuseIfRunning(true)  // Reuse existing container
 *     .removeOnClose(false)  // Keep for next run
 *     .stopOnClose(false)
 *     .start()
 * ```
 */
class ContainerGuard<T : Template>(
    private val template: T,
    private val executor: CommandExecutor = CommandExecutor()
) : AutoCloseable {

    private val logger = LoggerFactory.getLogger(ContainerGuard::class.java)

    private var options = GuardOptions()
    private var _containerId: ContainerId? = null
    private var _wasReused = false
    private var _testFailed = false
    private val cleanedUp = AtomicBoolean(false)

    /** The container ID (available after start). */
    val containerId: ContainerId? get() = _containerId

    /** Whether the container was reused from a previous run. */
    val wasReused: Boolean get() = _wasReused

    /** Access the underlying template. */
    fun template(): T = template

    // Builder methods

    /** Set whether to remove the container on close (default: true). */
    fun removeOnClose(remove: Boolean) = apply {
        options = options.copy(removeOnClose = remove)
    }

    /** Set whether to stop the container on close (default: true). */
    fun stopOnClose(stop: Boolean) = apply {
        options = options.copy(stopOnClose = stop)
    }

    /** Keep container running if test fails (default: false). */
    fun keepOnFailure(keep: Boolean) = apply {
        options = options.copy(keepOnFailure = keep)
    }

    /** Capture and print container logs on failure (default: false). */
    fun captureLogs(capture: Boolean) = apply {
        options = options.copy(captureLogs = capture)
    }

    /** Reuse existing container if already running (default: false). */
    fun reuseIfRunning(reuse: Boolean) = apply {
        options = options.copy(reuseIfRunning = reuse)
    }

    /** Wait for container to be ready after start (default: true). */
    fun waitForReady(wait: Boolean) = apply {
        options = options.copy(waitForReady = wait)
    }

    /** Set timeout for stop operations (default: 10 seconds). */
    fun stopTimeout(timeout: Duration) = apply {
        options = options.copy(stopTimeout = timeout)
    }

    /**
     * Start the container and return this guard.
     *
     * If `reuseIfRunning` is enabled and a container is already running,
     * it will be reused instead of starting a new one.
     *
     * If `waitForReady` is enabled (default), this method will block until
     * the container passes its readiness check.
     *
     * @return This guard for chaining
     * @throws TemplateException if the container fails to start
     */
    suspend fun start(): ContainerGuard<T> {
        // Check if we should reuse an existing container
        if (options.reuseIfRunning && template.isRunning) {
            logger.info("Reusing existing container: {}", template.name)
            _wasReused = true

            if (options.waitForReady) {
                template.waitForReady()
            }

            return this
        }

        // Start the container
        _containerId = if (options.waitForReady) {
            template.startAndWait()
        } else {
            template.start()
        }

        logger.info("Container started: {} ({})", template.name, _containerId?.short())

        return this
    }

    /**
     * Start the container (blocking version).
     */
    fun startBlocking(): ContainerGuard<T> = runBlocking { start() }

    /**
     * Check if the container is currently running.
     */
    val isRunning: Boolean get() = template.isRunning

    /**
     * Wait for the container to be ready.
     */
    suspend fun waitForReady() {
        template.waitForReady()
    }

    /**
     * Wait for the container to be ready (blocking).
     */
    fun waitForReadyBlocking() = runBlocking { waitForReady() }

    /**
     * Get container logs.
     */
    fun logs(follow: Boolean = false, tail: Int? = null): Flow<String> {
        return template.logs(follow, tail)
    }

    /**
     * Execute a command in the container.
     */
    suspend fun exec(vararg command: String): ExecResult {
        return template.exec(*command)
    }

    /**
     * Execute a command in the container (blocking).
     */
    fun execBlocking(vararg command: String): ExecResult = runBlocking { exec(*command) }

    /**
     * Mark the test as failed.
     *
     * When keepOnFailure is enabled, this prevents cleanup on close.
     */
    fun markFailed() {
        _testFailed = true
    }

    /**
     * Manually trigger cleanup (stop and remove).
     *
     * After calling this, close() will not attempt cleanup again.
     */
    suspend fun cleanup() {
        if (cleanedUp.getAndSet(true)) {
            return // Already cleaned up
        }

        val name = template.name

        try {
            if (options.stopOnClose) {
                logger.debug("Stopping container: {}", name)
                template.stop()
            }

            if (options.removeOnClose) {
                logger.debug("Removing container: {}", name)
                template.remove()
            }
        } catch (e: Exception) {
            logger.warn("Error during cleanup of container {}: {}", name, e.message)
        }
    }

    /**
     * Manually trigger cleanup (blocking).
     */
    fun cleanupBlocking() = runBlocking { cleanup() }

    override fun close() {
        if (cleanedUp.get()) {
            return
        }

        // Check if we should keep the container
        if (_testFailed && options.keepOnFailure) {
            logger.info("Test failed, keeping container '{}' for debugging", template.name)

            if (options.captureLogs) {
                try {
                    runBlocking {
                        val logContent = StringBuilder()
                        template.logs(tail = 100).collect { logContent.appendLine(it) }
                        logger.info("Container logs for '{}':\n{}", template.name, logContent)
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to capture logs: {}", e.message)
                }
            }

            return
        }

        // Skip cleanup for reused containers if not configured to clean them
        if (_wasReused && !options.removeOnClose) {
            return
        }

        runBlocking { cleanup() }
    }

    companion object {
        /**
         * Create a guard for the given template.
         */
        @JvmStatic
        fun <T : Template> of(template: T): ContainerGuard<T> = ContainerGuard(template)
    }
}

/**
 * Extension function to get connection string from a guard with a HasConnectionString template.
 */
fun <T> ContainerGuard<T>.connectionString(): String where T : Template, T : HasConnectionString {
    return template().connectionString()
}

/**
 * Kotlin DSL for creating a ContainerGuard.
 */
inline fun <T : Template> containerGuard(
    template: T,
    configure: ContainerGuard<T>.() -> Unit = {}
): ContainerGuard<T> {
    return ContainerGuard(template).apply(configure)
}
