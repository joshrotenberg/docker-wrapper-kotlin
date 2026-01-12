package io.github.joshrotenberg.dockerkotlin.core.command.secret

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.AbstractDockerCommand

/**
 * Command to remove one or more secrets.
 *
 * Equivalent to `docker secret rm`.
 *
 * Example usage:
 * ```kotlin
 * SecretRmCommand("my-secret").executeBlocking()
 * SecretRmCommand(listOf("secret1", "secret2")).executeBlocking()
 * ```
 */
class SecretRmCommand : AbstractDockerCommand<String> {

    private val secrets: List<String>

    constructor(secret: String, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.secrets = listOf(secret)
    }

    constructor(secrets: List<String>, executor: CommandExecutor = CommandExecutor()) : super(executor) {
        this.secrets = secrets
    }

    override fun buildArgs(): List<String> = buildList {
        add("secret")
        add("rm")
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
        fun builder(secret: String): SecretRmCommand = SecretRmCommand(secret)

        @JvmStatic
        fun builder(secrets: List<String>): SecretRmCommand = SecretRmCommand(secrets)
    }
}
