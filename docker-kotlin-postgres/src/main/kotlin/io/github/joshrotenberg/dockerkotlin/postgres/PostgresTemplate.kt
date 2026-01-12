package io.github.joshrotenberg.dockerkotlin.postgres

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.template.AbstractTemplate
import io.github.joshrotenberg.dockerkotlin.template.HasConnectionString
import io.github.joshrotenberg.dockerkotlin.template.TemplateConfig
import io.github.joshrotenberg.dockerkotlin.template.WaitStrategy

/**
 * Configuration for PostgreSQL template.
 */
class PostgresConfig : TemplateConfig() {
    /** PostgreSQL version/tag. */
    var version: String = "16-alpine"

    /** Database name. */
    var database: String = "postgres"

    /** Username. */
    var username: String = "postgres"

    /** Password. */
    var password: String = "postgres"

    /** Port to expose (default 5432). */
    var port: Int = 5432

    /** Host port mapping (null for dynamic allocation). */
    var hostPort: Int? = null

    /** Init scripts directory to mount. */
    var initScriptsPath: String? = null

    /** Data volume for persistence. */
    var dataVolume: String? = null

    /** Additional PostgreSQL configuration options. */
    val postgresConfig: MutableMap<String, String> = mutableMapOf()
}

/**
 * PostgreSQL container template.
 *
 * Provides a pre-configured PostgreSQL container with sensible defaults.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val postgres = PostgresTemplate("my-postgres") {
 *     database = "mydb"
 *     username = "myuser"
 *     password = "secret"
 *     port = 5432
 * }
 *
 * postgres.use {
 *     it.startAndWait()
 *     println(it.connectionString()) // jdbc:postgresql://localhost:5432/mydb
 *     println(it.jdbcUrl())          // jdbc:postgresql://localhost:5432/mydb
 * }
 * ```
 *
 * Example usage (Java):
 * ```java
 * try (PostgresTemplate postgres = PostgresTemplate.builder("my-postgres")
 *         .database("mydb")
 *         .username("myuser")
 *         .password("secret")
 *         .build()) {
 *     postgres.startAndWaitBlocking();
 *     System.out.println(postgres.jdbcUrl());
 * }
 * ```
 */
class PostgresTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: PostgresConfig.() -> Unit = {}
) : AbstractTemplate(name, executor), HasConnectionString {

    override val config = PostgresConfig().apply(configure)

    override val image: String = "postgres"
    override val tag: String get() = config.version

    private var cachedMappedPort: Int? = null

    init {
        // Set default wait strategy - wait for PostgreSQL to accept connections
        if (config.waitStrategy == WaitStrategy.Running) {
            config.waitStrategy = WaitStrategy.ForCommand(
                "pg_isready", "-U", config.username, "-d", config.database
            )
        }
    }

    /** Set the PostgreSQL version. */
    fun version(version: String) = apply { config.version = version }

    /** Set the database name. */
    fun database(database: String) = apply { config.database = database }

    /** Set the username. */
    fun username(username: String) = apply { config.username = username }

    /** Set the password. */
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

    /** Mount init scripts from a directory. */
    fun withInitScripts(path: String) = apply {
        config.initScriptsPath = path
    }

    /** Enable persistence with a named volume. */
    fun withPersistence(volumeName: String) = apply {
        config.dataVolume = volumeName
    }

    /** Add a PostgreSQL configuration option. */
    fun withConfig(key: String, value: String) = apply {
        config.postgresConfig[key] = value
    }

    override fun configureRunCommand(cmd: RunCommand) {
        // Environment variables
        cmd.env("POSTGRES_DB", config.database)
        cmd.env("POSTGRES_USER", config.username)
        cmd.env("POSTGRES_PASSWORD", config.password)

        // Port mapping
        if (config.hostPort != null) {
            cmd.port(config.hostPort!!, config.port)
        } else {
            cmd.dynamicPort(config.port)
        }

        // Init scripts
        config.initScriptsPath?.let {
            cmd.volume(it, "/docker-entrypoint-initdb.d")
        }

        // Data persistence
        config.dataVolume?.let {
            cmd.namedVolume(it, "/var/lib/postgresql/data")
        }

        // Additional configuration via command args
        if (config.postgresConfig.isNotEmpty()) {
            val args = config.postgresConfig.flatMap { (k, v) -> listOf("-c", "$k=$v") }
            cmd.command("postgres", *args.toTypedArray())
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
     * Get the mapped host port.
     */
    suspend fun discoverMappedPort(): Int {
        cachedMappedPort?.let { return it }
        config.hostPort?.let { return it }

        val id = containerId ?: return config.port
        val output = executor.execute(listOf("port", id.value, config.port.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.port

        cachedMappedPort = port
        return port
    }

    /**
     * Get the mapped host port (blocking version).
     */
    fun getMappedPortBlocking(): Int {
        cachedMappedPort?.let { return it }
        config.hostPort?.let { return it }

        val id = containerId ?: return config.port
        val output = executor.executeBlocking(listOf("port", id.value, config.port.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.port

        cachedMappedPort = port
        return port
    }

    /**
     * Get the mapped host port (returns cached or static value only).
     */
    fun getMappedPort(): Int {
        return cachedMappedPort ?: config.hostPort ?: config.port
    }

    override fun connectionString(): String {
        return jdbcUrl()
    }

    /**
     * Get the JDBC URL for connecting to PostgreSQL.
     */
    fun jdbcUrl(): String {
        val port = getMappedPort()
        return "jdbc:postgresql://localhost:$port/${config.database}"
    }

    /**
     * Get the R2DBC URL for reactive connections.
     */
    fun r2dbcUrl(): String {
        val port = getMappedPort()
        return "r2dbc:postgresql://localhost:$port/${config.database}"
    }

    /**
     * Execute a SQL command using psql.
     */
    suspend fun executeSql(sql: String): String {
        val result = exec(
            "psql",
            "-U", config.username,
            "-d", config.database,
            "-c", sql
        )
        return result.stdout.trim()
    }

    companion object {
        /**
         * Create a new PostgresTemplate builder (for Java interop).
         */
        @JvmStatic
        fun builder(name: String): PostgresTemplateBuilder = PostgresTemplateBuilder(name)
    }
}

/**
 * Builder for PostgresTemplate (Java-friendly).
 */
class PostgresTemplateBuilder(private val name: String) {
    private val config = PostgresConfig()

    fun version(version: String) = apply { config.version = version }
    fun database(database: String) = apply { config.database = database }
    fun username(username: String) = apply { config.username = username }
    fun password(password: String) = apply { config.password = password }
    fun port(port: Int) = apply { config.port = port; config.hostPort = port }
    fun withInitScripts(path: String) = apply { config.initScriptsPath = path }
    fun withPersistence(volumeName: String) = apply { config.dataVolume = volumeName }

    fun build(): PostgresTemplate = PostgresTemplate(name) {
        version = this@PostgresTemplateBuilder.config.version
        database = this@PostgresTemplateBuilder.config.database
        username = this@PostgresTemplateBuilder.config.username
        password = this@PostgresTemplateBuilder.config.password
        port = this@PostgresTemplateBuilder.config.port
        hostPort = this@PostgresTemplateBuilder.config.hostPort
        initScriptsPath = this@PostgresTemplateBuilder.config.initScriptsPath
        dataVolume = this@PostgresTemplateBuilder.config.dataVolume
    }
}
