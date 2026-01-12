package io.github.joshrotenberg.dockerkotlin.core.lifecycle

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.PsCommand
import io.github.joshrotenberg.dockerkotlin.core.command.RmCommand
import io.github.joshrotenberg.dockerkotlin.core.command.StopCommand
import io.github.joshrotenberg.dockerkotlin.core.model.ContainerSummary
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Global configuration and lifecycle management for Docker Kotlin.
 *
 * This object provides:
 * - Global configuration settings
 * - Container tracking with labels
 * - JVM shutdown hook for automatic cleanup
 * - Manual cleanup APIs
 *
 * Example usage:
 * ```kotlin
 * // Configure global settings
 * DockerKotlin.configure {
 *     enableShutdownHook = true
 *     shutdownTimeout = 30.seconds
 *     cleanupOnShutdown = true
 * }
 *
 * // Track a container
 * DockerKotlin.track(containerId)
 *
 * // Manual cleanup
 * DockerKotlin.cleanupAll()
 * ```
 */
object DockerKotlin {
    private val logger = LoggerFactory.getLogger(DockerKotlin::class.java)

    /** Unique session ID for this JVM instance. */
    val sessionId: String = UUID.randomUUID().toString().take(8)

    /** Label prefix for tracking managed containers. */
    const val LABEL_PREFIX = "io.github.docker-kotlin"

    /** Label key for session ID. */
    const val LABEL_SESSION = "$LABEL_PREFIX.session"

    /** Label key for managed marker. */
    const val LABEL_MANAGED = "$LABEL_PREFIX.managed"

    /** Label key for creation timestamp. */
    const val LABEL_CREATED = "$LABEL_PREFIX.created"

    private val config = LifecycleConfig()
    private val trackedContainers = ConcurrentHashMap.newKeySet<String>()
    private val shutdownHookRegistered = AtomicBoolean(false)
    private val executor = CommandExecutor()

    /**
     * Configure lifecycle settings.
     *
     * @param block Configuration block
     */
    @JvmStatic
    fun configure(block: LifecycleConfig.() -> Unit) {
        config.apply(block)
        ensureShutdownHook()
    }

    /**
     * Get the current configuration (read-only view).
     */
    @JvmStatic
    fun getConfig(): LifecycleConfigView = config

    /**
     * Get the labels to apply to managed containers.
     *
     * @return Map of labels for container tracking
     */
    @JvmStatic
    fun managedLabels(): Map<String, String> = mapOf(
        LABEL_MANAGED to "true",
        LABEL_SESSION to sessionId,
        LABEL_CREATED to System.currentTimeMillis().toString()
    )

    /**
     * Track a container for lifecycle management.
     *
     * @param containerId The container ID to track
     */
    @JvmStatic
    fun track(containerId: String) {
        trackedContainers.add(containerId)
        logger.debug("Tracking container: {}", containerId)
    }

    /**
     * Untrack a container (e.g., after manual removal).
     *
     * @param containerId The container ID to untrack
     */
    @JvmStatic
    fun untrack(containerId: String) {
        trackedContainers.remove(containerId)
        logger.debug("Untracked container: {}", containerId)
    }

    /**
     * Get all tracked container IDs.
     *
     * @return Set of tracked container IDs
     */
    @JvmStatic
    fun trackedContainers(): Set<String> = trackedContainers.toSet()

    /**
     * Clean up all tracked containers.
     * Stops and removes all containers tracked in this session.
     *
     * @param force Force removal of running containers
     * @return Number of containers cleaned up
     */
    @JvmStatic
    fun cleanupAll(force: Boolean = true): Int {
        val containers = trackedContainers.toList()
        if (containers.isEmpty()) {
            logger.debug("No tracked containers to clean up")
            return 0
        }

        logger.info("Cleaning up {} tracked containers", containers.size)
        var cleaned = 0

        for (containerId in containers) {
            try {
                cleanupContainer(containerId, force)
                trackedContainers.remove(containerId)
                cleaned++
            } catch (e: Exception) {
                logger.warn("Failed to clean up container {}: {}", containerId, e.message)
            }
        }

        return cleaned
    }

