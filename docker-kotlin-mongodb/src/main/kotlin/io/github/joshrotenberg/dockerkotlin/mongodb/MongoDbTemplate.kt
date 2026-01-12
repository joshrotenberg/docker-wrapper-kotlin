package io.github.joshrotenberg.dockerkotlin.mongodb

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.template.AbstractTemplate
import io.github.joshrotenberg.dockerkotlin.template.HasConnectionString
import io.github.joshrotenberg.dockerkotlin.template.TemplateConfig
import io.github.joshrotenberg.dockerkotlin.template.WaitStrategy

/**
 * Configuration for MongoDB template.
 */
class MongoDbConfig : TemplateConfig() {
    /** MongoDB version/tag. */
    var version: String = "7.0"

    /** Database name. */
    var database: String = "test"

    /** Root username (optional, enables auth). */
    var rootUsername: String? = null

    /** Root password (required if rootUsername is set). */
    var rootPassword: String? = null

    /** Port to expose (default 27017). */
    var port: Int = 27017

    /** Host port mapping (null for dynamic allocation). */
    var hostPort: Int? = null

    /** Init scripts directory to mount. */
    var initScriptsPath: String? = null

    /** Data volume for persistence. */
    var dataVolume: String? = null

    /** Enable replica set mode. */
    var replicaSet: String? = null

    /** Additional MongoDB configuration options. */
    val mongoConfig: MutableList<String> = mutableListOf()
}

/**
 * MongoDB container template.
 *
 * Provides a pre-configured MongoDB container with sensible defaults.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val mongo = MongoDbTemplate("my-mongo") {
 *     database = "mydb"
 *     rootUsername = "admin"
 *     rootPassword = "secret"
 * }
 *
 * mongo.use {
 *     it.startAndWait()
 *     println(it.connectionString()) // mongodb://admin:secret@localhost:27017/mydb
 * }
 * ```
 */
class MongoDbTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: MongoDbConfig.() -> Unit = {}
) : AbstractTemplate(name, executor), HasConnectionString {

    override val config = MongoDbConfig().apply(configure)

    override val image: String = "mongo"
    override val tag: String get() = config.version

    private var cachedMappedPort: Int? = null

    init {
        // Set default wait strategy
        if (config.waitStrategy == WaitStrategy.Running) {
            config.waitStrategy = WaitStrategy.ForCommand(
                "mongosh", "--eval", "db.adminCommand('ping')"
            )
        }
    }

    /** Set the MongoDB version. */
    fun version(version: String) = apply { config.version = version }

    /** Set the database name. */
    fun database(database: String) = apply { config.database = database }

    /** Set root credentials (enables authentication). */
    fun credentials(username: String, password: String) = apply {
        config.rootUsername = username
        config.rootPassword = password
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

    /** Enable replica set mode. */
    fun withReplicaSet(name: String = "rs0") = apply {
        config.replicaSet = name
    }

    /** Add a MongoDB configuration option. */
    fun withConfig(option: String) = apply {
        config.mongoConfig.add(option)
    }

    override fun configureRunCommand(cmd: RunCommand) {
        // Environment variables for authentication
        config.rootUsername?.let { cmd.env("MONGO_INITDB_ROOT_USERNAME", it) }
        config.rootPassword?.let { cmd.env("MONGO_INITDB_ROOT_PASSWORD", it) }
        cmd.env("MONGO_INITDB_DATABASE", config.database)

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
            cmd.namedVolume(it, "/data/db")
        }

        // MongoDB command args
        val args = mutableListOf<String>()

        config.replicaSet?.let {
            args.add("--replSet")
            args.add(it)
        }

        args.addAll(config.mongoConfig)

        if (args.isNotEmpty()) {
            cmd.command("mongod", *args.toTypedArray())
        }
    }

    override suspend fun checkCommandReady(command: List<String>): Boolean {
        return try {
            val cmdWithAuth = if (config.rootUsername != null) {
                listOf(
                    "mongosh",
                    "-u", config.rootUsername!!,
                    "-p", config.rootPassword!!,
                    "--authenticationDatabase", "admin",
                    "--eval", "db.adminCommand('ping')"
                )
            } else {
                command
            }
            val result = exec(*cmdWithAuth.toTypedArray())
            result.success && result.stdout.contains("ok")
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
        val port = getMappedPort()
        val auth = if (config.rootUsername != null) {
            "${config.rootUsername}:${config.rootPassword}@"
        } else ""
        val authSource = if (config.rootUsername != null) "?authSource=admin" else ""
        return "mongodb://${auth}localhost:$port/${config.database}$authSource"
    }

    /**
     * Execute a MongoDB command using mongosh.
     */
    suspend fun executeCommand(command: String): String {
        val args = mutableListOf("mongosh", "--quiet")

        config.rootUsername?.let {
            args.addAll(listOf("-u", it, "-p", config.rootPassword!!, "--authenticationDatabase", "admin"))
        }

        args.addAll(listOf(config.database, "--eval", command))

        val result = exec(*args.toTypedArray())
        return result.stdout.trim()
    }

    companion object {
        /**
         * Create a new MongoDbTemplate builder (for Java interop).
         */
        @JvmStatic
        fun builder(name: String): MongoDbTemplateBuilder = MongoDbTemplateBuilder(name)
    }
}

/**
 * Builder for MongoDbTemplate (Java-friendly).
 */
class MongoDbTemplateBuilder(private val name: String) {
    private val config = MongoDbConfig()

    fun version(version: String) = apply { config.version = version }
    fun database(database: String) = apply { config.database = database }
    fun credentials(username: String, password: String) = apply {
        config.rootUsername = username
        config.rootPassword = password
    }

    fun port(port: Int) = apply { config.port = port; config.hostPort = port }
    fun withInitScripts(path: String) = apply { config.initScriptsPath = path }
    fun withPersistence(volumeName: String) = apply { config.dataVolume = volumeName }
    fun withReplicaSet(name: String) = apply { config.replicaSet = name }

    fun build(): MongoDbTemplate = MongoDbTemplate(name) {
        version = this@MongoDbTemplateBuilder.config.version
        database = this@MongoDbTemplateBuilder.config.database
        rootUsername = this@MongoDbTemplateBuilder.config.rootUsername
        rootPassword = this@MongoDbTemplateBuilder.config.rootPassword
        port = this@MongoDbTemplateBuilder.config.port
        hostPort = this@MongoDbTemplateBuilder.config.hostPort
        initScriptsPath = this@MongoDbTemplateBuilder.config.initScriptsPath
        dataVolume = this@MongoDbTemplateBuilder.config.dataVolume
        replicaSet = this@MongoDbTemplateBuilder.config.replicaSet
    }
}
