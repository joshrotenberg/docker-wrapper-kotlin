package io.github.joshrotenberg.dockerkotlin.core.command.secret

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to display detailed information on one or more secrets.
 *
 * Equivalent to `docker secret inspect`.
 *
 * Example usage:
 * ```kotlin
 * SecretInspectCommand("my-secret").executeBlocking()
 * SecretInspectCommand("my-secret").format("{{.ID}}").executeBlocking()
 * SecretInspectCommand(listOf("secret1", "secret2")).executeBlocking()
 * ```
 */
class SecretInspectCommand : AbstractDockerCommand<String> {

    private val secrets: List<String>
    private var format: String? = null
    private var pretty = false

    constructor(secret: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.secrets = listOf(secret)
    }

    constructor(secrets: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.secrets = secrets
    }

    /** Format output using a Go template. */
    fun format(format: String) = apply { this.format = format }

    /** Print the information in a human friendly format. */
    fun pretty() = apply { pretty = true }

    override fun buildArgs(): List<String> = buildList {
        add("secret")
        add("inspect")
        format?.let { add("--format"); add(it) }
        if (pretty) add("--pretty")
        addAll(secrets)
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(secret: String): SecretInspectCommand = SecretInspectCommand(secret)

        @JvmStatic
        fun builder(secrets: List<String>): SecretInspectCommand = SecretInspectCommand(secrets)
    }
}
