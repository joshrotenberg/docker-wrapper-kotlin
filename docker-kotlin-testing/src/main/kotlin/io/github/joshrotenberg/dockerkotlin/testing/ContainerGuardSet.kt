package io.github.joshrotenberg.dockerkotlin.testing

import io.github.joshrotenberg.dockerkotlin.template.Template
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages a group of [ContainerGuard] instances as a unit.
 *
 * This is useful when tests require multiple containers that should be
 * started and stopped together. Containers can be started in parallel
 * for faster test setup.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * ContainerGuardSet()
 *     .add("redis", ContainerGuard(RedisTemplate("redis") { port(6379) }))
 *     .add("postgres", ContainerGuard(PostgresTemplate("postgres") { port(5432) }))
 *     .startAll()
 *     .use { set ->
 *         val redis = set.get<RedisTemplate>("redis")
 *         val postgres = set.get<PostgresTemplate>("postgres")
 *         // Use containers...
 *     }
 * // All containers stopped and removed
 * ```
 *
 * ## Parallel Start
 *
 * Start all containers concurrently for faster setup:
 *
 * ```kotlin
 * set.startAllParallel().use { ... }
 * ```
 *
 * ## Named Access
 *
 * Retrieve containers by name with type safety:
 *
 * ```kotlin
 * val redis: ContainerGuard<RedisTemplate> = set.get("redis")
 * ```
 */
class ContainerGuardSet : AutoCloseable {

    private val logger = LoggerFactory.getLogger(ContainerGuardSet::class.java)

    private val guards = ConcurrentHashMap<String, ContainerGuard<*>>()
    private val insertionOrder = mutableListOf<String>()
    private val cleanedUp = AtomicBoolean(false)
    private var _testFailed = false

    /**
     * Add a container guard to the set.
     *
     * @param name Unique name to identify this container
     * @param guard The container guard to add
     * @return This set for chaining
     */
    fun <T : Template> add(name: String, guard: ContainerGuard<T>): ContainerGuardSet {
        require(!guards.containsKey(name)) { "Container with name '$name' already exists" }
        guards[name] = guard
        synchronized(insertionOrder) {
            insertionOrder.add(name)
        }
        return this
    }

    /**
     * Get a container guard by name.
     *
     * @param name The name of the container
     * @return The container guard
     * @throws NoSuchElementException if no container with that name exists
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Template> get(name: String): ContainerGuard<T> {
        return guards[name] as? ContainerGuard<T>
            ?: throw NoSuchElementException("No container with name '$name'")
    }

    /**
     * Get a container guard by name, or null if not found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Template> getOrNull(name: String): ContainerGuard<T>? {
        return guards[name] as? ContainerGuard<T>
    }

    /**
     * Check if a container with the given name exists.
     */
    fun contains(name: String): Boolean = guards.containsKey(name)

    /**
     * Get all container names.
     */
    fun names(): Set<String> = guards.keys.toSet()

    /**
     * Get the number of containers in this set.
     */
    val size: Int get() = guards.size

    /**
     * Start all containers sequentially in insertion order.
     *
     * @return This set for chaining
     */
    suspend fun startAll(): ContainerGuardSet {
        val orderedNames = synchronized(insertionOrder) { insertionOrder.toList() }
        for (name in orderedNames) {
            val guard = guards[name]!!
            logger.debug("Starting container: {}", name)
            guard.start()
        }
        return this
    }

    /**
     * Start all containers sequentially (blocking version).
     */
    fun startAllBlocking(): ContainerGuardSet = runBlocking { startAll() }

    /**
     * Start all containers in parallel.
     *
     * This is faster than [startAll] but order of startup is not guaranteed.
     *
     * @return This set for chaining
     */
    suspend fun startAllParallel(): ContainerGuardSet = coroutineScope {
        val deferred = guards.map { (name, guard) ->
            async {
                logger.debug("Starting container (parallel): {}", name)
                guard.start()
            }
        }
        deferred.awaitAll()
        this@ContainerGuardSet
    }

    /**
     * Start all containers in parallel (blocking version).
     */
    fun startAllParallelBlocking(): ContainerGuardSet = runBlocking { startAllParallel() }

    /**
     * Mark all containers as failed.
     *
     * When keepOnFailure is enabled on individual containers,
     * this prevents cleanup on close.
     */
    fun markFailed() {
        _testFailed = true
        guards.values.forEach { it.markFailed() }
    }

    /**
     * Manually trigger cleanup of all containers.
     *
     * Containers are stopped in reverse insertion order.
     */
    suspend fun cleanup() {
        if (cleanedUp.getAndSet(true)) {
            return
        }

        val orderedNames = synchronized(insertionOrder) { insertionOrder.reversed() }
        for (name in orderedNames) {
            val guard = guards[name]!!
            try {
                logger.debug("Cleaning up container: {}", name)
                guard.cleanup()
            } catch (e: Exception) {
                logger.warn("Error cleaning up container '{}': {}", name, e.message)
            }
        }
    }

    /**
     * Cleanup all containers (blocking version).
     */
    fun cleanupBlocking() = runBlocking { cleanup() }

    override fun close() {
        if (cleanedUp.get()) {
            return
        }

        val orderedNames = synchronized(insertionOrder) { insertionOrder.reversed() }
        for (name in orderedNames) {
            val guard = guards[name]!!
            try {
                guard.close()
            } catch (e: Exception) {
                logger.warn("Error closing container '{}': {}", name, e.message)
            }
        }
        cleanedUp.set(true)
    }

    companion object {
        /**
         * Create a new empty container guard set.
         */
        @JvmStatic
        fun create(): ContainerGuardSet = ContainerGuardSet()
    }
}

/**
 * Kotlin DSL for creating a ContainerGuardSet.
 */
inline fun containerGuardSet(
    configure: ContainerGuardSetBuilder.() -> Unit
): ContainerGuardSet {
    return ContainerGuardSetBuilder().apply(configure).build()
}

/**
 * Builder for ContainerGuardSet using Kotlin DSL.
 */
class ContainerGuardSetBuilder {
    @PublishedApi
    internal val set = ContainerGuardSet()

    /**
     * Add a container to the set.
     */
    fun <T : Template> container(name: String, guard: ContainerGuard<T>) {
        set.add(name, guard)
    }

    /**
     * Add a container to the set with inline configuration.
     */
    inline fun <T : Template> container(
        name: String,
        template: T,
        configure: ContainerGuard<T>.() -> Unit = {}
    ) {
        set.add(name, ContainerGuard(template).apply(configure))
    }

    fun build(): ContainerGuardSet = set
}
