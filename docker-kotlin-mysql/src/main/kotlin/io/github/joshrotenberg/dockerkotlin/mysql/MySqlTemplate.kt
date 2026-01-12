package io.github.joshrotenberg.dockerkotlin.mysql

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.template.AbstractTemplate
import io.github.joshrotenberg.dockerkotlin.template.HasConnectionString
import io.github.joshrotenberg.dockerkotlin.template.TemplateConfig
import io.github.joshrotenberg.dockerkotlin.template.WaitStrategy

/**
 * Configuration for MySQL template.
 */
class MySqlConfig : TemplateConfig() {
    /** MySQL version/tag. */
    var version: String = "8.0"

    /** Database name. */
    var database: String = "mysql"

    /** Root password. */
    var rootPassword: String = "root"

    /** Username (optional, creates additional user). */
    var username: String? = null

    /** User password (required if username is set). */
    var password: String? = null

    /** Port to expose (default 3306). */
    var port: Int = 3306

    /** Host port mapping (null for dynamic allocation). */
    var hostPort: Int? = null

    /** Init scripts directory to mount. */
    var initScriptsPath: String? = null

    /** Data volume for persistence. */
    var dataVolume: String? = null

    /** Character set. */
    var characterSet: String = "utf8mb4"

    /** Collation. */
    var collation: String = "utf8mb4_unicode_ci"

    /** Additional MySQL configuration options. */
    val mysqlConfig: MutableMap<String, String> = mutableMapOf()
}

/**
 * MySQL container template.
 *
 * Provides a pre-configured MySQL container with sensible defaults.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val mysql = MySqlTemplate("my-mysql") {
 *     database = "mydb"
 *     rootPassword = "secret"
 *     username = "myuser"
 *     password = "userpass"
 * }
 *
 * mysql.use {
 *     it.startAndWait()
 *     println(it.jdbcUrl()) // jdbc:mysql://localhost:3306/mydb
 * }
 * ```
 */
class MySqlTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: MySqlConfig.() -> Unit = {}
) : AbstractTemplate(name, executor), HasConnectionString {

    override val config = MySqlConfig().apply(configure)

    override val image: String = "mysql"
    override val tag: String get() = config.version

    private var cachedMappedPort: Int? = null

    init {
        // Set default wait strategy - wait for MySQL to accept connections
        if (config.waitStrategy == WaitStrategy.Running) {
            config.waitStrategy = WaitStrategy.ForCommand(
                "mysqladmin", "ping", "-h", "localhost",
                "-u", "root", "-p${config.rootPassword}"
            )
        }
    }

    /** Set the MySQL version. */
    fun version(version: String) = apply { config.version = version }

    /** Set the database name. */
    fun database(database: String) = apply { config.database = database }

    /** Set the root password. */
    fun rootPassword(password: String) = apply { config.rootPassword = password }

    /** Set the username and password. */
    fun user(username: String, password: String) = apply {
        config.username = username
        config.password = password
    }

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

    /** Set character set and collation. */
    fun charset(characterSet: String, collation: String) = apply {
        config.characterSet = characterSet
        config.collation = collation
    }

    /** Add a MySQL configuration option. */
    fun withConfig(key: String, value: String) = apply {
        config.mysqlConfig[key] = value
    }

    override fun configureRunCommand(cmd: RunCommand) {
        // Environment variables
        cmd.env("MYSQL_ROOT_PASSWORD", config.rootPassword)
        cmd.env("MYSQL_DATABASE", config.database)

        config.username?.let { cmd.env("MYSQL_USER", it) }
        config.password?.let { cmd.env("MYSQL_PASSWORD", it) }

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
            cmd.namedVolume(it, "/var/lib/mysql")
        }

        // MySQL configuration via command args
        val args = mutableListOf<String>()
        args.add("--character-set-server=${config.characterSet}")
        args.add("--collation-server=${config.collation}")

        config.mysqlConfig.forEach { (k, v) ->
            args.add("--$k=$v")
        }

        if (args.isNotEmpty()) {
            cmd.command(*args.toTypedArray())
        }
    }

    override suspend fun checkCommandReady(command: List<String>): Boolean {
        return try {
            val result = exec(*command.toTypedArray())
            result.success || result.stdout.contains("mysqld is alive")
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
     * Get the JDBC URL for connecting to MySQL.
     */
    fun jdbcUrl(): String {
        val port = getMappedPort()
        return "jdbc:mysql://localhost:$port/${config.database}"
    }

    /**
     * Get the R2DBC URL for reactive connections.
     */
    fun r2dbcUrl(): String {
        val port = getMappedPort()
        return "r2dbc:mysql://localhost:$port/${config.database}"
    }

    /**
     * Execute a SQL command using mysql client.
     */
    suspend fun executeSql(sql: String): String {
        val user = config.username ?: "root"
        val pass = config.password ?: config.rootPassword
        val result = exec(
            "mysql",
            "-u", user,
            "-p$pass",
            "-D", config.database,
            "-e", sql
        )
        return result.stdout.trim()
    }

    companion object {
        /**
         * Create a new MySqlTemplate builder (for Java interop).
         */
        @JvmStatic
        fun builder(name: String): MySqlTemplateBuilder = MySqlTemplateBuilder(name)
    }
}

/**
 * Builder for MySqlTemplate (Java-friendly).
 */
class MySqlTemplateBuilder(private val name: String) {
    private val config = MySqlConfig()

    fun version(version: String) = apply { config.version = version }
    fun database(database: String) = apply { config.database = database }
    fun rootPassword(password: String) = apply { config.rootPassword = password }
    fun user(username: String, password: String) = apply {
        config.username = username
        config.password = password
    }

    fun port(port: Int) = apply { config.port = port; config.hostPort = port }
    fun withInitScripts(path: String) = apply { config.initScriptsPath = path }
    fun withPersistence(volumeName: String) = apply { config.dataVolume = volumeName }

    fun build(): MySqlTemplate = MySqlTemplate(name) {
        version = this@MySqlTemplateBuilder.config.version
        database = this@MySqlTemplateBuilder.config.database
        rootPassword = this@MySqlTemplateBuilder.config.rootPassword
        username = this@MySqlTemplateBuilder.config.username
        password = this@MySqlTemplateBuilder.config.password
        port = this@MySqlTemplateBuilder.config.port
        hostPort = this@MySqlTemplateBuilder.config.hostPort
        initScriptsPath = this@MySqlTemplateBuilder.config.initScriptsPath
        dataVolume = this@MySqlTemplateBuilder.config.dataVolume
    }
}
