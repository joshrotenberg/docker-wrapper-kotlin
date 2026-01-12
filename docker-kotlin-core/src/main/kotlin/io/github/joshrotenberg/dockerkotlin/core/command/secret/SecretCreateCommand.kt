package io.github.joshrotenberg.dockerkotlin.core.command.secret

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to create a secret from a file or STDIN.
 *
 * Equivalent to `docker secret create`.
 *
 * Example usage:
 * ```kotlin
 * SecretCreateCommand("my-secret", "/path/to/secret.txt").executeBlocking()
 * SecretCreateCommand("my-secret", "-").label("env", "prod").executeBlocking()
 * ```
 */
class SecretCreateCommand(
    private val name: String,
    private val file: String,
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val labels = mutableMapOf<String, String>()
    private var driver: String? = null
    private var templateDriver: String? = null

    /** Add a label to the secret. */
    fun label(key: String, value: String) = apply { labels[key] = value }

    /** Add multiple labels to the secret. */
    fun labels(labels: Map<String, String>) = apply { this.labels.putAll(labels) }

    /** Secret driver. */
    fun driver(driver: String) = apply { this.driver = driver }

    /** Template driver. */
    fun templateDriver(driver: String) = apply { templateDriver = driver }

    override fun buildArgs(): List<String> = buildList {
        add("secret")
        add("create")
        driver?.let { add("--driver"); add(it) }
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
        fun builder(name: String, file: String): SecretCreateCommand =
            SecretCreateCommand(name, file)
    }
}
