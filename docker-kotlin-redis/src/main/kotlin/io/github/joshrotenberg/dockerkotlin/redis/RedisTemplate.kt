package io.github.joshrotenberg.dockerkotlin.redis

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.template.AbstractTemplate
import io.github.joshrotenberg.dockerkotlin.template.HasConnectionString
import io.github.joshrotenberg.dockerkotlin.template.TemplateConfig
import io.github.joshrotenberg.dockerkotlin.template.WaitStrategy

/**
 * Redis memory eviction policies.
 */
enum class MaxMemoryPolicy(val value: String) {
    NOEVICTION("noeviction"),
    ALLKEYS_LRU("allkeys-lru"),
    ALLKEYS_LFU("allkeys-lfu"),
    ALLKEYS_RANDOM("allkeys-random"),
    VOLATILE_LRU("volatile-lru"),
    VOLATILE_LFU("volatile-lfu"),
    VOLATILE_RANDOM("volatile-random"),
    VOLATILE_TTL("volatile-ttl")
}

/**
 * Configuration for Redis template.
 */
class RedisConfig : TemplateConfig() {
    /** Redis version/tag. */
    var version: String = "7-alpine"

    /** Redis password (empty for no auth). */
    var password: String? = null

    /** Port to expose (default 6379). */
    var port: Int = 6379

    /** Host port mapping (null for dynamic allocation). */
    var hostPort: Int? = null

    /** Maximum memory limit. */
    var maxMemory: String? = null

    /** Memory eviction policy. */
    var maxMemoryPolicy: MaxMemoryPolicy? = null

    /** Enable persistence with the given volume. */
    var persistenceVolume: String? = null

    /** Additional Redis configuration options. */
    val redisConfig: MutableList<String> = mutableListOf()
}

/**
 * Redis container template.
 *
 * Provides a pre-configured Redis container with sensible defaults.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val redis = RedisTemplate("my-redis") {
 *     port(6379)
 *     password("secret")
 *     version("7.2")
 * }
 *
 * redis.use {
 *     it.startAndWait()
 *     println(it.connectionString()) // redis://:secret@localhost:6379
 * }
 * ```
 *
 * Example usage (Java):
 * ```java
 * try (RedisTemplate redis = RedisTemplate.builder("my-redis")
 *         .port(6379)
 *         .password("secret")
 *         .build()) {
 *     redis.startAndWaitBlocking();
 *     System.out.println(redis.connectionString());
 * }
 * ```
 */
class RedisTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: RedisConfig.() -> Unit = {}
) : AbstractTemplate(name, executor), HasConnectionString {

    override val config = RedisConfig().apply(configure)

    override val image: String = "redis"
    override val tag: String get() = config.version

    private var mappedPort: Int? = null

    init {
        // Set default wait strategy
        if (config.waitStrategy == WaitStrategy.Running) {
            config.waitStrategy = WaitStrategy.ForCommand("redis-cli", "ping")
        }
    }

    /** Set the Redis version. */
    fun version(version: String) = apply { config.version = version }

    /** Set the Redis password. */
    fun password(password: String) = apply { config.password = password }

    /** Set the port to expose. */
    fun port(port: Int) = apply {
        config.port = port
        config.hostPort = port
    }

    /** Use dynamic port allocation. */
    fun dynamicPort() = apply {
        config.hostPort = null
    }

    /** Set maximum memory. */
    fun maxMemory(maxMemory: String) = apply { config.maxMemory = maxMemory }

    /** Set memory eviction policy. */
    fun maxMemoryPolicy(policy: MaxMemoryPolicy) = apply { config.maxMemoryPolicy = policy }

    /** Enable persistence with a named volume. */
    fun withPersistence(volumeName: String) = apply {
        config.persistenceVolume = volumeName
    }

    /** Add a Redis configuration option. */
    fun withConfig(option: String) = apply { config.redisConfig.add(option) }

    override fun configureRunCommand(cmd: RunCommand) {
        // Port mapping
        if (config.hostPort != null) {
            cmd.port(config.hostPort!!, config.port)
        } else {
            cmd.dynamicPort(config.port)
        }

        // Persistence
        config.persistenceVolume?.let {
            cmd.namedVolume(it, "/data")
        }

        // Build Redis command arguments
        val redisArgs = buildList {
            add("redis-server")

            config.password?.let {
                add("--requirepass")
                add(it)
            }

            config.maxMemory?.let {
                add("--maxmemory")
                add(it)
            }

            config.maxMemoryPolicy?.let {
                add("--maxmemory-policy")
                add(it.value)
            }

            config.persistenceVolume?.let {
                add("--appendonly")
                add("yes")
            }

            config.redisConfig.forEach { add(it) }
        }

        if (redisArgs.size > 1) {
            cmd.command(*redisArgs.toTypedArray())
        }
    }

    override suspend fun checkPortReady(port: Int): Boolean {
        return try {
            val result = exec("redis-cli", "ping")
            result.stdout.trim() == "PONG"
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun checkCommandReady(command: List<String>): Boolean {
        return try {
            val result = exec(*command.toTypedArray())
            result.success && result.stdout.trim() == "PONG"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the mapped host port.
     */
    fun getMappedPort(): Int {
        return mappedPort ?: config.hostPort ?: config.port
    }

    override fun connectionString(): String {
        val port = getMappedPort()
        val auth = config.password?.let { ":$it@" } ?: ""
        return "redis://${auth}localhost:$port"
    }

    /**
     * Execute a Redis CLI command.
     */
    suspend fun redisCommand(vararg args: String): String {
        val cmdArgs = buildList {
            add("redis-cli")
            config.password?.let {
                add("-a")
                add(it)
            }
            addAll(args)
        }
        val result = exec(*cmdArgs.toTypedArray())
        return result.stdout.trim()
    }

    companion object {
        /**
         * Create a new RedisTemplate builder (for Java interop).
         */
        @JvmStatic
        fun builder(name: String): RedisTemplateBuilder = RedisTemplateBuilder(name)
    }
}

/**
 * Builder for RedisTemplate (Java-friendly).
 */
class RedisTemplateBuilder(private val name: String) {
    private val config = RedisConfig()

    fun version(version: String) = apply { config.version = version }
    fun password(password: String) = apply { config.password = password }
    fun port(port: Int) = apply { config.port = port; config.hostPort = port }
    fun maxMemory(maxMemory: String) = apply { config.maxMemory = maxMemory }
    fun maxMemoryPolicy(policy: MaxMemoryPolicy) = apply { config.maxMemoryPolicy = policy }
    fun withPersistence(volumeName: String) = apply { config.persistenceVolume = volumeName }

    fun build(): RedisTemplate = RedisTemplate(name) {
        version = this@RedisTemplateBuilder.config.version
        password = this@RedisTemplateBuilder.config.password
        port = this@RedisTemplateBuilder.config.port
        hostPort = this@RedisTemplateBuilder.config.hostPort
        maxMemory = this@RedisTemplateBuilder.config.maxMemory
        maxMemoryPolicy = this@RedisTemplateBuilder.config.maxMemoryPolicy
        persistenceVolume = this@RedisTemplateBuilder.config.persistenceVolume
    }
}
