package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to log out from a Docker registry.
 *
 * Equivalent to `docker logout`.
 *
 * Example usage:
 * ```kotlin
 * LogoutCommand().executeBlocking()
 * LogoutCommand().server("gcr.io").executeBlocking()
 * ```
 */
class LogoutCommand(
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private var server: String? = null

    /** Set the registry server to log out from (defaults to Docker Hub). */
    fun server(server: String) = apply { this.server = server }

    override fun buildArgs(): List<String> = buildList {
        add("logout")
        server?.let { add(it) }
    }

    override suspend fun execute(): String {
        return executeRaw().stdout
    }

    override fun executeBlocking(): String {
        return executeRawBlocking().stdout
    }

    companion object {
        @JvmStatic
        fun builder(): LogoutCommand = LogoutCommand()
    }
}
