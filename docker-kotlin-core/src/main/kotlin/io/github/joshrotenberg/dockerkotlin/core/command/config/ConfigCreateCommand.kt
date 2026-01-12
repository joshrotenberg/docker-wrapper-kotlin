package io.github.joshrotenberg.dockerkotlin.core.command.config

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to create a config from a file or STDIN.
 *
 * Equivalent to `docker config create`.
 *
 * Example usage:
 * ```kotlin
 * ConfigCreateCommand("my-config", "/path/to/config.json").executeBlocking()
 * ConfigCreateCommand("my-config", "-").label("env", "prod").executeBlocking()
 * ```
 */
class ConfigCreateCommand(
    private val name: String,
    private val file: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val labels = mutableMapOf<String, String>()
    private var templateDriver: String? = null

    /** Add a label to the config. */
    fun label(key: String, value: String) = apply { labels[key] = value }

    /** Add multiple labels to the config. */
    fun labels(labels: Map<String, String>) = apply { this.labels.putAll(labels) }

    /** Template driver. */
    fun templateDriver(driver: String) = apply { templateDriver = driver }

    override fun buildArgs(): List<String> = buildList {
        add("config")
        add("create")
        labels.forEach { (key, value) -> add("--label"); add("$key=$value") }
        templateDriver?.let { add("--template-driver"); add(it) }
        add(name)
        add(file)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout.trim()
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout.trim()
    }

    companion object {
        @JvmStatic
        fun builder(name: String, file: String): ConfigCreateCommand =
            ConfigCreateCommand(name, file)
    }
}