    /**
     * Clean up orphaned containers from previous sessions.
     * Finds and removes containers with the managed label but different session ID.
     *
     * @param force Force removal of running containers
     * @return Number of orphaned containers cleaned up
     */
    @JvmStatic
    fun cleanupOrphans(force: Boolean = true): Int {
        logger.info("Looking for orphaned containers")

        val orphans = findOrphanedContainers()
        if (orphans.isEmpty()) {
            logger.debug("No orphaned containers found")
            return 0
        }

        logger.info("Found {} orphaned containers", orphans.size)
        var cleaned = 0

        for (container in orphans) {
            try {
                cleanupContainer(container.id, force)
                cleaned++
            } catch (e: Exception) {
                logger.warn("Failed to clean up orphan {}: {}", container.id, e.message)
            }
        }

        return cleaned
    }

    /**
     * Clean up containers matching a filter predicate.
     *
     * @param force Force removal of running containers
     * @param predicate Filter to select containers to clean up
     * @return Number of containers cleaned up
     */
    @JvmStatic
    fun cleanup(force: Boolean = true, predicate: (ContainerSummary) -> Boolean): Int {
        val allContainers = listManagedContainers()
        val toCleanup = allContainers.filter(predicate)

        if (toCleanup.isEmpty()) {
            logger.debug("No containers match cleanup predicate")
            return 0
        }

        logger.info("Cleaning up {} containers matching predicate", toCleanup.size)
        var cleaned = 0

        for (container in toCleanup) {
            try {
                cleanupContainer(container.id, force)
                trackedContainers.remove(container.id)
                cleaned++
            } catch (e: Exception) {
                logger.warn("Failed to clean up container {}: {}", container.id, e.message)
            }
        }

        return cleaned
    }

    /**
     * List all managed containers (from any session).
     *
     * @return List of managed containers
     */
    @JvmStatic
    fun listManagedContainers(): List<ContainerSummary> {
        return try {
            PsCommand(executor)
                .all()
                .filter("label=$LABEL_MANAGED=true")
                .executeBlocking()
        } catch (e: Exception) {
            logger.warn("Failed to list managed containers: {}", e.message)
            emptyList()
        }
    }

    /**
     * Find orphaned containers (from previous sessions).
     */
    private fun findOrphanedContainers(): List<ContainerSummary> {
        return try {
            PsCommand(executor)
                .all()
                .filter("label=$LABEL_MANAGED=true")
                .executeBlocking()
                .filter { container ->
                    val labels = container.labelsMap()
                    val containerSession = labels[LABEL_SESSION]
                    containerSession != null && containerSession != sessionId
                }
        } catch (e: Exception) {
            logger.warn("Failed to find orphaned containers: {}", e.message)
            emptyList()
        }
    }

    /**
     * Stop and remove a container.
     */
    private fun cleanupContainer(containerId: String, force: Boolean) {
        logger.debug("Cleaning up container: {}", containerId)

        // Try to stop gracefully first
        try {
            StopCommand(containerId, executor)
                .time(config.shutdownTimeout.inWholeSeconds.toInt())
                .executeBlocking()
        } catch (e: Exception) {
            logger.debug("Stop failed for {}: {}", containerId, e.message)
        }

        // Remove the container
        RmCommand(containerId, executor)
            .apply { if (force) force() }
            .executeBlocking()

        logger.debug("Removed container: {}", containerId)
    }

    /**
     * Ensure shutdown hook is registered if enabled.
     */
    private fun ensureShutdownHook() {
        if (config.enableShutdownHook && shutdownHookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(Thread {
                if (config.cleanupOnShutdown) {
                    logger.info("JVM shutdown - cleaning up containers")
                    cleanupAll(force = true)
                }
            })
            logger.debug("Registered JVM shutdown hook")
        }
    }

    /**
     * Reset state (for testing).
     */
    internal fun reset() {
        trackedContainers.clear()
        config.reset()
    }
}

/**
 * Read-only view of lifecycle configuration.
 */
interface LifecycleConfigView {
    val enableShutdownHook: Boolean
    val shutdownTimeout: Duration
    val cleanupOnShutdown: Boolean
}

/**
 * Configuration for container lifecycle management.
 */
class LifecycleConfig : LifecycleConfigView {
    /** Whether to register a JVM shutdown hook for cleanup. */
    override var enableShutdownHook: Boolean = true

    /** Grace period for container stop before force kill. */
    override var shutdownTimeout: Duration = 30.seconds

    /** Whether to clean up tracked containers on JVM shutdown. */
    override var cleanupOnShutdown: Boolean = true

    internal fun reset() {
        enableShutdownHook = true
        shutdownTimeout = 30.seconds
        cleanupOnShutdown = true
    }
}
